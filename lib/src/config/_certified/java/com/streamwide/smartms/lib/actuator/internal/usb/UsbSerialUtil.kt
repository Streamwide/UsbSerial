/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 30 Oct 2024 11:56:05 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Wed, 30 Oct 2024 11:56:04 +0100
 */

package com.streamwide.smartms.lib.actuator.internal.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter

internal object UsbSerialUtil {

    fun registerBroadcast(
        context: Context,
        intentFilter: IntentFilter,
        usbBroadcastReceiver: BroadcastReceiver,
    ) {
        context.registerReceiver(
            usbBroadcastReceiver,
            intentFilter,
            Context.RECEIVER_EXPORTED
        )
    }
}