/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.platform.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Manager responsible of all dialog, notification and warning
 * 
 * @author jpascal
 */
public final class MessengerUtils
{

    private MessengerUtils()
    {
    }

    // //////////////////////////////////////////////////////////////////////
    // TOAST
    // //////////////////////////////////////////////////////////////////////
    public static void showToast(Context context, String text)
    {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static void showToast(Context context, int text)
    {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static void showLongToast(Context context, String text)
    {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static void showLongToast(Context context, int text)
    {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    // //////////////////////////////////////////////////////////////////////
    // DIALOG
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // CROUTON
    // //////////////////////////////////////////////////////////////////////
    public static void showInfoCrouton(Activity activity, String text)
    {
        Crouton.cancelAllCroutons();
        Crouton.showText(activity, text, Style.INFO);
    }

    public static void showAlertCrouton(Activity activity, String text)
    {
        Crouton.cancelAllCroutons();
        Crouton.showText(activity, text, Style.ALERT);
    }

    public static void showInfoCrouton(Activity activity, int text)
    {
        Crouton.cancelAllCroutons();
        Crouton.showText(activity, text, Style.INFO);
    }

    public static void showAlertCrouton(Activity activity, int text)
    {
        Crouton.cancelAllCroutons();
        Crouton.showText(activity, text, Style.ALERT);
    }
}
