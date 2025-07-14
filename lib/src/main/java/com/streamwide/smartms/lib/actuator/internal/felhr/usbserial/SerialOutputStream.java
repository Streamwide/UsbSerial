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

import java.io.OutputStream;

public class SerialOutputStream extends OutputStream
{
    private int timeout = 0;

    protected final UsbSerialInterface device;

    public SerialOutputStream(@NonNull UsbSerialInterface device)
    {
        this.device = device;
    }

    @Override
    public void write(int b)
    {
        device.syncWrite(new byte[] { (byte)b }, timeout);
    }

    @Override
    public void write(@NonNull byte[] b)
    {
        device.syncWrite(b, timeout);
    }

    @Override
    public void write(@NonNull byte b[], int off, int len)
    {
        if(off < 0 ){
            throw new IndexOutOfBoundsException("Offset must be >= 0");
        }

        if(len < 0){
            throw new IndexOutOfBoundsException("Length must positive");
        }

        if(off + len > b.length) {
            throw new IndexOutOfBoundsException("off + length greater than buffer length");
        }

        if (off == 0 && len == b.length) {
            write(b);
            return;
        }

        device.syncWrite(b, off, len, timeout);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
