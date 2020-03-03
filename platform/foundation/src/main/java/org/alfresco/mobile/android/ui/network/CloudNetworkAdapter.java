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
package org.alfresco.mobile.android.ui.network;

import java.util.List;

import org.alfresco.mobile.android.api.session.CloudNetwork;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import androidx.fragment.app.FragmentActivity;
import android.view.View;

/**
 * Provides access to CloudNetworks and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class CloudNetworkAdapter extends BaseListAdapter<CloudNetwork, TwoLinesViewHolder>
{
    public CloudNetworkAdapter(FragmentActivity context, int textViewResourceId, List<CloudNetwork> listItems)
    {
        super(context, textViewResourceId, listItems);
        this.vhClassName = TwoLinesViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, CloudNetwork item)
    {
        vh.topText.setText(item.getIdentifier());
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, CloudNetwork item)
    {
        vh.bottomText.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, CloudNetwork item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_network));
        AlfrescoAccount currentAccount = AlfrescoAccountManager.getInstance(getContext()).getDefaultAccount();
        if (currentAccount != null && currentAccount.getRepositoryId().equals(item.getIdentifier()))
        {
            vh.choose.setVisibility(View.VISIBLE);
            vh.choose.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_validate));
        }
        else
        {
            vh.choose.setVisibility(View.GONE);
        }
    }
}
