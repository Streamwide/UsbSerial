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

public class CH34xIds
{
    private CH34xIds()
    {

    }

    private static final long[] ch34xDevices = Helpers.createTable(
            Helpers.createDevice(0x4348, 0x5523),
            Helpers.createDevice(0x1a86, 0x7523),
            Helpers.createDevice(0x1a86, 0x5523),
            Helpers.createDevice(0x1a86, 0x0445)
    );

    public static boolean isDeviceSupported(int vendorId, int productId)
    {
        return Helpers.exists(ch34xDevices, vendorId, productId);
    }
}
