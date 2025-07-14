/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 18 Feb 2021 11:01:26 +0100
 * @copyright  Copyright (c) 2021 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2021 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 18 Feb 2021 11:01:25 +0100
 */

package com.streamwide.smartms.lib.actuator.internal.felhr.usbserial;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.actuator.internal.felhr.deviceids.CH34xIds;
import com.streamwide.smartms.lib.actuator.internal.felhr.deviceids.CP210xIds;
import com.streamwide.smartms.lib.actuator.internal.felhr.deviceids.FTDISioIds;
import com.streamwide.smartms.lib.actuator.internal.felhr.deviceids.PL2303Ids;

public abstract class UsbSerialDevice implements UsbSerialInterface
{
    private static final String CLASS_ID = UsbSerialDevice.class.getSimpleName();
    private static final String CLASS_NAME = "UsbSerialDevice";
    public static final String CDC = "cdc";
    public static final String CH34x = "ch34x";
    public static final String CP210x = "cp210x";
    public static final String FTDI = "ftdi";
    public static final String PL2303 = "pl2303";

    protected static final String COM_PORT = "COM ";

    static final boolean mr1Version = true;
    protected final UsbDevice device;
    protected final UsbDeviceConnection connection;

    protected static final int USB_TIMEOUT = 0;

    @Nullable
    protected SerialBuffer serialBuffer;

    @Nullable
    protected WorkerThread workerThread;
    @Nullable
    protected WriteThread writeThread;
    @Nullable
    protected ReadThread readThread;

    // Endpoints for synchronous read and write operations
    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;

    // InputStream and OutputStream (only for sync api)
    @Nullable
    protected SerialInputStream inputStream;
    @Nullable
    protected SerialOutputStream outputStream;

    protected boolean asyncMode;

    private String portName = "";
    protected boolean isOpen;

    public UsbSerialDevice(@NonNull UsbDevice device, @Nullable UsbDeviceConnection connection)
    {
        this.device = device;
        this.connection = connection;
        this.asyncMode = true;
        serialBuffer = new SerialBuffer(mr1Version);
    }

    @Nullable
    public static UsbSerialDevice createUsbSerialDevice(@NonNull UsbDevice device, @Nullable UsbDeviceConnection connection)
    {
        return createUsbSerialDevice(device, connection, -1);
    }

    @Nullable
    public static UsbSerialDevice createUsbSerialDevice(@NonNull UsbDevice device, @Nullable UsbDeviceConnection connection, int iface)
    {
		/*
		 * It checks given vid and pid and will return a custom driver or a CDC serial driver.
		 * When CDC is returned open() method is even more important, its response will inform about if it can be really
		 * opened as a serial device with a generic CDC serial driver
		 */
        int vid = device.getVendorId();
        int pid = device.getProductId();

        if(FTDISioIds.isDeviceSupported(device))
            return new FTDISerialDevice(device, connection, iface);
        else if(CP210xIds.isDeviceSupported(vid, pid))
            return new CP2102SerialDevice(device, connection, iface);
        else if(PL2303Ids.isDeviceSupported(vid, pid))
            return new PL2303SerialDevice(device, connection, iface);
        else if(CH34xIds.isDeviceSupported(vid, pid))
            return new CH34xSerialDevice(device, connection, iface);
        else if(isCdcDevice(device))
            return new CDCSerialDevice(device, connection, iface);
        else
            return null;
    }

    @NonNull
    public static UsbSerialDevice createUsbSerialDevice(@NonNull String type,@NonNull UsbDevice device,@NonNull UsbDeviceConnection connection, int iface){
        if(type.equals(FTDI)){
            return new FTDISerialDevice(device, connection, iface);
        }else if(type.equals(CP210x)){
            return new CP2102SerialDevice(device, connection, iface);
        }else if(type.equals(PL2303)){
            return new PL2303SerialDevice(device, connection, iface);
        }else if(type.equals(CH34x)){
            return new CH34xSerialDevice(device, connection, iface);
        }else if(type.equals(CDC)){
            return new CDCSerialDevice(device, connection, iface);
        }else{
            throw new IllegalArgumentException("Invalid type argument. Must be:cdc, ch34x, cp210x, ftdi or pl2303");
        }
    }

    public static boolean isSupported(@NonNull UsbDevice device)
    {
        int vid = device.getVendorId();
        int pid = device.getProductId();

        if(FTDISioIds.isDeviceSupported(device))
            return true;
        else if(CP210xIds.isDeviceSupported(vid, pid))
            return true;
        else if(PL2303Ids.isDeviceSupported(vid, pid))
            return true;
        else if(CH34xIds.isDeviceSupported(vid, pid))
            return true;
        else if(isCdcDevice(device))
            return true;
        else
            return false;
    }

