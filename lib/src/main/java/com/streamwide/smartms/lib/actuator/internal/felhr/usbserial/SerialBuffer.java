/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 2 mars 2021 10:23:12 +0100
 * @copyright  Copyright (c) 2021 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2021 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn lun., 1 mars 2021 14:04:19 +0100
 */

package com.streamwide.smartms.lib.actuator.internal.felhr.usbserial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.actuator.internal.felhr.utils.HexData;
import com.streamwide.smartms.lib.actuator.logger.Logger;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import okio.Buffer;

public class SerialBuffer
{
    static final String CLASS_NAME = "SerialBuffer";
    static final int DEFAULT_READ_BUFFER_SIZE = 16 * 1024;
    static final int MAX_BULK_BUFFER = 16 * 1024;
    private ByteBuffer readBuffer;

    private final SynchronizedBuffer writeBuffer;
    private byte[] readBufferCompatible; // Read buffer for android < 4.2

    public SerialBuffer(boolean version)
    {
        writeBuffer = new SynchronizedBuffer();
        if(version)
        {
            readBuffer = ByteBuffer.allocate(DEFAULT_READ_BUFFER_SIZE);

        }else
        {
            readBufferCompatible = new byte[DEFAULT_READ_BUFFER_SIZE];
        }
    }


    @Nullable
    public ByteBuffer getReadBuffer()
    {
        synchronized(this)
        {
            return readBuffer;
        }
    }


    @NonNull
    public byte[] getDataReceived()
    {
        synchronized(this)
        {
            byte[] dst = new byte[readBuffer.position()];
            readBuffer.position(0);
            readBuffer.get(dst, 0, dst.length);
            Logger.info(CLASS_NAME,"Data obtained from Read buffer: " + new String(dst) +
                    " Raw data from Read buffer:" + HexData.hexToString(dst) +
                    " Number of bytes obtained from Read buffer: " + dst.length);
            return dst;
        }
    }

    public void clearReadBuffer()
    {
        synchronized(this)
        {
            readBuffer.clear();
        }
    }

    @NonNull
    public byte[] getWriteBuffer()
    {
        return writeBuffer.get();
    }

    public void putWriteBuffer(@NonNull byte[]data)
    {
        writeBuffer.put(data);
    }


    @Nullable
    public byte[] getBufferCompatible()
    {
        return readBufferCompatible != null ? readBufferCompatible.clone() : null;
    }

    @NonNull
    public byte[] getDataReceivedCompatible(int numberBytes)
    {
        return Arrays.copyOfRange(readBufferCompatible, 0, numberBytes);
    }

    private class SynchronizedBuffer
    {
        private final Buffer buffer;

        SynchronizedBuffer()
        {
            this.buffer = new Buffer();
        }

        synchronized void put(byte[] src)
        {
            if(src == null || src.length == 0) return;

            Logger.info(CLASS_NAME,"Data obtained from Read buffer:: " + new String(src) +
                    " Raw data from Read buffer: " + HexData.hexToString(src) +
                    " Number of bytes obtained from Read buffer: " + src.length);

            buffer.write(src);
            notifyAll();
        }

        synchronized byte[] get()
        {
            if(buffer.size() ==  0)
            {
                try
                {
                    wait();
                } catch (InterruptedException e)
                {
                   Logger.error(CLASS_NAME, " InterruptedException = "+e);
                    Thread.currentThread().interrupt();
                }
            }
            byte[] dst;
            if(buffer.size() <= MAX_BULK_BUFFER){
                dst = buffer.readByteArray();
            }else{
                try {
                    dst = buffer.readByteArray(MAX_BULK_BUFFER);
                } catch (EOFException e) {
                    Logger.error(CLASS_NAME, " InterruptedException = "+e);
                    return new byte[0];
                }
            }

            Logger.info(CLASS_NAME,"Data obtained from write buffer:: " + new String(dst) +
                    "Raw data from write buffer:" + HexData.hexToString(dst) +
                    "Number of bytes obtained from write buffer: " + dst.length);

            return dst;
        }
    }

}
