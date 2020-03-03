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
package org.alfresco.mobile.android.ui.site;

import java.util.List;

import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import androidx.fragment.app.FragmentActivity;

/**
 * Provides access to sites and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class SitesFoundationAdapter extends BaseListAdapter<Site, TwoLinesViewHolder>
{

    public SitesFoundationAdapter(FragmentActivity context, int textViewResourceId, List<Site> listItems)
    {
        super(context, textViewResourceId, listItems);
        vhClassName = TwoLinesViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, Site item)
    {
        vh.topText.setText(item.getTitle());
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, Site item)
    {
        vh.bottomText.setText(item.getDescription());
        HolderUtils.makeMultiLine(vh.bottomText, 3);
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, Site item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_site_light));
    }
}
