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
package org.alfresco.mobile.android.application.fragments.account;

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AccountsAdapter extends BaseListAdapter<AlfrescoAccount, GenericViewHolder>
{
    public static final int NETWORK_ITEM = -3;

    public static final int MANAGE_ITEM = -5;

    public static final int PROFILES_ITEM = -4;

    private List<AlfrescoAccount> selectedItems;

    private WeakReference<Activity> activityRef;

    private int layoutId;

    public AccountsAdapter(Activity activity, List<AlfrescoAccount> items, int layoutId,
            List<AlfrescoAccount> selectedItems)
    {
        super(activity, layoutId, items);
        this.selectedItems = selectedItems;
        this.layoutId = layoutId;
        this.activityRef = new WeakReference<Activity>(activity);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, AlfrescoAccount acc)
    {
        vh.topText.setText(acc.getTitle());
        if (activityRef.get() instanceof PublicDispatcherActivity){
            vh.topText.setTextColor(Color.BLACK);
        }
    }

    @Override
    protected void updateBottomText(GenericViewHolder v, AlfrescoAccount acc)
    {
        switch (layoutId)
        {
            case R.layout.app_account_list_row:
                // Do nothing
                break;
            default:
                updateBottomTextList(v, acc);
                break;
        }
    }

    private void updateBottomTextList(GenericViewHolder v, AlfrescoAccount acc)
    {
        v.bottomText.setText(acc.getUsername());
        if (activityRef.get() instanceof PublicDispatcherActivity){
            v.bottomText.setTextColor(android.R.color.black);
        }

        if (selectedItems != null && selectedItems.contains(acc))
        {
            UIUtils.setBackground(((LinearLayout) v.icon.getParent().getParent()), getContext().getResources()
                    .getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((LinearLayout) v.icon.getParent().getParent()), null);
        }
        v.choose.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, AlfrescoAccount acc)
    {
        switch (layoutId)
        {
            case R.layout.app_account_list_row:
                int itemName = (int) acc.getId();
                int defaultIcon = R.drawable.ic_account_light;
                switch (itemName)
                {
                    case PROFILES_ITEM:
                    case NETWORK_ITEM:
                    case MANAGE_ITEM:
                        defaultIcon = R.drawable.ic_settings_light;
                        vh.icon.setImageDrawable(getContext().getResources().getDrawable(defaultIcon));
                        break;
                    default:
                        RenditionManager.with(activityRef.get()).loadAvatar(acc.getUsername()).placeHolder(defaultIcon)
                                .into(vh.icon);
                        break;
                }
                break;
            default:
                updateIconList(vh, acc);
                break;
        }

    }

    private void updateIconList(GenericViewHolder vh, AlfrescoAccount acc)
    {
        int iconId = R.drawable.ic_onpremise;
        int descriptionId = R.string.account_alfresco_onpremise;
        switch (acc.getTypeId())
        {
            case AlfrescoAccount.TYPE_ALFRESCO_TEST_BASIC:
            case AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH:
                iconId = R.drawable.ic_cloud_alf;
                descriptionId = R.string.account_alfresco_cloud;
                break;
            case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                iconId = R.drawable.ic_cloud;
                descriptionId = R.string.account_alfresco_cloud;
                break;
            default:
                iconId = R.drawable.ic_onpremise;
                descriptionId = R.string.account_alfresco_onpremise;
                break;
        }
        RenditionManager.with(activityRef.get()).loadAvatar(acc.getUsername()).placeHolder(iconId).into(vh.icon);
        AccessibilityUtils.addContentDescription(vh.icon, descriptionId);
    }
}
