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

package com.streamwide.smartms.lib.actuator.internal.felhr.deviceids;

import java.util.Arrays;

class Helpers {

    /**
     * Create a device id, since they are 4 bytes each, we can pack the pair in an long.
     */
    static long createDevice(int vendorId, int productId) {
        return ((long) vendorId) << 32 | (productId & 0xFFFF_FFFFL);
    }

    /**
     * Creates a sorted table.
     * This way, we can use binarySearch to find whether the entry exists.
     */
    static long[] createTable(long ... entries) {
        Arrays.sort(entries);
        return entries;
    }

    static boolean exists(long[] devices, int vendorId, int productId) {
        return Arrays.binarySearch(devices, createDevice(vendorId, productId)) >= 0;
    }
}
