/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Tue, 18 Mar 2025 13:45:36 +0100
 * @copyright  Copyright (c) 2025 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2025 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Tue, 18 Mar 2025 13:44:57 +0100
 */

package com.streamwide.smartms.lib.actuator.internal.usb;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static android.hardware.usb.UsbManager.EXTRA_DEVICE;
import static android.hardware.usb.UsbManager.EXTRA_PERMISSION_GRANTED;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.actuator.api.IUSBActionReceiver;
import com.streamwide.smartms.lib.actuator.api.IUSBDeviceCallback;
import com.streamwide.smartms.lib.actuator.api.USBDevice;
import com.streamwide.smartms.lib.actuator.api.UsbDeviceHandler;
import com.streamwide.smartms.lib.actuator.internal.felhr.usbserial.UsbSerialDevice;
import com.streamwide.smartms.lib.actuator.internal.felhr.usbserial.UsbSerialInterface;
import com.streamwide.smartms.lib.actuator.logger.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UsbManager {

    private static final String CLASS_NAME = "SWUsbManager";
    private static final String ACTION_USB_PERMISSION = "com.streamwide.sw_uart.usb.USB_PERMISSION";

    private static UsbManager mInstance;

    private UsbDevice mUsbDevice;
    private UsbDeviceHandler mDeviceHandler;
    private UsbDeviceConnection mUsbDeviceConnection;

    private Set<IUSBDeviceCallback> mUsbDeviceCallback = new HashSet<>();
    private UsbSerialDeviceConfig mUsbSerialDeviceConfig;
    private IUSBActionReceiver mUsbActionReceiver;

    private boolean mConnected= false;

    /**
     * SWUsbManager singleton getter
     *
     * @return SWUsbManager instance
     */
    @NonNull
    public static synchronized UsbManager getInstance()
    {
        if (mInstance == null) {
            mInstance = new UsbManager();
        }
        return mInstance;
    }

    /**
     * Constructor.
     * Private because SWUARTManager is meant to be a singleton.
     *
     *
     */
    private UsbManager()
    {

    }

    public boolean isDeviceConnected(int vendorId){
        if(mUsbDevice != null)
           return vendorId == mUsbDevice.getVendorId();
        else {
            return false ;
        }
    }

    private final BroadcastReceiver mUsbBroadcastReceiver = new BroadcastReceiver() {
        //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context,@NonNull Intent intent) {

            Logger.debug(CLASS_NAME,"intent received : "+intent.getAction());

            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {

              boolean granted;
              if(intent.getExtras() == null) {
                  android.hardware.usb.UsbManager usbManager = (android.hardware.usb.UsbManager)context.getSystemService(Context.USB_SERVICE);
                  granted = mUsbDevice != null && usbManager.hasPermission(mUsbDevice);
              } else {
                   granted = intent.getExtras().getBoolean(EXTRA_PERMISSION_GRANTED);
              }

                if (!granted) {
                    Logger.error(CLASS_NAME,"Permission is NOT granted: USB device will not be connected");
                    notifyDeviceCallbackError("SERIAL PERMISSION NOT GRANTED");

                    setConnected(false);
                } else {
                    /**
                     * connect the USB device
                     */
                    Logger.debug(CLASS_NAME,"Permission is granted: USB device will be connected");
                    connect(context);
                }
            } else if (ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {

                UsbDevice usbDevice = intent.getParcelableExtra(EXTRA_DEVICE);

                if(usbDevice != null)
                {
                    notifyDeviceCallbackPlugged(usbDevice);


                    /**
                     * connect the USB device
                     */
                    connect(context);
                }
            } else if (ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {

                notifyDeviceCallbackUnplugged();

                Logger.debug(CLASS_NAME,"USB detached");
                setConnected(false);
                disconnect(context);

            }
        }

    };

    private void configureSerialPort(UsbSerialDeviceConfig usbSerialDeviceConfig)  {

        Logger.debug(CLASS_NAME,"configureSerialPort with params");

        // Configure the UART port
        if(mDeviceHandler == null)
        {
            Logger.error(CLASS_NAME,"usb device is null");
            return ;
        }
        mDeviceHandler.getUsbSerialDevice().setBaudRate(usbSerialDeviceConfig.getBaudRate());
        mDeviceHandler.getUsbSerialDevice().setDataBits(usbSerialDeviceConfig.getDataBits());
        mDeviceHandler.getUsbSerialDevice().setStopBits(usbSerialDeviceConfig.getStopBits());
        mDeviceHandler.getUsbSerialDevice().setParity(usbSerialDeviceConfig.getParity());
        mDeviceHandler.getUsbSerialDevice().setFlowControl(usbSerialDeviceConfig.getFlowControl());
    }

    //Defining a Callback which triggers whenever data is read.
    private UsbSerialInterface.UsbReadCallback mUsbReadCallback = arg0 -> {

          String  data = new String(arg0, StandardCharsets.UTF_8);
          if(mDeviceHandler != null)
          {
              mDeviceHandler.handleDataRead(data);
          }

    };

    public boolean deviceHasPermission(@NonNull Context context){

        android.hardware.usb.UsbManager usbManager= (android.hardware.usb.UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        boolean hasPermissions = false;
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                UsbDevice usbDevice = entry.getValue();
                int deviceVID = usbDevice.getVendorId();
                Logger.debug(CLASS_NAME,"vendor Id: "+deviceVID);

                if (mUsbSerialDeviceConfig != null && deviceVID == mUsbSerialDeviceConfig.getDeviceVenderId()
                        && !usbManager.hasPermission(usbDevice)) {
                    PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION),
                            PendingIntent.FLAG_IMMUTABLE );
                    usbManager.requestPermission(usbDevice, pi);
                    hasPermissions = true;
                }
                if (hasPermissions)
                    break;
            }
        }

        return hasPermissions;
    }

    private boolean getDevice(@NonNull Context context, int deviceVendorId)
    {
        Logger.debug(CLASS_NAME,"getDevice: deviceVendorId = "+deviceVendorId);
        android.hardware.usb.UsbManager usbManager = (android.hardware.usb.UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            Logger.error(CLASS_NAME," getDevice: usbManager is Null");
            return false;
        }
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        boolean deviceFound = false;
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                mUsbDevice = entry.getValue();
                int deviceVID = 0;
                if (mUsbDevice != null) {
                    deviceVID = mUsbDevice.getVendorId();
                } else {
                    Logger.error(CLASS_NAME,"le UsbDevice est NULL ");
                }

                Logger.debug(CLASS_NAME,"vendor Id: "+deviceVID);

                if (deviceVID == deviceVendorId) //usb Vendor ID
                {
                    boolean usbDeviceHasPermission = usbManager.hasPermission(mUsbDevice);
                    Logger.debug(CLASS_NAME,"usb device has permission? "+usbDeviceHasPermission);
                    if (!usbDeviceHasPermission) {
                        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION),
                                PendingIntent.FLAG_IMMUTABLE);
                        usbManager.requestPermission(mUsbDevice, pi);
                        deviceFound = false;
                    } else {
                        deviceFound = true;
                    }
                    break;

                } else {
                    mUsbDeviceConnection = null;
                    mUsbDevice = null;
                    deviceFound = false;
                }
            }
        } else {
            Logger.error(CLASS_NAME,"usbDevices is empty");

            notifyDeviceCallbackError("usbDevices is empty");

            deviceFound= false;
        }

        return deviceFound;
    }

    private void registerForUsbBroadcastReceiver(@NonNull Context context, @NonNull USBDevice usbDevice)
    {
        UsbSerialDeviceConfig usbSerialDeviceConfig = new UsbSerialDeviceConfig(usbDevice.getVendorId());

        if(usbSerialDeviceConfig.getDeviceVenderId() == 0)
        {
            notifyDeviceCallbackError("Vendor device id is empty ");

            Logger.error(CLASS_NAME,"Vendor device id is null");
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        UsbSerialUtil.INSTANCE.registerBroadcast(context, filter, mUsbBroadcastReceiver);

        mUsbSerialDeviceConfig = usbSerialDeviceConfig;

    }

    private void unregisterForUsbDevice(@NonNull Context context){
        if(mUsbBroadcastReceiver != null) {
            context.unregisterReceiver(mUsbBroadcastReceiver);
        }
    }
    /**
     * Called to connect to the usb device when he has permission
     * @param context the context of application
     */
    public void connectUsbDevice(@NonNull Context context, @NonNull USBDevice usbDevice) {
        registerForUsbBroadcastReceiver(context, usbDevice);
        connect(context);
    }

    protected void connect(@NonNull Context context) {

        Logger.info(CLASS_NAME," we are trying to connect the usb device ");

        if (mUsbSerialDeviceConfig == null) {
            notifyDeviceCallbackError("UsbSerialDeviceConfig object is null");

            Logger.error(CLASS_NAME, "UsbSerialDeviceConfig object is null");

            return;
        }
        if(!getDevice(context, mUsbSerialDeviceConfig.getDeviceVenderId()))
        {
            notifyDeviceCallbackError("Could not find device with vendorId: "+mUsbSerialDeviceConfig.getDeviceVenderId());

            Logger.error(CLASS_NAME,"Could not find device with vendorId: "+mUsbSerialDeviceConfig.getDeviceVenderId());

            return;
        }

        android.hardware.usb.UsbManager usbManager = (android.hardware.usb.UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            Logger.error(CLASS_NAME,"usbManager object is null");
            return;
        }

        mUsbDeviceConnection = usbManager.openDevice(mUsbDevice);
        if (mUsbDeviceConnection != null) {
            UsbSerialDevice usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(mUsbDevice, mUsbDeviceConnection);

            if (usbSerialDevice != null) {

                if (usbSerialDevice.open()) { //Set Serial Connection Parameters.
                    Logger.debug(CLASS_NAME," the Serial port is opened");

                    mDeviceHandler = new UsbDeviceHandler(usbSerialDevice, mUsbSerialDeviceConfig.getDeviceVenderId());

                    configureSerialPort(mUsbSerialDeviceConfig);
                    usbSerialDevice.read(mUsbReadCallback);

                    setConnected(true);
                    notifyDeviceCallbackConnect();
                } else {
                    Logger.error(CLASS_NAME,"Serial port NOT OPEN");
                    notifyDeviceCallbackError("Serial port NOT OPEN");
                    notifyDeviceCallbackDisconnect();

                    setConnected(false);
                }
            } else {
                Logger.error(CLASS_NAME,"Serial port IS NULL");
                notifyDeviceCallbackError("Serial port IS NULL");
                notifyDeviceCallbackDisconnect();

                setConnected(false);
            }
        } else {
            Logger.error(CLASS_NAME,"UsbDeviceConnection IS NULL");
            notifyDeviceCallbackError("Usb Device Connection IS NULL");
            notifyDeviceCallbackDisconnect();

            setConnected(false);
        }
    }

    public void disconnect(@NonNull Context context)
    {

        if(mDeviceHandler != null) {
            Logger.info(CLASS_NAME,"disconnect from the connected UsbDevice - vendorId = "+mDeviceHandler.getDeviceVendorId());

            mDeviceHandler.getUsbSerialDevice().close();
            mDeviceHandler = null;
            mUsbDeviceConnection = null;
        }
        notifyDeviceCallbackDisconnect();


        /**
         * should not disconnect otherwise you will never be notified about plug event
         */
      //  unregisterForUsbDevice(context);

        mConnected = false;
    }

    private void sendData(@NonNull String data)
    {
        Logger.debug(CLASS_NAME,"sendData called data : "+data);

        if(mDeviceHandler == null)
        {
            Logger.debug(CLASS_NAME,"Usb serial device is null");
            notifyDeviceCallbackError("Usb serial device is null");

            return;
        }

        mDeviceHandler.writeData(data);

    }

    @Deprecated
    public void senDataAction(@NonNull String outDataAction){

        sendData(outDataAction);
    }

    public boolean isConnected() {
        return mConnected;
    }

    @Nullable
    public UsbSerialDeviceConfig getUsbSerialDeviceConfig() {
        return mUsbSerialDeviceConfig;
    }

    @Nullable
    public UsbDeviceHandler getConnectedUsbDeviceHandler() {
        return mDeviceHandler;
    }

    void setConnected(boolean connected) {
        mConnected = connected;
    }

    @Nullable
    public IUSBActionReceiver getUsbActionReceiver() {
        return mUsbActionReceiver;
    }

    @Deprecated
    public void registerForUsbDeviceCallback(@Nullable IUSBDeviceCallback usbDeviceCallback, @Nullable IUSBActionReceiver usbActionReceiver) {
        Logger.info(CLASS_NAME,"registerForUsbDeviceCallback");
        if (usbDeviceCallback != null) {
            this.mUsbDeviceCallback.add(usbDeviceCallback);
        }
        if (usbActionReceiver != null) {
            this.mUsbActionReceiver = usbActionReceiver;
        }
    }

    public void registerForUsbDeviceStatusCallback(@NonNull IUSBDeviceCallback usbDeviceCallback) {
        this.mUsbDeviceCallback.add(usbDeviceCallback);
    }

    public void unregisterForUsbDeviceStatusCallback(@NonNull IUSBDeviceCallback usbDeviceCallback) {
        Logger.info(CLASS_NAME,"unregisterForUsbDeviceStatusCallback");
        this.mUsbDeviceCallback.remove(usbDeviceCallback);
    }
 public void unregisterForUsbDeviceActionsCallback(@NonNull IUSBActionReceiver usbActionReceiver) {
        Logger.info(CLASS_NAME,"unregisterForUsbDeviceActionsCallback");
        if (usbActionReceiver == mUsbActionReceiver) {
            this.mUsbActionReceiver = null;
        }
    }

    private void notifyDeviceCallbackError(String error){
        Set<IUSBDeviceCallback> callbacks = new HashSet<>(mUsbDeviceCallback);
        for (IUSBDeviceCallback callback : callbacks) {
            callback.onError(error);
        }
    }
    private void notifyDeviceCallbackDisconnect(){
        Set<IUSBDeviceCallback> callbacks = new HashSet<>(mUsbDeviceCallback);
        for (IUSBDeviceCallback callback : callbacks) {
            callback.onDisconnect();
        }
    }
    private void notifyDeviceCallbackConnect(){
        Set<IUSBDeviceCallback> callbacks = new HashSet<>(mUsbDeviceCallback);
        for (IUSBDeviceCallback callback : callbacks) {
            callback.onConnect();
        }
    }
    private void notifyDeviceCallbackPlugged(UsbDevice usbDevice){
        Set<IUSBDeviceCallback> callbacks = new HashSet<>(mUsbDeviceCallback);
        for (IUSBDeviceCallback callback : callbacks) {
            callback.onPlugged(usbDevice);
        }
    }
    private void notifyDeviceCallbackUnplugged(){
        Set<IUSBDeviceCallback> callbacks = new HashSet<>(mUsbDeviceCallback);
        for (IUSBDeviceCallback callback : callbacks) {
            callback.onUnPlugged();
        }
    }
}
