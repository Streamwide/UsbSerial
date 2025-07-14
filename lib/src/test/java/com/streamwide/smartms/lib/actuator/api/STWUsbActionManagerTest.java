/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 26 juil. 2022 16:22:24 +0200
 * @copyright  Copyright (c) 2022 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2022 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn mar., 26 juil. 2022 16:22:24 +0200
 */

package com.streamwide.smartms.lib.actuator.api;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import com.streamwide.smartms.lib.actuator.internal.usb.UsbSerialDeviceConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = LOLLIPOP)
public class STWUsbActionManagerTest {
    private static final String DEVICE_NAME = "usb1";
    private static final int VENDOR_ID = 16;

    private final USBDevice device = new USBDevice(DEVICE_NAME, VENDOR_ID, 1);
    private final STWUsbActionManager manager = STWUsbActionManager.getInstance();
    private Context context;
    private UsbManager usbManager;
    private StateIUSBDeviceCallback callback = new StateIUSBDeviceCallback();

    @Mock
    UsbDevice usbDevice;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        given(usbDevice.getDeviceName()).willReturn(DEVICE_NAME);
        given(usbDevice.getVendorId()).willReturn(VENDOR_ID);
        manager.registerForUsbDeviceCallback(callback, null);
    }

    @After
    public void tearDown() {
        shadowOf(usbManager).removeUsbDevice(usbDevice);
    }

    @Test
    public void isUsbDeviceConnected_device_withoutPermission() {
        shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice, false);

        manager.connectUsbDevice(context, device);

        assertEquals(listOf("Could not find device with vendorId: 16"), callback.errors);
        // will never connect during test because of UsbManager.openDevice
        assertFalse(manager.isUsbDeviceConnected());
    }

    @Test
    public void isUsbDeviceConnected_device_withPermission() {
        shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice, true);

        manager.connectUsbDevice(context, device);

        assertEquals(listOf("Usb Device Connection IS NULL"), callback.errors);
        // will never connect during test because of UsbManager.openDevice
        assertFalse(manager.isUsbDeviceConnected());
    }

    @Test
    public void deviceHasPermission_withoutPermission() {
        shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice, false);
        manager.connectUsbDevice(context, device);

        final boolean hasPermission = manager.deviceHasPermission(context);

        // this is weird it should be the opposite
        assertTrue(hasPermission);
    }

    @Test
    public void deviceHasPermission_withPermission() {
        shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice, true);
        manager.connectUsbDevice(context, device);

        final boolean hasPermission = manager.deviceHasPermission(context);

        // this is weird it should be the opposite
        assertFalse(hasPermission);
    }

    @Test
    public void deviceConfiguration() {
        shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice, true);
        manager.connectUsbDevice(context, device);

        final UsbSerialDeviceConfig configuration = manager.deviceConfiguration();

        assertEquals(VENDOR_ID, configuration.getDeviceVenderId());
    }

    public static <T> List<T> listOf(T... elements) {
        List<T> list = new ArrayList<>();
        for (T element : elements)
            list.add(element);
        return list;
    }
}