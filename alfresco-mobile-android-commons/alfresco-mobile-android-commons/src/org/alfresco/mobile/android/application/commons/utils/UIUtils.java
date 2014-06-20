/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.commons.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Display;
import android.view.View;

/**
 * Utility around UI Management.
 * 
 * @author Jean Marie Pascal
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class UIUtils
{

    /**
     * Set the background view with the drawable associated.
     * 
     * @param v
     * @param background
     */
    @SuppressWarnings("deprecation")
    public static void setBackground(View v, Drawable background)
    {
        if (AndroidVersion.isJBOrAbove())
        {
            v.setBackground(background);
        }
        else
        {
            v.setBackgroundDrawable(background);
        }
    }

    public static int[] getScreenDimension(Activity activity)
    {
        int width = 0;
        int height = 0;

        Display display = activity.getWindowManager().getDefaultDisplay();
        if (AndroidVersion.isHCMR2OrAbove())
        {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        }
        else
        {
            width = display.getWidth(); // deprecated
            height = display.getHeight(); // deprecated
        }

        return new int[] { width, height };
    }

    private static final Pattern NAME_PATTERN = Pattern
            .compile("(.*[\"\\*\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)");

    public static boolean hasInvalidName(String name)
    {
        Matcher matcher = NAME_PATTERN.matcher(name);
        return matcher.matches();
    }
}
