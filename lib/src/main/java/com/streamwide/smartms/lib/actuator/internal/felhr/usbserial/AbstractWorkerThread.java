/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 2 mars 2021 10:23:12 +0100
 * @copyright  Copyright (c) 2021 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2021 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn lun., 1 mars 2021 14:21:32 +0100
 */

package com.streamwide.smartms.lib.actuator.internal.felhr.usbserial;

abstract class AbstractWorkerThread extends Thread {
    boolean firstTime = true;
    private volatile boolean keep = true;
    private Thread workingThread;

    void stopThread() {
        keep = false;
        if (this.workingThread != null) {
            this.workingThread.interrupt();
        }
    }

    public final void run() {
        if (!this.keep) {
            return;
        }
        this.workingThread = Thread.currentThread();
        while (this.keep && (!this.workingThread.isInterrupted())) {
            doRun();
        }
    }

    abstract void doRun();
}
