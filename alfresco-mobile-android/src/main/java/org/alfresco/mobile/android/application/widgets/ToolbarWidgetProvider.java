/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.widgets;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.intent.PublicIntentAPIUtils;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Responsible to manage and display an Application Widget on Device Android
 * Screen.
 * <p>
 * It contains 3/4 actions
 * <ul>
 * <li>Shortcut to HomeScreen</li>
 * <li>Shortcut to create a text file</li>
 * <li>Shortcut to speech to text</li>
 * <li>Shortcut to take a photo</li>
 * </ul>
 * 
 * @author Jean Marie Pascal
 */
public class ToolbarWidgetProvider extends AppWidgetProvider
{

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this
        // provider
        for (int i = 0; i < N; i++)
        {
            int appWidgetId = appWidgetIds[i];

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_toolbar);
            views.setOnClickPendingIntent(R.id.quickaction_home, createAlfrescoIntent(context));

            // Speech2Text available ?
            if (!ActionUtils.hasSpeechToText(context))
            {
                views.setViewVisibility(R.id.quickaction_voicetotext, View.GONE);
            }
            else
            {
                views.setOnClickPendingIntent(R.id.quickaction_voicetotext, createSpeechTextEditorIntent(context));
            }

            // Camera available ?
            if (!ActionUtils.hasCameraAvailable(context))
            {
                views.setViewVisibility(R.id.quickaction_photo, View.GONE);
            }
            else
            {
                views.setOnClickPendingIntent(R.id.quickaction_photo, createPhotoCaptureIntent(context));
            }

            views.setOnClickPendingIntent(R.id.quickaction_text, createTextEditorIntent(context));

            // Tell the AppWidgetManager to perform an update on the current app
            // widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private PendingIntent createAlfrescoIntent(Context context)
    {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private PendingIntent createSpeechTextEditorIntent(Context context)
    {
        // Analytics
        AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_WIDGET, AnalyticsManager.ACTION_TOOLBAR,
                AnalyticsManager.LABEL_SPEECH_2_TEXT, 1, false);

        return PendingIntent.getActivity(context, 1, PublicIntentAPIUtils.speechToTextIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createTextEditorIntent(Context context)
    {
        // Analytics
        AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_WIDGET, AnalyticsManager.ACTION_TOOLBAR,
                AnalyticsManager.LABEL_CREATE_TEXT, 1, false);

        return PendingIntent.getActivity(context, 0, PublicIntentAPIUtils.createTextIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createPhotoCaptureIntent(Context context)
    {
        // Analytics
        AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_WIDGET, AnalyticsManager.ACTION_TOOLBAR,
                AnalyticsManager.LABEL_TAKE_PHOTO, 1, false);

        return PendingIntent.getActivity(context, 2, PublicIntentAPIUtils.captureImageIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
