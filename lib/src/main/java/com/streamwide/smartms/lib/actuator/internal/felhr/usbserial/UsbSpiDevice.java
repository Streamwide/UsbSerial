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

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.actuator.internal.felhr.deviceids.CP2130Ids;

public abstract class UsbSpiDevice implements UsbSpiInterface
{

    protected static final int USB_TIMEOUT = 5000;

    protected final UsbDevice device;
    protected final UsbDeviceConnection connection;

    @Nullable
    protected SerialBuffer serialBuffer;
    @Nullable
    protected WriteThread writeThread;
    @Nullable
    protected ReadThread readThread;

    // Endpoints for synchronous read and write operations
    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;

    public UsbSpiDevice(@NonNull UsbDevice device,@Nullable UsbDeviceConnection connection)
    {
        this.device = device;
        this.connection = connection;
        this.serialBuffer = new SerialBuffer(false);
    }

    @Nullable
    public static UsbSpiDevice createUsbSerialDevice(@NonNull UsbDevice device,@Nullable UsbDeviceConnection connection)
    {
        return createUsbSerialDevice(device, connection, -1);
    }

    @Nullable
    public static UsbSpiDevice createUsbSerialDevice(@NonNull UsbDevice device,@Nullable UsbDeviceConnection connection, int iface)
    {
        int vid = device.getVendorId();
        int pid = device.getProductId();

        if(CP2130Ids.isDeviceSupported(vid, pid))
            return new CP2130SpiDevice(device, connection, iface);
        else
            return null;
    }


    @Override
    public abstract boolean connectSPI();

    @Override
    public abstract void writeMOSI(@NonNull byte[] buffer);

    @Override
    public abstract void readMISO(int lengthBuffer);

    @Override
    public abstract void writeRead(@NonNull byte[] buffer, int lengthRead);

    @Override
    public abstract void setClock(int clockDivider);

    @Override
    public abstract void selectSlave(int nSlave);

    @Override
    public void setMISOCallback(@Nullable UsbMISOCallback misoCallback)
    {
        if(readThread != null) {
            readThread.setCallback(misoCallback);
        }
    }

    @Override
    public abstract int getClockDivider();

    @Override
    public abstract int getSelectedSlave();

    @Override
    public abstract void closeSPI();

    protected class WriteThread extends AbstractWorkerThread
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
        private UsbMISOCallback misoCallback;
        private UsbEndpoint inEndpoint;

        public void setCallback(@Nullable UsbMISOCallback misoCallback)
        {
            this.misoCallback = misoCallback;
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
                onReceivedData(dataReceived);
            }

        }

        public void setUsbEndpoint(UsbEndpoint inEndpoint)
        {
            this.inEndpoint = inEndpoint;
        }

        private void onReceivedData(byte[] data)
        {
            if(misoCallback != null)
                misoCallback.onReceivedData(data);
        }
    }

    protected void setThreadsParams(@Nullable UsbEndpoint inEndpoint,@Nullable UsbEndpoint outEndpoint)
    {
        if(writeThread != null)
            writeThread.setUsbEndpoint(outEndpoint);

        if(readThread != null)
            readThread.setUsbEndpoint(inEndpoint);
    }

    /*
     * Kill workingThread; This must be called when closing a device
     */
    protected void killWorkingThread()
    {
        if(readThread != null)
        {
            readThread.stopThread();
            readThread = null;
        }
    }

    /*
     * Restart workingThread if it has been killed before
     */
    protected void restartWorkingThread()
    {
        readThread = new ReadThread();
        readThread.start();
        while(!readThread.isAlive()){} // Busy waiting
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
