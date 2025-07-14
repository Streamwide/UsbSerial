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

package com.streamwide.smartms.lib.actuator.internal.felhr.utils;

import android.annotation.SuppressLint;
import android.hardware.usb.UsbRequest;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;


public class SafeUsbRequest extends UsbRequest
{
    static final String usbRqBufferField = "mBuffer";
    static final String usbRqLengthField = "mLength";

    @SuppressLint("DiscouragedPrivateApi")
    @Override
    public boolean queue(@Nullable ByteBuffer buffer)
    {
        Field usbRequestBuffer;
        Field usbRequestLength;
        try
        {
            usbRequestBuffer = UsbRequest.class.getDeclaredField(usbRqBufferField);
            usbRequestLength = UsbRequest.class.getDeclaredField(usbRqLengthField);
            usbRequestBuffer.setAccessible(true);
            usbRequestLength.setAccessible(true);
            usbRequestBuffer.set(this, buffer);
            byte[] dst = new byte[buffer.position()];
            usbRequestLength.set(this, dst.length);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return super.queue(buffer);
    }
}
