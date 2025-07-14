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

public class PL2303Ids
{
    private PL2303Ids()
    {

    }

    private static final long[] pl2303Devices = Helpers.createTable(
            Helpers.createDevice(0x04a5, 0x4027),
            Helpers.createDevice(0x067b, 0x2303),
            Helpers.createDevice(0x067b, 0x04bb),
            Helpers.createDevice(0x067b, 0x1234),
            Helpers.createDevice(0x067b, 0xaaa0),
            Helpers.createDevice(0x067b, 0xaaa2),
            Helpers.createDevice(0x067b, 0x0611),
            Helpers.createDevice(0x067b, 0x0612),
            Helpers.createDevice(0x067b, 0x0609),
            Helpers.createDevice(0x067b, 0x331a),
            Helpers.createDevice(0x067b, 0x0307),
            Helpers.createDevice(0x067b, 0x0463),
            Helpers.createDevice(0x0557, 0x2008),
            Helpers.createDevice(0x0547, 0x2008),
            Helpers.createDevice(0x04bb, 0x0a03),
            Helpers.createDevice(0x04bb, 0x0a0e),
            Helpers.createDevice(0x056e, 0x5003),
            Helpers.createDevice(0x056e, 0x5004),
            Helpers.createDevice(0x0eba, 0x1080),
            Helpers.createDevice(0x0eba, 0x2080),
            Helpers.createDevice(0x0df7, 0x0620),
            Helpers.createDevice(0x0584, 0xb000),
            Helpers.createDevice(0x2478, 0x2008),
            Helpers.createDevice(0x1453, 0x4026),
            Helpers.createDevice(0x0731, 0x0528),
            Helpers.createDevice(0x6189, 0x2068),
            Helpers.createDevice(0x11f7, 0x02df),
            Helpers.createDevice(0x04e8, 0x8001),
            Helpers.createDevice(0x11f5, 0x0001),
            Helpers.createDevice(0x11f5, 0x0003),
            Helpers.createDevice(0x11f5, 0x0004),
            Helpers.createDevice(0x11f5, 0x0005),
            Helpers.createDevice(0x0745, 0x0001),
            Helpers.createDevice(0x078b, 0x1234),
            Helpers.createDevice(0x10b5, 0xac70),
            Helpers.createDevice(0x079b, 0x0027),
            Helpers.createDevice(0x0413, 0x2101),
            Helpers.createDevice(0x0e55, 0x110b),
            Helpers.createDevice(0x0731, 0x2003),
            Helpers.createDevice(0x050d, 0x0257),
            Helpers.createDevice(0x058f, 0x9720),
            Helpers.createDevice(0x11f6, 0x2001),
            Helpers.createDevice(0x07aa, 0x002a),
            Helpers.createDevice(0x05ad, 0x0fba),
            Helpers.createDevice(0x5372, 0x2303),
            Helpers.createDevice(0x03f0, 0x0b39),
            Helpers.createDevice(0x03f0, 0x3139),
            Helpers.createDevice(0x03f0, 0x3239),
            Helpers.createDevice(0x03f0, 0x3524),
            Helpers.createDevice(0x04b8, 0x0521),
            Helpers.createDevice(0x04b8, 0x0522),
            Helpers.createDevice(0x054c, 0x0437),
            Helpers.createDevice(0x11ad, 0x0001),
            Helpers.createDevice(0x0b63, 0x6530),
            Helpers.createDevice(0x0b8c, 0x2303),
            Helpers.createDevice(0x110a, 0x1150),
            Helpers.createDevice(0x0557, 0x2008)
    );

    public static boolean isDeviceSupported(int vendorId, int productId)
    {
        return Helpers.exists(pl2303Devices, vendorId, productId);
    }
}
