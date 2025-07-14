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

package com.streamwide.smartms.lib.actuator.api;

/**
 * an interface to all action received from the accessory
 */
public interface IUSBActionReceiver {

    /**
     * called when user accepted a call from the accessory
     */
    void onCallAccepted();

    /**
     * called when hangs up a call
     */
    void onCallHangUp();

    /**
     * called when user presses PTT1 or PTT2 button, the second value indicated the current active
     * channel in the accessory
     *
     * @param pttButtonNumber <strong>Integer:</strong>
     *                        The PTT button number, value could be 1 for PTT1 or 2 for PTT2.
     * @param channelNumber <strong>Integer:</strong>
     *                      the channel number value could 1 to 5 .
     */
    void onPTTDown(int pttButtonNumber,int channelNumber);

    /**
     * called when user releases PTT1 or PTT2 button.
     *
     * @param pttButtonNumber <strong>Integer:</strong>
     *                        The PTT button number, value could be 1 for PTT1 or 2 for PTT2.
     */
    void onPTTUp(int pttButtonNumber);

    /**
     * called when user presses the Emergency button.
     */
    void onStartEmergency();

    /**
     * called when the uses presses the stop emergency button
     */
    void onEmergencyStopped();

    /**
     * called when user presses on the video streaming button to start a video stream to dispatcher
     */
    void onStartVideoStream();

    /**
     * called when user wants to stop an ongoing video streaming session
     */
    void onStopVideoStream();


    /**
     * called when pressing on hold button, to put all channel sessions on hold.
     */
    void onSessionsOnHold();

    /**
     * called when user presses the resume button, to resume all channel sessions
     */
    void onResumeSessions();

}