    // Common Usb Serial Operations (I/O Asynchronous)
    @Override
    public abstract boolean open();

    @Override
    public void write(@NonNull byte[] buffer)
    {
        if(asyncMode)
            serialBuffer.putWriteBuffer(buffer);
    }

    /**
     * <p>
     *     Use this setter <strong>before</strong> calling {@link #open()} to override the default baud rate defined in this particular class.
     * </p>
     *
     * <p>
     *     This is a workaround for devices where calling {@link #setBaudRate(int)} has no effect once {@link #open()} has been called.
     * </p>
     *
     * @param initialBaudRate baud rate to be used when initializing the serial connection
     */
    public void setInitialBaudRate(int initialBaudRate) {
        // this class does not implement initialBaudRate
    }

    /**
     * Classes that do not implement {@link #setInitialBaudRate(int)} should always return -1
     *
     * @return initial baud rate used when initializing the serial connection
     */
    public int getInitialBaudRate() {
        return -1;
    }

    @Override
    public int read(@Nullable UsbReadCallback mCallback) {
        if (!asyncMode)
            return -1;

        if (workerThread != null) {
            workerThread.setCallback(mCallback);
            workerThread.getUsbRequest().queue(serialBuffer.getReadBuffer());
        }

        return 0;
    }


    @Override
    public abstract void close();

    // Common Usb Serial Operations (I/O Synchronous)
    @Override
    public abstract boolean syncOpen();

    @Override
    public abstract void syncClose();

    @Override
    public int syncWrite(@Nullable byte[] buffer, int timeout)
    {
        if(!asyncMode)
        {
            if(buffer == null)
                return 0;

            return connection.bulkTransfer(outEndpoint, buffer, buffer.length, timeout);
        }else
        {
            return -1;
        }
    }

    @Override
    public int syncRead(@Nullable byte[] buffer, int timeout)
    {
        if(asyncMode)
        {
            return -1;
        }

        if (buffer == null)
            return 0;

        return connection.bulkTransfer(inEndpoint, buffer, buffer.length, timeout);
    }

    @Override
    public int syncWrite(@Nullable byte[] buffer, int offset, int length, int timeout) {
        if(!asyncMode)
        {
            if(buffer == null)
                return 0;

            return connection.bulkTransfer(outEndpoint, buffer, offset, length, timeout);
        }else
        {
            return -1;
        }
    }

    @Override
    public int syncRead(@Nullable byte[] buffer, int offset, int length, int timeout) {
        if(asyncMode)
        {
            return -1;
        }

        if (buffer == null)
            return 0;

        return connection.bulkTransfer(inEndpoint, buffer, offset, length, timeout);
    }

    // Serial port configuration
    @Override
    public abstract void setBaudRate(int baudRate);
    @Override
    public abstract void setDataBits(int dataBits);
    @Override
    public abstract void setStopBits(int stopBits);
    @Override
    public abstract void setParity(int parity);
    @Override
    public abstract void setFlowControl(int flowControl);
    @Override
    public abstract void setBreak(boolean state);

    @Nullable
    public SerialInputStream getInputStream() {
        if(asyncMode)
            throw new IllegalStateException("InputStream only available in Sync mode. \n" +
                    "Open the port with syncOpen()");
        return inputStream;
    }


    public boolean isOpen(){
        return isOpen;
    }

    boolean isFTDIDevice()
    {
        return (this instanceof FTDISerialDevice);
    }

    public static boolean isCdcDevice(@NonNull UsbDevice device)
    {
        int iIndex = device.getInterfaceCount();
        for(int i=0;i<=iIndex-1;i++)
        {
            UsbInterface iface = device.getInterface(i);
            if(iface.getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA)
                return true;
        }
        return false;
    }


    /*
     * WorkerThread waits for request notifications from IN endpoint
     */
    protected class WorkerThread extends AbstractWorkerThread
    {
        private final UsbSerialDevice usbSerialDevice;

        private UsbReadCallback callback;
        private UsbRequest requestIN;

        public WorkerThread(UsbSerialDevice usbSerialDevice)
        {
            this.usbSerialDevice = usbSerialDevice;
        }

