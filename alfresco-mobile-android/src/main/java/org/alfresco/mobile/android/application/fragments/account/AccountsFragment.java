/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.account;

import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AccountsFragment extends AlfrescoFragment
{
    public static final String TAG = AccountsFragment.class.getName();

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public AccountsFragment()
    {
        requiredSession = false;
        checkSession = false;
        eventBusRequired = false;
        screenName = AnalyticsManager.SCREEN_ACCOUNTS_LISTING;
    }

    protected static AccountsFragment newInstanceByTemplate(Bundle b)
    {
        AccountsFragment cbf = new AccountsFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String onPrepareTitle()
    {
        return getString(R.string.accounts_title_select);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.fr_accounts, container, false));

        // Accounts
        List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(getActivity());
        View accountView;
        LinearLayout accountContainer = (LinearLayout) viewById(R.id.settings_accounts_container);
        accountContainer.removeAllViews();
        TwoLinesViewHolder vh;
        for (AlfrescoAccount account : accounts)
        {
            accountView = LayoutInflater.from(getActivity()).inflate(R.layout.row_two_lines_borderless_rounded,
                    accountContainer, false);
            accountView.setTag(account.getId());
            vh = HolderUtils.configure(accountView, account.getUsername(), account.getTitle(),
                    R.drawable.ic_account_circle_grey);
            AccountsAdapter.displayAvatar(getActivity(), account, R.drawable.ic_account_light, vh.icon);

            vh.choose.setVisibility(View.GONE);

            accountView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (getActivity() instanceof PublicDispatcherActivity)
                    {
                        AlfrescoAccount selectedAccount = AlfrescoAccountManager.getInstance(getActivity())
                                .retrieveAccount((Long) v.getTag());
                        ((PublicDispatcherActivity) getActivity()).managePublicIntent(selectedAccount);
                    }
                }
            });
            accountContainer.addView(accountView);
        }

        return getRootView();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }

}
