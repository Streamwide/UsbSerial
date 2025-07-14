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

import com.streamwide.smartms.lib.actuator.internal.felhr.utils.SafeUsbRequest;
import com.streamwide.smartms.lib.actuator.logger.Logger;

public class PL2303SerialDevice extends UsbSerialDevice
{
    private static final String CLASS_ID = PL2303SerialDevice.class.getSimpleName();
    private static final String CLASS_NAME = "PL2303SerialDevice";

    private static final int PL2303_REQTYPE_HOST2DEVICE_VENDOR = 0x40;
    private static final int PL2303_REQTYPE_DEVICE2HOST_VENDOR = 0xC0;
    private static final int PL2303_REQTYPE_HOST2DEVICE = 0x21;

    private static final int PL2303_VENDOR_WRITE_REQUEST = 0x01;
    private static final int PL2303_SET_LINE_CODING = 0x20;
    private static final int PL2303_SET_CONTROL_REQUEST = 0x22;

    private final byte[] defaultSetLine = new byte[]{
            (byte) 0x80, // [0:3] Baud rate (reverse hex encoding 9600:00 00 25 80 -> 80 25 00 00)
            (byte) 0x25,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00, // [4] Stop Bits (0=1, 1=1.5, 2=2)
            (byte) 0x00, // [5] Parity (0=NONE 1=ODD 2=EVEN 3=MARK 4=SPACE)
            (byte) 0x08  // [6] Data Bits (5=5, 6=6, 7=7, 8=8)
    };


    private final UsbInterface mInterface;
    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;


    public PL2303SerialDevice(@NonNull UsbDevice device, @NonNull UsbDeviceConnection connection, int iface)
    {
        super(device, connection);

        if (iface > 1)
        {
            throw new IllegalArgumentException("Multi-interface PL2303 devices not supported!");
        }

        mInterface = device.getInterface(iface >= 0 ? iface : 0);
    }

    @Override
    public boolean open()
    {
        boolean ret = openPL2303();

        if(ret)
        {
            // Initialize UsbRequest
            UsbRequest requestIN = new SafeUsbRequest();
            requestIN.initialize(connection, inEndpoint);

            // Restart the working thread if it has been killed before and  get and claim interface
            restartWorkingThread();
            restartWriteThread();

            // Pass references to the threads
            setThreadsParams(requestIN, outEndpoint);

            asyncMode = true;
            isOpen = true;

            return true;
        }else
        {
            isOpen = false;
            return false;
        }
    }

    @Override
    public void close()
    {
        killWorkingThread();
        killWriteThread();
        connection.releaseInterface(mInterface);
        isOpen = false;
    }

    @Override
    public boolean syncOpen()
    {
        boolean ret = openPL2303();
        if(ret)
        {
            setSyncParams(inEndpoint, outEndpoint);
            asyncMode = false;
            isOpen = true;

            // Init Streams
            inputStream = new SerialInputStream(this);
            outputStream = new SerialOutputStream(this);

            return true;
        }else
        {
            isOpen = false;
            return false;
        }
    }

    @Override
    public void syncClose()
    {
        connection.releaseInterface(mInterface);
        isOpen = false;
    }

    @Override
    public void setBaudRate(int baudRate)
    {
        byte[] tempBuffer = new byte[4];
        tempBuffer[0] = (byte) (baudRate & 0xff);
        tempBuffer[1] = (byte) (baudRate >> 8 & 0xff);
        tempBuffer[2] = (byte) (baudRate >> 16 & 0xff);
        tempBuffer[3] = (byte) (baudRate >> 24 & 0xff);
        if(tempBuffer[0] != defaultSetLine[0] || tempBuffer[1] != defaultSetLine[1] || tempBuffer[2] != defaultSetLine[2]
                || tempBuffer[3] != defaultSetLine[3])
        {
            defaultSetLine[0] = tempBuffer[0];
            defaultSetLine[1] = tempBuffer[1];
            defaultSetLine[2] = tempBuffer[2];
            defaultSetLine[3] = tempBuffer[3];
            setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
        }
    }

