/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.ui.widget;

import org.alfresco.mobile.android.application.R;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.jmpergar.awesometext.AwesomeTextHandler;

/**
 * Created by jpascal on 03/03/2015.
 */
public class TagsSpanRenderer implements AwesomeTextHandler.ViewSpanRenderer
{
    private final static int textSizeInDips = 14;

    private final static int backgroundResource = R.drawable.tags_brackground;

    private final static int textColorResource = android.R.color.black;

    @Override
    public View getView(String text, Context context)
    {
        TextView view = new TextView(context);
        view.setText(text.replaceAll("¤", "").trim());
        view.setTextSize(dipsToPixels(context, textSizeInDips));
        view.setBackgroundResource(backgroundResource);
        int textColor = context.getResources().getColor(textColorResource);
        view.setTextColor(textColor);
        return view;
    }

    private static int dipsToPixels(Context ctx, float dips)
    {
        final float scale = ctx.getResources().getDisplayMetrics().density;
        int px = (int) (dips * scale + 0.5f);
        return px;
    }
}
