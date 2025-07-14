/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 13 Jul 2022 05:58:10 +0100
 * @copyright  Copyright (c) 2022 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2022 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Wed, 13 Jul 2022 05:58:08 +0100
 */

package com.streamwide.smartms.lib.actuator.api;

import androidx.annotation.NonNull;

import java.util.Objects;

public class USBDevice {
    String name;
    int vendorId;
    int channelNumber;

    public USBDevice(String name, int vendorId, int numberOfChannels) {
        this.name = name;
        this.vendorId = vendorId;
        this.channelNumber = numberOfChannels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getNumberOfChannels() {
        return channelNumber;
    }

    @NonNull
    @Override
    public String toString() {
        return "USBDevice{" +
                "name='" + name + '\'' +
                ", vendorId=" + vendorId +
                ", channelNumber=" + channelNumber +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, vendorId, channelNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        USBDevice usbDevice = (USBDevice) o;
        return vendorId == usbDevice.vendorId && channelNumber == usbDevice.channelNumber;
    }

}
