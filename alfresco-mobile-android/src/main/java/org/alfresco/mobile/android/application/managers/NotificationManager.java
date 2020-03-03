/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.managers;

import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class NotificationManager extends AlfrescoNotificationManager
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static NotificationManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new NotificationManager(context);
            }

            return (NotificationManager) mInstance;
        }
    }

    protected NotificationManager(Context context)
    {
        super(context);
    }

    // //////////////////////////////////////////////////////////////////////
    // CROUTON
    // //////////////////////////////////////////////////////////////////////
    public void showInfoCrouton(FragmentActivity activity, String text)
    {
        // Snackbar.make(activity.findViewById(R.id.left_pane_body),
        // Html.fromHtml(text), Snackbar.LENGTH_SHORT).show();
        Crouton.cancelAllCroutons();
        Crouton.showText(activity, Html.fromHtml(text), Style.INFO);
    }

    public void showAlertCrouton(FragmentActivity activity, String text)
    {
        Crouton.cancelAllCroutons();
        Crouton.showText(activity, Html.fromHtml(text), Style.ALERT);
    }

    public void showInfoCrouton(FragmentActivity activity, int text)
    {
        // Snackbar.make(activity.findViewById(R.id.left_pane_body), text,
        // Snackbar.LENGTH_SHORT).show();
        Crouton.cancelAllCroutons();
        Crouton.showText(activity, text, Style.INFO);
    }

    public void showAlertCrouton(FragmentActivity activity, int text)
    {
        Crouton.cancelAllCroutons();
        Crouton.showText(activity, text, Style.ALERT);
    }
}
