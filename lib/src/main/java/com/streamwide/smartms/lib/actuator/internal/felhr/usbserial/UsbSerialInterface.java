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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interface to handle a serial port
 * @author felhr (felhr85@gmail.com)
 *
 */
public interface UsbSerialInterface
{
    // Common values
    int DATA_BITS_5 = 5;
    int DATA_BITS_6 = 6;
    int DATA_BITS_7 = 7;
    int DATA_BITS_8 = 8;

    int STOP_BITS_1 = 1;
    int STOP_BITS_15 = 3;
    int STOP_BITS_2 = 2;

    int PARITY_NONE = 0;
    int PARITY_ODD = 1;
    int PARITY_EVEN = 2;
    int PARITY_MARK = 3;
    int PARITY_SPACE = 4;

    int FLOW_CONTROL_OFF = 0;
    int FLOW_CONTROL_RTS_CTS= 1;
    int FLOW_CONTROL_DSR_DTR = 2;
    int FLOW_CONTROL_XON_XOFF = 3;

    // Common Usb Serial Operations (I/O Asynchronous)
    boolean open();
    void write(@NonNull byte[] buffer);
    int read(@Nullable UsbReadCallback mCallback);
    void close();

    // Common Usb Serial Operations (I/O Synchronous)
    boolean syncOpen();
    int syncWrite(@Nullable byte[] buffer, int timeout);
    int syncRead(@Nullable byte[] buffer, int timeout);
    int syncWrite(@Nullable byte[] buffer, int offset, int length, int timeout);
    int syncRead(@Nullable byte[] buffer, int offset, int length, int timeout);
    void syncClose();

    // Serial port configuration
    void setBaudRate(int baudRate);
    void setDataBits(int dataBits);
    void setStopBits(int stopBits);
    void setParity(int parity);
    void setFlowControl(int flowControl);
    void setBreak(boolean state);

    // Flow control commands and interface callback
    void setRTS(boolean state);
    void setDTR(boolean state);
    void getCTS(@NonNull UsbCTSCallback ctsCallback);
    void getDSR(@NonNull UsbDSRCallback dsrCallback);

    // Status methods
    void getBreak(@NonNull UsbBreakCallback breakCallback);
    void getFrame(@NonNull UsbFrameCallback frameCallback);
    void getOverrun(@NonNull UsbOverrunCallback overrunCallback);
    void getParity(@NonNull UsbParityCallback parityCallback);

    interface UsbCTSCallback
    {
        void onCTSChanged(boolean state);
    }

    interface UsbDSRCallback
    {
        void onDSRChanged(boolean state);
    }

    // Error signals callbacks
    interface UsbBreakCallback
    {
        void onBreakInterrupt();
    }

    interface UsbFrameCallback
    {
        void onFramingError();
    }

    interface  UsbOverrunCallback
    {
        void onOverrunError();
    }

    interface UsbParityCallback
    {
        void onParityError();
    }

    // Usb Read Callback
    interface UsbReadCallback
    {
        void onReceivedData(@NonNull byte[] data);
    }

}
