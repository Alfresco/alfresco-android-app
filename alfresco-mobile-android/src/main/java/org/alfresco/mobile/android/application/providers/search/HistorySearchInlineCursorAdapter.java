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
package org.alfresco.mobile.android.application.providers.search;

import java.lang.ref.WeakReference;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.site.search.SearchSitesFragment;
import org.alfresco.mobile.android.ui.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.database.Cursor;
import androidx.fragment.app.Fragment;
import android.view.View;

public class HistorySearchInlineCursorAdapter extends BaseCursorLoader<TwoLinesViewHolder>
{
    private WeakReference<Fragment> frRef;

    public HistorySearchInlineCursorAdapter(Fragment fr, Cursor c, int layoutId)
    {
        super(fr.getActivity(), c, layoutId);
        this.frRef = new WeakReference<>(fr);
        this.vhClassName = TwoLinesViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, Cursor cursor)
    {
        // ((View)vh.icon.getParent()).setTag(cursor.getLong(HistorySearchSchema.COLUMN_ID_ID));
        if (cursor.isClosed()) { return; }
        vh.topText.setText(cursor.getString(HistorySearchSchema.COLUMN_DESCRIPTION_ID));
        HolderUtils.makeMultiLine(vh.topText, 2);
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder v, Cursor cursor)
    {
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, Cursor cursor)
    {
        if (cursor.isClosed()) { return; }
        vh.icon.setImageResource(R.drawable.ic_clock);
        vh.choose.setImageResource(R.drawable.ic_item_up);
        vh.choose.setVisibility(View.VISIBLE);
        vh.choose.setTag(cursor.getLong(HistorySearchSchema.COLUMN_ID_ID));
        if (frRef.get() != null && frRef.get() instanceof SearchSitesFragment)
        {
            vh.choose.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HistorySearch history = HistorySearchManager.retrieveHistorySearch(context, (Long) v.getTag());
                    ((SearchSitesFragment) frRef.get()).setSearchValue(history.getQuery());
                }
            });
        }
    }
}
