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

package com.streamwide.smartms.lib.actuator.api;

import android.hardware.usb.UsbDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IUSBDeviceCallback {
    /**
     *Triggered when an error is handled
     * @param error : {@link String} the error description
     */
    void onError(@NonNull String error);

    /**
     * Triggered when connection is established
     */
    void onConnect();

    /**
     * Triggered when usb device is disconnected
     */
    void onDisconnect();

    /**
     * Tirggered when an usb device is plugged
     * @param usbDevice {@link UsbDevice} : the plugged device
     */
    void onPlugged(@Nullable UsbDevice usbDevice);

    /**
     * Triggered when usb device is unplugged
     */
    void onUnPlugged();
}
