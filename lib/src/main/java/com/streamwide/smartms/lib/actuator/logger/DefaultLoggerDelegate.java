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

package com.streamwide.smartms.lib.actuator.logger;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.actuator.BuildConfig;

/**
 * Default logger delegate implementation which logs in LogCat with {@link Log}.
 * Log tag is set to <b>DefaultLoggerDelegate</b> for all the logs.
 */
public class DefaultLoggerDelegate implements Logger.LoggerDelegate {

    private static final String TAG = "Telo-LoggerDelegate";

    @Override
    public void error(@Nullable String tag, @NonNull String message)
    {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, tag + " - " + message);
        }
    }

    @Override
    public void error(@Nullable String tag, @NonNull String message, @Nullable Throwable exception)
    {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, tag + " - " + message, exception);
        }
    }

    @Override
    public void debug(@Nullable String tag, @NonNull String message)
    {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, tag + " - " + message);
        }
    }

    @Override
    public void info(@Nullable String tag, @NonNull String message)
    {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, tag + " - " + message);
        }
    }
}
