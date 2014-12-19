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
package org.alfresco.mobile.android.application.fragments.profiles;

import java.util.List;

import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.view.View;

/**
 * Provides access to ProfileConfigs and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class ProfileConfigAdapter extends BaseListAdapter<ProfileConfig, GenericViewHolder>
{
    public ProfileConfigAdapter(Activity context, int textViewResourceId, List<ProfileConfig> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, ProfileConfig item)
    {
        vh.topText.setText(item.getLabel());
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, ProfileConfig item)
    {
        vh.bottomText.setText(item.getDescription());
        vh.choose.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, ProfileConfig item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_settings));
    }
}
