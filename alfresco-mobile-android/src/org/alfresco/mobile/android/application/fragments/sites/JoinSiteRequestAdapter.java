/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.sites;

import java.util.List;

import org.alfresco.mobile.android.api.model.JoinSiteRequest;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.ViewHolder;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Responsible to display the list of JoinSiteRequest;
 * 
 * @author Jean Marie Pascal
 */
public class JoinSiteRequestAdapter extends BaseListAdapter<JoinSiteRequest, GenericViewHolder>
{
    private Fragment fragment;

    public JoinSiteRequestAdapter(Fragment fr, int textViewResourceId, List<JoinSiteRequest> objects)
    {
        super(fr.getActivity(), textViewResourceId, objects);
        this.vhClassName = GenericViewHolder.class.getCanonicalName();
        this.fragment = fr;
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, JoinSiteRequest item)
    {
        vh.topText.setText(item.getSiteShortName());
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, JoinSiteRequest item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mime_site));

        vh.cancel_request.setTag(item);
        vh.cancel_request.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bundle b = new Bundle();
                b.putSerializable(JointSiteRequestCancelLoaderCallBack.PARAM_JOIN_SITE_REQUEST,
                        (JoinSiteRequest) v.getTag());
                new JointSiteRequestCancelLoaderCallBack(fragment).execute(b);
            }
        });
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, JoinSiteRequest item)
    {
        vh.bottomText.setVisibility(View.GONE);
    }
}

final class GenericViewHolder extends ViewHolder
{
    public TextView topText;

    public TextView bottomText;

    public ImageView icon;

    public Button cancel_request;

    public GenericViewHolder(View v)
    {
        super(v);
        icon = (ImageView) v.findViewById(R.id.icon);
        topText = (TextView) v.findViewById(R.id.toptext);
        bottomText = (TextView) v.findViewById(R.id.bottomtext);
        cancel_request = (Button) v.findViewById(R.id.cancel_request);
    }
}