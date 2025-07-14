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

package com.streamwide.smartms.lib.actuator.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.actuator.internal.usb.UsbManager;
import com.streamwide.smartms.lib.actuator.internal.usb.UsbSerialDeviceConfig;


public class STWUsbActionManager {

    private static final String CLASS_NAME = "STWUsbActionManager";

    private static STWUsbActionManager mInstance;

    /**
     * STWUsbActionManager singleton getter
     *
     * @return STWUsbActionManager instance
     */
    @NonNull
    public static synchronized STWUsbActionManager getInstance()
    {
        if (mInstance == null) {
            mInstance = new STWUsbActionManager();
        }
        return mInstance;
    }

    /**
     * Constructor.
     * Private because STWUsbActionManager is meant to be a singleton.
     *
     *
     */
    private STWUsbActionManager()
    {

    }


    /**
     * Called to connect to the usb device when he has permission
     * @param context the context of application
     * @param usbDevice the USBDevice to which we will try to connect
     */
    public void connectUsbDevice(@NonNull Context context, @NonNull USBDevice usbDevice) {
        UsbManager.getInstance().connectUsbDevice(context, usbDevice);
    }

    /**
     * Called to disconnect from the connected USBDevice
     * @param context the context of application
     */
    public void disconnectFromUsbDevice(@NonNull Context context) {
        UsbManager.getInstance().disconnect(context);
    }

    /**
     * Called to check if USB device is connected or not
     * @return true if usb device is connected, false otherwise
     *
     * @Depricated This method is deprecated as of version 4.0.
     * Use {@link #isDeviceConnected()} That will check the usb connectivity  .
     */
    public boolean isUsbDeviceConnected(){
        return UsbManager.getInstance().isConnected();
    }



    /**
     * Called to check if USB device is connected or not
     * @return true if usb device is connected, false otherwise
     */
    public boolean isUsbDeviceConnected(int vendorId){
        return UsbManager.getInstance().isDeviceConnected(vendorId) && UsbManager.getInstance().isConnected();
    }
    public UsbDeviceHandler getConnectedDeviceHandler(){
        return UsbManager.getInstance().getConnectedUsbDeviceHandler();
    }


    @Deprecated
    public void sendDataAction(@NonNull String outDataAction) {
        UsbManager.getInstance().senDataAction(outDataAction);
    }

    /**
     * Called to check if device has permission
     * @param context the context of application
     * @return true if device has permission, false otherwise
     */
    public boolean deviceHasPermission(@NonNull Context context){
        return UsbManager.getInstance().deviceHasPermission(context);
    }

    /**
     * Called to get the configuration of usb device
     * @return the device configuration
     */
    @NonNull
    public UsbSerialDeviceConfig deviceConfiguration(){
        return UsbManager.getInstance().getUsbSerialDeviceConfig();
    }


    /**
     * Called to set the usb action receivers callback
     * @param usbDeviceCallback usb device callback
     * @param usbActionReceiver usb action receiver callback
     */
    @Deprecated
    public void registerForUsbDeviceCallback(@Nullable IUSBDeviceCallback usbDeviceCallback, @Nullable IUSBActionReceiver usbActionReceiver) {
        UsbManager.getInstance().registerForUsbDeviceCallback(usbDeviceCallback, usbActionReceiver);
    }

    public void registerForUsbDeviceStatusCallback(@NonNull IUSBDeviceCallback usbDeviceCallback) {
        UsbManager.getInstance().registerForUsbDeviceStatusCallback(usbDeviceCallback);
    }

    /**
     * Called to unregister the usb status receivers callback
     * @param usbDeviceCallback usb device callback
     */
    public void unregisterForUsbDeviceStatusCallback(@Nullable IUSBDeviceCallback usbDeviceCallback) {
        UsbManager.getInstance().unregisterForUsbDeviceStatusCallback(usbDeviceCallback);
    }
    /**
     * Called to unregister the usb action receivers callback
     * @param usbActionReceiver usb device actions callback
     */
    public void unregisterForUsbDeviceActionsCallback(@Nullable IUSBActionReceiver usbActionReceiver) {
        UsbManager.getInstance().unregisterForUsbDeviceActionsCallback(usbActionReceiver);
    }

}
