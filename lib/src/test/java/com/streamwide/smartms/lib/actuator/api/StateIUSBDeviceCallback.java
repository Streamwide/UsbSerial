/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 26 juil. 2022 17:10:46 +0200
 * @copyright  Copyright (c) 2022 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2022 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn mar., 26 juil. 2022 17:10:46 +0200
 */

package com.streamwide.smartms.lib.actuator.api;

import android.hardware.usb.UsbDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StateIUSBDeviceCallback implements IUSBDeviceCallback {
    public List<String> errors = new ArrayList<>();
    public boolean onConnect;
    public boolean onDisconnect;
    public UsbDevice onPlugged;
    public boolean onUnPlugged;

    @Override
    public void onError(@NonNull String error) {
        this.errors.add(error);
    }

    @Override
    public void onConnect() {
        onConnect = true;
    }

    @Override
    public void onDisconnect() {
        onDisconnect = true;

    }

    @Override
    public void onPlugged(@Nullable UsbDevice usbDevice) {
        onPlugged = usbDevice;
    }

    @Override
    public void onUnPlugged() {
        onUnPlugged = true;
    }
}