        @Override
        public void doRun()
        {
            UsbRequest request = null;
            if (connection != null) {
                request = connection.requestWait();
            }
            if(request != null && request.getEndpoint().getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                    && request.getEndpoint().getDirection() == UsbConstants.USB_DIR_IN)
            {
                byte[] data = serialBuffer.getDataReceived();

                // FTDI devices reserves two first bytes of an IN endpoint with info about
                // modem and Line.
                if(isFTDIDevice())
                {
                    ((FTDISerialDevice) usbSerialDevice).ftdiUtilities.checkModemStatus(data); //Check the Modem status
                    serialBuffer.clearReadBuffer();

                    if(data.length > 2)
                    {
                        data = FTDISerialDevice.adaptArray(data);
                        onReceivedData(data);
                    }
                }else
                {
                    // Clear buffer, execute the callback
                    serialBuffer.clearReadBuffer();
                    onReceivedData(data);
                }
                // Queue a new request
                requestIN.queue(serialBuffer.getReadBuffer());
            }
        }

        public void setCallback(UsbReadCallback callback)
        {
            this.callback = callback;
        }

        public void setUsbRequest(UsbRequest request)
        {
            this.requestIN = request;
        }

        public UsbRequest getUsbRequest()
        {
            return requestIN;
        }

        private void onReceivedData(byte[] data)
        {
            if(callback != null)
                callback.onReceivedData(data);
        }
    }

    class WriteThread extends AbstractWorkerThread
    {
        private UsbEndpoint outEndpoint;

        @Override
        public void doRun()
        {
            byte[] data = serialBuffer.getWriteBuffer();
            if(data.length > 0)
                connection.bulkTransfer(outEndpoint, data, data.length, USB_TIMEOUT);
        }

        public void setUsbEndpoint(UsbEndpoint outEndpoint)
        {
            this.outEndpoint = outEndpoint;
        }
    }

    protected class ReadThread extends AbstractWorkerThread
    {
        private final UsbSerialDevice usbSerialDevice;

        private UsbReadCallback callback;
        private UsbEndpoint inEndpoint;

        public ReadThread(UsbSerialDevice usbSerialDevice)
        {
            this.usbSerialDevice = usbSerialDevice;
        }

        public void setCallback(UsbReadCallback callback)
        {
            this.callback = callback;
        }

        @Override
        public void doRun()
        {
            byte[] dataReceived = null;
            int numberBytes;
            if(inEndpoint != null)
                numberBytes = connection.bulkTransfer(inEndpoint, serialBuffer.getBufferCompatible(),
                        SerialBuffer.DEFAULT_READ_BUFFER_SIZE, 0);
            else
                numberBytes = 0;

            if(numberBytes > 0)
            {
                dataReceived = serialBuffer.getDataReceivedCompatible(numberBytes);

                // FTDI devices reserve two first bytes of an IN endpoint with info about
                // modem and Line.
                if(isFTDIDevice())
                {
                    ((FTDISerialDevice) usbSerialDevice).ftdiUtilities.checkModemStatus(dataReceived);

                    if(dataReceived.length > 2)
                    {
                        dataReceived = FTDISerialDevice.adaptArray(dataReceived);
                        onReceivedData(dataReceived);
                    }
                }else
                {
                    onReceivedData(dataReceived);
                }
            }
        }

        public void setUsbEndpoint(UsbEndpoint inEndpoint)
        {
            this.inEndpoint = inEndpoint;
        }

        private void onReceivedData(byte[] data)
        {
            if(callback != null)
                callback.onReceivedData(data);
        }
    }

    protected void setSyncParams(@NonNull UsbEndpoint inEndpoint,@NonNull UsbEndpoint outEndpoint)
    {
        this.inEndpoint = inEndpoint;
        this.outEndpoint = outEndpoint;
    }

    protected void setThreadsParams(@NonNull UsbRequest request,@NonNull UsbEndpoint endpoint)
    {
        if(writeThread != null){
            writeThread.setUsbEndpoint(endpoint);
            workerThread.setUsbRequest(request);
        }
    }

    /*
     * Kill workingThread; This must be called when closing a device
     */
    protected void killWorkingThread()
    {
        if(workerThread != null)
        {
            workerThread.stopThread();
            workerThread = null;
        }
    }

    /*
     * Restart workingThread if it has been killed before
     */
    protected void restartWorkingThread()
    {
        if(workerThread == null)
        {
            workerThread = new WorkerThread(this);
            workerThread.start();
            while(!workerThread.isAlive()){} // Busy waiting
        }
    }

    protected void killWriteThread()
    {
        if(writeThread != null)
        {
            writeThread.stopThread();
            writeThread = null;
        }
    }

    protected void restartWriteThread()
    {
        if(writeThread == null)
        {
            writeThread = new WriteThread();
            writeThread.start();
            while(!writeThread.isAlive()){} // Busy waiting
        }
    }
}
