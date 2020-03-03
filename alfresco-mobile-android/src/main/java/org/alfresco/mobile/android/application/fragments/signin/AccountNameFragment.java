/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco Activiti Mobile for Android.
 *
 * Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.alfresco.mobile.android.application.fragments.signin;

import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.fragments.account.AccountsAdapter;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.person.AvatarEvent;
import org.alfresco.mobile.android.async.person.AvatarRequest;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.otto.Subscribe;

public class AccountNameFragment extends AlfrescoFragment
{
    public static final String TAG = AccountNameFragment.class.getName();

    private static final String ARGUMENT_ACCOUNT_ID = "accountId";

    private AlfrescoAccount account;

    private MaterialEditText accountView;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountNameFragment()
    {
        super();
        screenName = AnalyticsManager.SCREEN_ACCOUNT_NAME;
    }

    public static AccountNameFragment newInstanceByTemplate(Bundle b)
    {
        AccountNameFragment cbf = new AccountNameFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.fr_account_name, container, false));
        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final Long accountId = BundleUtils.getLong(getArguments(), ARGUMENT_ACCOUNT_ID);
        account = AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId);

        accountView = ((MaterialEditText) viewById(R.id.account_name));
        accountView.setHint(account.getTitle());
        accountView.requestFocus();
        UIUtils.showKeyboard(getActivity(), accountView);

        // Retrieve Account Icon
        Operator.with(getActivity()).load(new AvatarRequest.Builder(account.getUsername()));

        // Display placeholder
        AccountsAdapter.displayAvatar(getActivity(), account, R.drawable.ic_account_light,
                (ImageView) viewById(R.id.profile_picture));

        viewById(R.id.account_action_server).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (accountView.getText().length() > 0)
                {
                    AlfrescoAccountManager.getInstance(getActivity()).update(account.getId(),
                            AlfrescoAccount.ACCOUNT_NAME, accountView.getText().toString().trim());
                    AlfrescoAccount updatedAccount = AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(
                            accountId);
                    SessionManager.getInstance(getActivity()).saveSession(updatedAccount, getSession());
                    SessionManager.getInstance(getActivity()).saveAccount(updatedAccount);
                }

                if (getActivity() instanceof WelcomeActivity && ((WelcomeActivity) getActivity()).isCreation())
                {
                    getActivity().finish();
                }
                else
                {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                }
            }
        });

    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAvatarEvent(AvatarEvent event)
    {
        if (event.hasException) { return; }
        AccountsAdapter.displayAvatar(getActivity(), account, R.drawable.ic_account_light,
                (ImageView) viewById(R.id.profile_picture));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
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

        public Builder accountId(Long accountId)
        {
            extraConfiguration.putLong(ARGUMENT_ACCOUNT_ID, accountId);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }
}
