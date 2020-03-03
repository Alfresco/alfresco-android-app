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
package org.alfresco.mobile.android.application.fragments.site.request;

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.site.member.CancelPendingMembershipRequest;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;

import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Responsible to display the list of Site;
 * 
 * @author Jean Marie Pascal
 */
public class JoinSiteRequestAdapter extends BaseListAdapter<Site, JoinSiteViewHolder>
{
    private WeakReference<Fragment> fragment;

    public JoinSiteRequestAdapter(Fragment fr, int textViewResourceId, List<Site> objects)
    {
        super(fr.getActivity(), textViewResourceId, objects);
        this.vhClassName = JoinSiteViewHolder.class.getCanonicalName();
        this.fragment = new WeakReference<Fragment>(fr);
    }

    @Override
    protected void updateIcon(JoinSiteViewHolder vh, Site item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_site_light));

        vh.cancel_request.setTag(item);
        AccessibilityUtils.addContentDescription(vh.cancel_request,
                String.format(getContext().getString(R.string.joinsiterequest_cancel_description), item.getTitle()));
        vh.cancel_request.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Operator.with(fragment.get().getActivity()).load(
                        new CancelPendingMembershipRequest.Builder((Site) v.getTag()));
                v.setEnabled(false);
            }
        });
    }

    @Override
    protected void updateTopText(JoinSiteViewHolder vh, Site item)
    {
        vh.topText.setText(item.getShortName());
    }

    @Override
    protected void updateBottomText(JoinSiteViewHolder vh, Site item)
    {
        vh.bottomText.setVisibility(View.GONE);
    }
}
