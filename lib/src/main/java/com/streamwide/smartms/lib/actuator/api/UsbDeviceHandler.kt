/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Fri, 24 Feb 2023 15:04:43 +0100
 * @copyright  Copyright (c) 2023 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2023 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Fri, 24 Feb 2023 14:16:31 +0100
 */

package com.streamwide.smartms.lib.actuator.api

import com.streamwide.smartms.lib.actuator.internal.felhr.usbserial.UsbSerialDevice
import com.streamwide.smartms.lib.actuator.logger.Logger

class UsbDeviceHandler internal constructor(
    serialDevice: UsbSerialDevice, vendorId : Int) {

    var usbSerialDevice: UsbSerialDevice = serialDevice

    var deviceVendorId: Int = vendorId

    var usbActionReceiver: IDataReceiver? = null

    fun handleDataRead(data: String) {
        Logger.info(
            CLASS_NAME,
            "handleDataRead | handle message received from Actuator - received data : $data"
        )
        usbActionReceiver?.onDataReceived(data)
    }


    fun writeData(data: String) {
        usbSerialDevice.write(data.toByteArray())
    }

    interface IDataReceiver {

        fun onDataReceived(data: String)
    }

    companion object {
        const val CLASS_NAME = "UsbDeviceHandler"
    }
}