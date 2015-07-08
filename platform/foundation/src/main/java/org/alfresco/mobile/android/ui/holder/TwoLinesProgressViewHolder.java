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

package org.alfresco.mobile.android.ui.holder;

import org.alfresco.mobile.android.foundation.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Default class for listing item viewholder.
 * 
 * @author Jean Marie Pascal
 */
public class TwoLinesProgressViewHolder extends TwoLinesViewHolder
{
    public ProgressBar progress;

    public ImageView favoriteIcon;

    public ImageView syncIcon;

    public ImageView iconRight;

    public TwoLinesProgressViewHolder(View v)
    {
        super(v);
        progress = (ProgressBar) v.findViewById(R.id.status_progress);
        favoriteIcon = (ImageView) v.findViewById(R.id.favorite_icon);
        syncIcon = (ImageView) v.findViewById(R.id.sync_icon);
        iconRight = (ImageView) v.findViewById(R.id.icon_bottom_right);
        topTextRight = (TextView) v.findViewById(R.id.toptext_right);
    }
}
