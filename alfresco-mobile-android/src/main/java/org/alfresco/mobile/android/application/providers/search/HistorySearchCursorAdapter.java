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
package org.alfresco.mobile.android.application.providers.search;

import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.LinearLayout;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class HistorySearchCursorAdapter extends BaseCursorLoader<GenericViewHolder>
{
    protected List<Long> selectedItems;
    
    public HistorySearchCursorAdapter(Context context, Cursor c, int layoutId, List<Long> selectedItems)
    {
        super(context, c, layoutId);
        this.selectedItems = selectedItems;
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Cursor cursor)
    {
        vh.topText.setText(cursor.getString(HistorySearchSchema.COLUMN_DESCRIPTION_ID));
        if (selectedItems != null && selectedItems.contains(cursor.getLong(HistorySearchSchema.COLUMN_ID_ID)))
        {
            UIUtils.setBackground(getSelectionLayout(vh),
                    vh.topText.getContext().getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(getSelectionLayout(vh), null);
        }
    }

    @Override
    protected void updateBottomText(GenericViewHolder v, Cursor cursor)
    {
        v.bottomText.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Cursor cursor)
    {
        vh.icon.setImageResource(R.drawable.ic_clock);
    }
    
    private LinearLayout getSelectionLayout(GenericViewHolder vh)
    {
        return (LinearLayout) vh.topText.getParent().getParent();
    }
}
