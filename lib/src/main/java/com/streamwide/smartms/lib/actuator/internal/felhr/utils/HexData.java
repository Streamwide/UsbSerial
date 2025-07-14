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

package com.streamwide.smartms.lib.actuator.internal.felhr.utils;

import androidx.annotation.Nullable;

public class HexData
{
    private static final String HEXES = "0123456789ABCDEF";
    private static final String HEX_INDICATOR = "0x";
    private static final String SPACE = " ";

    private HexData()
    {

    }

    @Nullable
    public static String hexToString(@Nullable byte[] data)
    {
        if(data != null)
        {
            StringBuilder hex = new StringBuilder(2*data.length);
            for(int i=0;i<=data.length-1;i++)
            {
                byte dataAtIndex = data[i];
                hex.append(HEX_INDICATOR);
                hex.append(HEXES.charAt((dataAtIndex & 0xF0) >> 4))
                        .append(HEXES.charAt((dataAtIndex & 0x0F)));
                hex.append(SPACE);
            }
            return hex.toString();
        }else
        {
            return null;
        }
    }

}
