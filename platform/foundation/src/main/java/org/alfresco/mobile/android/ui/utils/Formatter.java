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
package org.alfresco.mobile.android.ui.utils;

import java.util.Date;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.foundation.R;

import android.content.Context;
import android.content.res.Resources;

public final class Formatter
{

    private static final int MINUTE_IN_SECONDS = 60;

    private static final int HOUR_IN_SECONDS = 3600;

    private static final int HOUR_IN_MINUTES = 60;

    private static final int DAY_IN_SECONDS = 86400;

    private static final int DAY_IN_HOURS = 24;

    private static final int YEAR_IN_SECONDS = 31536000;

    private static final int YEAR_IN_DAYS = 365;

    private Formatter()
    {
    }

    /**
     * Format a date into a relative human readable date.
     * 
     * @param c
     * @param date
     * @return
     */
    public static String formatToRelativeDate(Context c, Date date)
    {
        if (date == null) { return null; }

        Resources res = c.getResources();

        Date todayDate = new Date();
        float ti = (todayDate.getTime() - date.getTime()) / 1000;

        if (ti < 1)
        {
            return res.getString(R.string.relative_date_just_now);
        }
        else if (ti < MINUTE_IN_SECONDS)
        {
            return res.getString(R.string.relative_date_less_than_a_minute_ago);
        }
        else if (ti < HOUR_IN_SECONDS)
        {
            int diff = Math.round(ti / MINUTE_IN_SECONDS);
            return String.format(res.getQuantityString(R.plurals.relative_date_minutes_ago, diff), diff);
        }
        else if (ti < DAY_IN_SECONDS)
        {
            int diff = Math.round(ti / MINUTE_IN_SECONDS / HOUR_IN_MINUTES);
            return String.format(res.getQuantityString(R.plurals.relative_date_hours_ago, diff), diff);
        }
        else if (ti < YEAR_IN_SECONDS)
        {
            int diff = Math.round(ti / MINUTE_IN_SECONDS / HOUR_IN_MINUTES / DAY_IN_HOURS);
            return String.format(res.getQuantityString(R.plurals.relative_date_days_ago, diff), diff);
        }
        else
        {
            int diff = Math.round(ti / MINUTE_IN_SECONDS / HOUR_IN_MINUTES / DAY_IN_HOURS / YEAR_IN_DAYS);
            return String.format(res.getQuantityString(R.plurals.relative_date_years_ago, diff), diff);
        }
    }

    /**
     * Format a file size in human readable text.
     * 
     * @param context
     * @param sizeInByte
     * @return
     */
    public static String formatFileSize(Context context, long sizeInByte)
    {
        return android.text.format.Formatter.formatShortFileSize(context, sizeInByte);
    }

    /**
     * Create default bottom text for a node.
     * 
     * @param context
     * @param node
     * @return
     */
    public static String createContentBottomText(Context context, Node node)
    {
        return createContentBottomText(context, node, false);
    }

    public static String createContentBottomText(Context context, Node node, boolean extended)
    {
        StringBuilder s = new StringBuilder();
        if (node.getModifiedAt() != null)
        {
            s.append(formatToRelativeDate(context, node.getModifiedAt().getTime()));
            if (node.isDocument())
            {
                Document doc = (Document) node;
                s.append(" - ");
                s.append(formatFileSize(context, doc.getContentStreamLength()));

                if (extended)
                {
                    s.append(" - V:");
                    if ("0.0".equals(doc.getVersionLabel()))
                    {
                        s.append("1.0");
                    }
                    else
                    {
                        s.append(doc.getVersionLabel());
                    }
                }
            }
        }
        return s.toString();
    }
}
