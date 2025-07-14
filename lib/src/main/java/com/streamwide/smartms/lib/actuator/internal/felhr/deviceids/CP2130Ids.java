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

import static com.streamwide.smartms.lib.actuator.internal.felhr.deviceids.Helpers.createTable;
import static com.streamwide.smartms.lib.actuator.internal.felhr.deviceids.Helpers.createDevice;

public class CP2130Ids
{
    private static final long[] cp2130Devices = createTable(
            createDevice(0x10C4, 0x87a0)
    );

    public static boolean isDeviceSupported(int vendorId, int productId)
    {
        return Helpers.exists(cp2130Devices, vendorId, productId);
    }
}
