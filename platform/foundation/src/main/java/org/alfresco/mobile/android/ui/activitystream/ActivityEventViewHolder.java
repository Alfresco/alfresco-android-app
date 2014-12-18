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
package org.alfresco.mobile.android.ui.activitystream;

import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.utils.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityEventViewHolder extends ViewHolder
{
    public TextView topText;

    public TextView bottomText;

    public ImageView icon;

    public TextView content;

    public ActivityEventViewHolder(View v)
    {
        super(v);
        icon = (ImageView) v.findViewById(R.id.icon);
        topText = (TextView) v.findViewById(R.id.toptext);
        bottomText = (TextView) v.findViewById(R.id.bottomtext);
        content = (TextView) v.findViewById(R.id.content);
    }
}
