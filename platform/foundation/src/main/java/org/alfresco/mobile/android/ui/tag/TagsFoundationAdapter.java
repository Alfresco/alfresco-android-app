/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.ui.tag;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.Tag;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.widget.Filter;

/**
 * Provides access to tags and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class TagsFoundationAdapter extends BaseListAdapter<Tag, TwoLinesViewHolder>
{

    private List<Tag> selectedItems;

    private List<Tag> mOriginalValues;

    private ArrayFilter mFilter;

    private final Object mLock = new Object();

    public TagsFoundationAdapter(FragmentActivity context, int textViewResourceId, List<Tag> listItems)
    {
        this(context, textViewResourceId, listItems, new ArrayList<Tag>(0));
        vhClassName = TwoLinesViewHolder.class.getCanonicalName();
    }

    public TagsFoundationAdapter(FragmentActivity context, int textViewResourceId, List<Tag> listItems,
            List<Tag> selectedItems)
    {
        super(context, textViewResourceId, listItems);
        this.selectedItems = selectedItems;
        mOriginalValues = listItems;
        vhClassName = TwoLinesViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, Tag item)
    {
        vh.topText.setText(item.getValue());
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, Tag item)
    {
        if (vh.bottomText != null)
        {
            vh.bottomText.setVisibility(View.GONE);
        }
        if (selectedItems.contains(item))
        {
            vh.choose.setVisibility(View.VISIBLE);
        }
        else
        {
            vh.choose.setVisibility(View.GONE);
        }
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, Tag item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mime_tags));
    }

    /**
     * {@inheritDoc}
     */
    public Filter getFilter()
    {
        if (mFilter == null)
        {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    /**
     * <p>
     * An array filter constrains the content of the array adapter with a
     * prefix. Each item that does not start with the supplied prefix is removed
     * from the list.
     * </p>
     */
    private class ArrayFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence prefix)
        {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null)
            {
                synchronized (mLock)
                {
                    mOriginalValues = new ArrayList<Tag>();
                }
            }

            if (prefix == null || prefix.length() == 0)
            {
                ArrayList<Tag> list;
                synchronized (mLock)
                {
                    list = new ArrayList<Tag>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            }
            else
            {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<Tag> values;
                synchronized (mLock)
                {
                    values = new ArrayList<Tag>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<Tag> newValues = new ArrayList<Tag>();

                for (int i = 0; i < count; i++)
                {
                    final Tag value = values.get(i);
                    final String valueText = value.getValue().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString))
                    {
                        newValues.add(value);
                    }
                    else
                    {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with
                        // space(s)
                        for (int k = 0; k < wordCount; k++)
                        {
                            if (words[k].startsWith(prefixString))
                            {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            synchronized (mLock)
            {
                final ArrayList<Tag> localItems = (ArrayList<Tag>) results.values;
                notifyDataSetChanged();
                clear();
                // Add the items back in
                for (Tag tag : localItems)
                {
                    add(tag);
                }
            }

        }
    }
}
