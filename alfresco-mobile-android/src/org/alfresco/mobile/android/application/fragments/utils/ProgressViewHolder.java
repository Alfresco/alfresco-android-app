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
package org.alfresco.mobile.android.application.fragments.utils;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public final class ProgressViewHolder extends GenericViewHolder
{
    public ProgressBar progress;

    public ImageView iconTopRight;

    public ImageView iconBottomRight;

    public ImageView favoriteIcon;

    public ProgressViewHolder(View v)
    {
        super(v);
        this.progress = (ProgressBar) v.findViewById(R.id.status_progress);
        this.iconTopRight = (ImageView) v.findViewById(R.id.icon_top_right);
        this.iconBottomRight = (ImageView) v.findViewById(R.id.icon_bottom_right);
        this.favoriteIcon = (ImageView) v.findViewById(R.id.favorite_icon);
    }
}
