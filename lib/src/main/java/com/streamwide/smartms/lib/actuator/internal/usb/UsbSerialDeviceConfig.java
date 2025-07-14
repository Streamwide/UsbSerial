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

package com.streamwide.smartms.lib.actuator.internal.usb;

import com.streamwide.smartms.lib.actuator.internal.felhr.usbserial.UsbSerialInterface;

public class UsbSerialDeviceConfig {

    private int mBaudRate = 9600;

    private int mDataBits = UsbSerialInterface.DATA_BITS_8;

    private int mStopBits = UsbSerialInterface.STOP_BITS_1;

    private int mParity = UsbSerialInterface.PARITY_NONE;

    private int mFlowControl = UsbSerialInterface.FLOW_CONTROL_OFF;

    private int mDeviceVendorId = 0x1f12;



    /*

     */
    public UsbSerialDeviceConfig()
    {
        super();
    }

    public UsbSerialDeviceConfig(int deviceVendorId)
    {
      this.mDeviceVendorId = deviceVendorId;
    }


    public UsbSerialDeviceConfig(int deviceVenderId,int baudRate,int stopBits,int parity,int flowControl)
    {
        this.mDeviceVendorId =deviceVenderId;
        this.mBaudRate=baudRate;
        this.mParity=parity;
        this.mStopBits=stopBits;
        this.mFlowControl=flowControl;

    }

    public int getBaudRate() {
        return mBaudRate;
    }

    public void setBaudRate(int mBaudRate) {
        this.mBaudRate = mBaudRate;
    }

    public int getDataBits() {
        return mDataBits;
    }

    public void setDataBits(int mDataBits) {
        this.mDataBits = mDataBits;
    }

    public int getStopBits() {
        return mStopBits;
    }

    public void setStopBits(int mStopBits) {
        this.mStopBits = mStopBits;
    }

    public int getParity() {
        return mParity;
    }

    public void setParity(int mParity) {
        this.mParity = mParity;
    }

    public int getFlowControl() {
        return mFlowControl;
    }

    public void setFlowControl(int mFlowControl) {
        this.mFlowControl = mFlowControl;
    }

    public int getDeviceVenderId() {
        return mDeviceVendorId;
    }

}
