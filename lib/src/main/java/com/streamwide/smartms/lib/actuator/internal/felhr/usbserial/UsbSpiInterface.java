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

public interface UsbSpiInterface
{
    // Clock dividers;
    int DIVIDER_2 = 2;
    int DIVIDER_4 = 4;
    int DIVIDER_8 = 8;
    int DIVIDER_16 = 16;
    int DIVIDER_32 = 32;
    int DIVIDER_64 = 64;
    int DIVIDER_128 = 128;

    // Common SPI operations
    boolean connectSPI();
    void writeMOSI(@NonNull byte[] buffer);
    void readMISO(int lengthBuffer);
    void writeRead(@NonNull byte[] buffer, int lenghtRead);
    void setClock(int clockDivider);
    void selectSlave(int nSlave);
    void setMISOCallback(@Nullable UsbMISOCallback misoCallback);
    void closeSPI();

    // Status information
    int getClockDivider();
    int getSelectedSlave();

    interface UsbMISOCallback
    {
        int onReceivedData(@NonNull byte[] data);
    }
}
