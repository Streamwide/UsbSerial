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

public class CDCSerialDevice extends UsbSerialDevice
{
    private static final String CLASS_ID = CDCSerialDevice.class.getSimpleName();
    private static final String CLASS_NAME = "CDCSerialDevice";

    private static final int CDC_REQTYPE_HOST2DEVICE = 0x21;
    private static final int CDC_REQTYPE_DEVICE2HOST = 0xA1;

    private static final int CDC_SET_LINE_CODING = 0x20;
    private static final int CDC_GET_LINE_CODING = 0x21;
    private static final int CDC_SET_CONTROL_LINE_STATE = 0x22;

    private static final int CDC_SET_CONTROL_LINE_STATE_RTS = 0x2;
    private static final int CDC_SET_CONTROL_LINE_STATE_DTR = 0x1;


    /***
     *  Default Serial Configuration
     *  Baud rate: 115200
     *  Data bits: 8
     *  Stop bits: 1
     *  Parity: None
     *  Flow Control: Off
     */
    private static final byte[] CDC_DEFAULT_LINE_CODING = new byte[] {
            (byte) 0x00, // Offset 0:4 dwDTERate
            (byte) 0xC2,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x00, // Offset 5 bCharFormat (1 Stop bit)
            (byte) 0x00, // bParityType (None)
            (byte) 0x08  // bDataBits (8)
    };

    private static final int CDC_CONTROL_LINE_ON = 0x0003;
    private static final int CDC_CONTROL_LINE_OFF = 0x0000;

    private final UsbInterface mInterface;
    private final int mControlInterfaceID;
    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;

    private int initialBaudRate = 0;

    private int controlLineState = CDC_CONTROL_LINE_ON;

    public CDCSerialDevice(@NonNull UsbDevice device, @Nullable UsbDeviceConnection connection, int iface)
    {
        super(device, connection);
        final int ifaceIdx = iface >= 0 ? iface : findFirstCDC(device);
        mInterface = device.getInterface(ifaceIdx);
        mControlInterfaceID = findControlInterface(device, ifaceIdx);
    }

    @Override
    public void setInitialBaudRate(int initialBaudRate) {
        this.initialBaudRate = initialBaudRate;
    }

    @Override
    public int getInitialBaudRate() {
        return initialBaudRate;
    }

