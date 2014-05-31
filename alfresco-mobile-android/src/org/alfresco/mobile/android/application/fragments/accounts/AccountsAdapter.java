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
package org.alfresco.mobile.android.application.fragments.accounts;

import java.util.List;

import org.alfresco.mobile.android.accounts.AccountSchema;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AccountsAdapter extends BaseListAdapter<AlfrescoAccount, GenericViewHolder>
{
    private List<AlfrescoAccount> selectedItems;

    private List<AlfrescoAccount> accounts;

    private int layoutId;

    public AccountsAdapter(Context context, List<AlfrescoAccount> items, int layoutId, List<AlfrescoAccount> selectedItems)
    {
        super(context, layoutId, items);
        this.selectedItems = selectedItems;
        this.layoutId = layoutId;
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
        if (acc.getActivation() != null)
        {
            v.bottomText.setText(getContext().getText(R.string.sign_up_cloud_awaiting_email));
        }
        else
        {
            v.bottomText.setText(acc.getUsername());
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
                    case NETWORK_ITEM:
                    case MANAGE_ITEM:
                        defaultIcon = R.drawable.ic_settings_light;
                        break;
                    default:
                        break;
                }
                vh.icon.setImageDrawable(getContext().getResources().getDrawable(defaultIcon));
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
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(iconId));
        AccessibilityUtils.addContentDescription(vh.icon, descriptionId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public static final int NETWORK_ITEM = -3;

    public static final int MANAGE_ITEM = -4;

    public static Cursor createMergeCursor(Context c, Cursor cursor)
    {
        MatrixCursor extras = new MatrixCursor(new String[] { AccountSchema.COLUMN_ID, AccountSchema.COLUMN_NAME });
        if (SessionUtils.getAccount(c) != null)
        {
            long type = SessionUtils.getAccount(c).getTypeId();
            if (type == AlfrescoAccount.TYPE_ALFRESCO_CLOUD || type == AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH)
            {
                extras.addRow(new String[] { NETWORK_ITEM + "", c.getString(R.string.cloud_networks_switch) });
            }
        }
        extras.addRow(new String[] { MANAGE_ITEM + "", c.getString(R.string.manage_accounts) });
        Cursor[] cursors = { cursor, extras };
        return new MergeCursor(cursors);
    }
}