    @Override
    public void setDataBits(int dataBits)
    {
        switch(dataBits)
        {
            case DATA_BITS_5:
                if(defaultSetLine[6] != 0x05)
                {
                    defaultSetLine[6] = 0x05;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            case DATA_BITS_6:
                if(defaultSetLine[6] != 0x06)
                {
                    defaultSetLine[6] = 0x06;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            case DATA_BITS_7:
                if(defaultSetLine[6] != 0x07)
                {
                    defaultSetLine[6] = 0x07;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            case DATA_BITS_8:
                if(defaultSetLine[6] != 0x08)
                {
                    defaultSetLine[6] = 0x08;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            default:
                return;
        }

    }

    @Override
    public void setStopBits(int stopBits)
    {
        switch(stopBits)
        {
            case STOP_BITS_1:
                if(defaultSetLine[4] != 0x00)
                {
                    defaultSetLine[4] = 0x00;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            case STOP_BITS_15:
                if(defaultSetLine[4] != 0x01)
                {
                    defaultSetLine[4] = 0x01;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            case STOP_BITS_2:
                if(defaultSetLine[4] != 0x02)
                {
                    defaultSetLine[4] = 0x02;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            default:
                return;
        }
    }

    @Override
    public void setParity(int parity)
    {
        switch(parity)
        {
            case PARITY_NONE:
                if(defaultSetLine[5] != 0x00)
                {
                    defaultSetLine[5] = 0x00;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            case PARITY_ODD:
                if(defaultSetLine[5] != 0x01)
                {
                    defaultSetLine[5] = 0x01;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            case PARITY_EVEN:
                if(defaultSetLine[5] != 0x02)
                {
                    defaultSetLine[5] = 0x02;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            case PARITY_MARK:
                if(defaultSetLine[5] != 0x03)
                {
                    defaultSetLine[5] = 0x03;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            case PARITY_SPACE:
                if(defaultSetLine[5] != 0x04)
                {
                    defaultSetLine[5] = 0x04;
                    setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine);
                }
                break;
            default:
                return;
        }

    }

    @Override
    public void setFlowControl(int flowControl)
    {
        // TODO

    }

    @Override
    public void setBreak(boolean state)
    {
        //TODO
    }

    @Override
    public void setRTS(boolean state)
    {
        //TODO
    }

    @Override
    public void setDTR(boolean state)
    {
        //TODO
    }

    @Override
    public void getCTS(@Nullable UsbCTSCallback ctsCallback)
    {
        //TODO
    }

    @Override
    public void getDSR(@Nullable UsbDSRCallback dsrCallback)
    {
        //TODO
    }

    @Override
    public void getBreak(@Nullable UsbBreakCallback breakCallback)
    {
        //TODO
    }

    @Override
    public void getFrame(@Nullable UsbFrameCallback frameCallback)
    {
        //TODO
    }

    @Override
    public void getOverrun(@Nullable UsbOverrunCallback overrunCallback)
    {
        //TODO
    }

    @Override
    public void getParity(@Nullable UsbParityCallback parityCallback)
    {
        //TODO
    }

    private boolean openPL2303()
    {
        if(connection.claimInterface(mInterface, true))
        {
            Logger.debug(CLASS_NAME, "Interface succesfully claimed");
        }else
        {
            Logger.debug(CLASS_NAME, "Interface could not be claimed");
            return false;
        }

        // Assign endpoints
        int numberEndpoints = mInterface.getEndpointCount();
        for(int i=0;i<=numberEndpoints-1;i++)
        {
            UsbEndpoint endpoint = mInterface.getEndpoint(i);
            if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                    && endpoint.getDirection() == UsbConstants.USB_DIR_IN)
                inEndpoint = endpoint;
            else if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                    && endpoint.getDirection() == UsbConstants.USB_DIR_OUT)
                outEndpoint = endpoint;
        }

        //Default Setup
        byte[] buf = new byte[1];
        //Specific vendor stuff that I barely understand but It is on linux drivers, So I trust :)
        if(setControlCommand(PL2303_REQTYPE_DEVICE2HOST_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_HOST2DEVICE_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x0404, 0, null) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_DEVICE2HOST_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_DEVICE2HOST_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x8383, 0, buf) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_DEVICE2HOST_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_HOST2DEVICE_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x0404, 1, null) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_DEVICE2HOST_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_DEVICE2HOST_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x8383, 0, buf) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_HOST2DEVICE_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x0000, 1, null) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_HOST2DEVICE_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x0001, 0, null) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_HOST2DEVICE_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x0002, 0x0044, null) < 0)
            return false;
        // End of specific vendor stuff
        if(setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_CONTROL_REQUEST, 0x0003, 0,null) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0, defaultSetLine) < 0)
            return false;
        if(setControlCommand(PL2303_REQTYPE_HOST2DEVICE_VENDOR, PL2303_VENDOR_WRITE_REQUEST, 0x0505, 0x1311, null) < 0)
            return false;

        return true;
    }

    private int setControlCommand(int reqType , int request, int value, int index, byte[] data)
    {
        int dataLength = 0;
        if(data != null)
            dataLength = data.length;
        int response = connection.controlTransfer(reqType, request, value, index, data, dataLength, USB_TIMEOUT);
        Logger.debug(CLASS_NAME,"Control Transfer Response: " + String.valueOf(response));
        return response;
    }
}
