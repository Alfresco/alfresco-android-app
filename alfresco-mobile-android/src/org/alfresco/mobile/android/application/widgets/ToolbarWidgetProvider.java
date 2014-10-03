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
package org.alfresco.mobile.android.application.widgets;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.intent.PublicIntentAPIUtils;
import org.alfresco.mobile.android.application.managers.ActionUtils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

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
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return pendingIntent;
    }

    private PendingIntent createSpeechTextEditorIntent(Context context)
    {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, PublicIntentAPIUtils.speechToTextIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent createTextEditorIntent(Context context)
    {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, PublicIntentAPIUtils.createTextIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent createPhotoCaptureIntent(Context context)
    {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, PublicIntentAPIUtils.captureImageIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
