/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.widget;

import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;

public class ActivityWidget extends AppWidgetProvider
{

    static void forceUpdate(Context context)
    {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        UpdateWidget(context, views);
        // Push update for this widget to the home screen
        ComponentName thisWidget = new ComponentName(context, ActivityWidget.class);
        manager.updateAppWidget(thisWidget, views);
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        final Handler h = new Handler();

        new Thread()
        {
            @Override
            public void run()
            {
                // Perform this loop procedure for each App Widget that belongs
                // to this provider
                for (int i = 0; i < appWidgetIds.length; i++)
                {
                    final int appWidgetId = appWidgetIds[i];
                    h.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            UpdateWidget(context, views);

                            // Tell the AppWidgetManager to perform an update on
                            // the current App Widget
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    });
                }

                super.run();
            }

        }.start();

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void UpdateWidget(Context context, RemoteViews views)
    {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.Expand, pendingIntent);
        views.setOnClickPendingIntent(R.id.Summary, pendingIntent);
        views.setOnClickPendingIntent(R.id.QuoteText, pendingIntent);

        // views.setTextViewText (R.id.Summary, current.getSummary());

        boolean modernVersion = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("ModernVersion",
                false);

        Spanned content = Html.fromHtml("hello");
        views.setTextViewText(R.id.QuoteText, content);
    }
}
