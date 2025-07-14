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

public class XdcVcpIds
{
	/*
	 * Werner Wolfrum (w.wolfrum@wolfrum-elektronik.de)
	 */

    /* Different products and vendors of XdcVcp family
    */
    private static final long[] xdcvcpDevices = Helpers.createTable(
            Helpers.createDevice(0x264D, 0x0232), // VCP (Virtual Com Port)
            Helpers.createDevice(0x264D, 0x0120),  // USI (Universal Sensor Interface)
            Helpers.createDevice(0x0483, 0x5740) //CC3D (STM)
    );

    public static boolean isDeviceSupported(int vendorId, int productId)
    {
        return Helpers.exists(xdcvcpDevices, vendorId, productId);
    }
}
