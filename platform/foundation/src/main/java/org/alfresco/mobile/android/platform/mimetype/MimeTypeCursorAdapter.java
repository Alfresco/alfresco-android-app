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
package org.alfresco.mobile.android.platform.mimetype;

import org.alfresco.mobile.android.ui.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class MimeTypeCursorAdapter extends BaseCursorLoader<TwoLinesViewHolder>
{
    public MimeTypeCursorAdapter(Context context, Cursor c, int layoutId)
    {
        super(context, c, layoutId);
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, Cursor cursor)
    {
        vh.topText.setText(cursor.getString(MimeTypeSchema.COLUMN_DESCRIPTION_ID));
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder v, Cursor cursor)
    {
        v.bottomText.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, Cursor cursor)
    {
    }
}