    @Override
    public boolean open()
    {
        boolean ret = openCDC();

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
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, CDC_CONTROL_LINE_OFF, null);
        killWorkingThread();
        killWriteThread();
        connection.releaseInterface(mInterface);
        connection.close();
        isOpen = false;
    }

    @Override
    public boolean syncOpen()
    {
        boolean ret = openCDC();
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
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, CDC_CONTROL_LINE_OFF, null);
        connection.releaseInterface(mInterface);
        connection.close();
        isOpen = false;
    }

    @Override
    public void setBaudRate(int baudRate)
    {
        byte[] data = getLineCoding();

        data[0] = (byte) (baudRate & 0xff);
        data[1] = (byte) (baudRate >> 8 & 0xff);
        data[2] = (byte) (baudRate >> 16 & 0xff);
        data[3] = (byte) (baudRate >> 24 & 0xff);

        setControlCommand(CDC_SET_LINE_CODING, 0, data);
    }

    @Override
    public void setDataBits(int dataBits)
    {
        byte[] data = getLineCoding();
        switch(dataBits)
        {
            case DATA_BITS_5:
                data[6] = 0x05;
                break;
            case DATA_BITS_6:
                data[6] = 0x06;
                break;
            case DATA_BITS_7:
                data[6] = 0x07;
                break;
            case DATA_BITS_8:
                data[6] = 0x08;
                break;
            default:
                return;
        }

        setControlCommand(CDC_SET_LINE_CODING, 0, data);

    }

    @Override
    public void setStopBits(int stopBits)
    {
        byte[] data = getLineCoding();
        switch(stopBits)
        {
            case STOP_BITS_1:
                data[4] = 0x00;
                break;
            case STOP_BITS_15:
                data[4] = 0x01;
                break;
            case STOP_BITS_2:
                data[4] = 0x02;
                break;
            default:
                return;
        }

        setControlCommand(CDC_SET_LINE_CODING, 0, data);


    }

    @Override
    public void setParity(int parity)
    {
        byte[] data = getLineCoding();
        switch(parity)
        {
            case PARITY_NONE:
                data[5] = 0x00;
                break;
            case PARITY_ODD:
                data[5] = 0x01;
                break;
            case PARITY_EVEN:
                data[5] = 0x02;
                break;
            case PARITY_MARK:
                data[5] = 0x03;
                break;
            case PARITY_SPACE:
                data[5] = 0x04;
                break;
            default:
                return;
        }

        setControlCommand(CDC_SET_LINE_CODING, 0, data);

    }

    @Override
    public void setFlowControl(int flowControl)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBreak(boolean state)
    {
        //TODO
    }

    @Override
    public void setRTS(boolean state)
    {
        if (state)
            controlLineState |= CDC_SET_CONTROL_LINE_STATE_RTS;
        else
            controlLineState &= ~CDC_SET_CONTROL_LINE_STATE_RTS;
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, controlLineState, null);

    }

    @Override
    public void setDTR(boolean state)
    {
        if (state)
            controlLineState |= CDC_SET_CONTROL_LINE_STATE_DTR;
        else
            controlLineState &= ~CDC_SET_CONTROL_LINE_STATE_DTR;
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, controlLineState, null);
    }

    @Override
    public void getCTS(@NonNull UsbCTSCallback ctsCallback)
    {
        Logger.debug(CLASS_NAME,"getCTS");
    }

    @Override
    public void getDSR(@NonNull UsbDSRCallback dsrCallback)
    {
        Logger.debug(CLASS_NAME,"getDSR");
    }

    @Override
    public void getBreak(@NonNull UsbBreakCallback breakCallback)
    {
        Logger.debug(CLASS_NAME,"getBreak");
    }

    @Override
    public void getFrame(@NonNull UsbFrameCallback frameCallback)
    {
        Logger.debug(CLASS_NAME,"getFrame");
    }

    @Override
    public void getOverrun(@NonNull UsbOverrunCallback overrunCallback)
    {
        Logger.debug(CLASS_NAME,"getOverrun");
    }

    @Override
    public void getParity(@NonNull UsbParityCallback parityCallback)
    {
        Logger.debug(CLASS_NAME,"getParity");
    }

    private boolean openCDC()
    {
        if(connection.claimInterface(mInterface, true))
        {
            Logger.debug(CLASS_NAME, "Interface succesfully claimed");
        }else
        {
            Logger.error(CLASS_NAME, "Interface could not be claimed");
            return false;
        }

        // Assign endpoints
        int numberEndpoints = mInterface.getEndpointCount();
        for(int i=0;i<=numberEndpoints-1;i++)
        {
            UsbEndpoint endpoint = mInterface.getEndpoint(i);
            if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                    && endpoint.getDirection() == UsbConstants.USB_DIR_IN)
            {
                inEndpoint = endpoint;
            }else if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                    && endpoint.getDirection() == UsbConstants.USB_DIR_OUT)
            {
                outEndpoint = endpoint;
            }
        }

        if(outEndpoint == null || inEndpoint == null)
        {
            Logger.error(CLASS_NAME, "Interface does not have an IN or OUT interface");
            return false;
        }

        // Default Setup
        setControlCommand(CDC_SET_LINE_CODING, 0, getInitialLineCoding());
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, CDC_CONTROL_LINE_ON, null);

        return true;
    }

    @NonNull
    protected byte[] getInitialLineCoding() {
        byte[] lineCoding;

        int initialBaudRate = getInitialBaudRate();

        if(initialBaudRate > 0) {
            lineCoding = CDC_DEFAULT_LINE_CODING.clone();
            for (int i = 0; i < 4; i++) {
                lineCoding[i] = (byte) (initialBaudRate >> i*8 & 0xFF);
            }
        } else {
            lineCoding = CDC_DEFAULT_LINE_CODING;
        }

        return lineCoding;
    }

    private int setControlCommand(int request, int value, byte[] data)
    {
        int dataLength = 0;
        if(data != null)
        {
            dataLength = data.length;
        }
        int response = connection.controlTransfer(CDC_REQTYPE_HOST2DEVICE, request, value, mControlInterfaceID, data, dataLength, USB_TIMEOUT);
        Logger.debug(CLASS_NAME,"Control Transfer Response: " + String.valueOf(response));
        return response;
    }

    private byte[] getLineCoding()
    {
        byte[] data = new byte[7];
        int response = connection.controlTransfer(CDC_REQTYPE_DEVICE2HOST, CDC_GET_LINE_CODING, 0, mControlInterfaceID, data, data.length, USB_TIMEOUT);
        Logger.debug(CLASS_NAME,"Control Transfer Response: " + String.valueOf(response));
        return data;
    }

    private static int findFirstCDC(UsbDevice device)
    {
        int interfaceCount = device.getInterfaceCount();

        for (int iIndex = 0; iIndex < interfaceCount; ++iIndex)
        {
            if (device.getInterface(iIndex).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA)
            {
                return iIndex;
            }
        }

       Logger.debug(CLASS_NAME, "There is no CDC class interface");
        return -1;
    }

    private static int findControlInterface(UsbDevice device, int ifaceIdx)
    {
        int ifaceNumber = 0;
        int interfaceCount = device.getInterfaceCount();

        for (int iIndex = 0; iIndex < interfaceCount; ++iIndex)
        {
            if (device.getInterface(iIndex).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA)
                ifaceNumber++;

            if(iIndex == ifaceIdx)
                break;
        }

        int controlIFaceNumber = 0;
        for (int iIndex = 0; iIndex < interfaceCount; ++iIndex)
        {
            if (device.getInterface(iIndex).getInterfaceClass() == UsbConstants.USB_CLASS_COMM)
                controlIFaceNumber++;

            if(ifaceNumber == controlIFaceNumber)
                return device.getInterface(iIndex).getId();
        }

        return 0;
    }

}
