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
package org.alfresco.mobile.android.application.fragments.preferences;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.account.AccountsAdapter;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.signin.AccountSignInFragment;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.security.DataProtectionUserDialogFragment;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.mdm.MDMManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Manage global application preferences.
 *
 * @author Jean Marie Pascal
 */
public class GeneralPreferences extends AlfrescoFragment
{
    public static final String TAG = GeneralPreferences.class.getName();

    public static final String HAS_ACCESSED_PAID_SERVICES = "HasAccessedPaidServices";

    private static final String PRIVATE_FOLDERS_BUTTON = "privatefoldersbutton";

    private AlfrescoAccount account;

    private MDMManager mdmManager;

    private TwoLinesViewHolder dataProtectionVH, passcodeVH;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public GeneralPreferences()
    {
        requiredSession = false;
        setHasOptionsMenu(true);
    }

    protected static GeneralPreferences newInstanceByTemplate(Bundle b)
    {
        GeneralPreferences cbf = new GeneralPreferences();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.fr_settings, container, false));

        // TITLE
        getActivity().setTitle(R.string.settings);

        TwoLinesViewHolder vh;
        // Links
        // Alfresco Website
        vh = HolderUtils.configure(viewById(R.id.settings_links_website), getString(R.string.settings_links_website),
                getString(R.string.settings_links_website_summary), -1);
        viewById(R.id.settings_links_website_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActionUtils.startWebBrowser(getActivity(), getString(R.string.settings_links_website_url));
            }
        });

        // Play Store
        vh = HolderUtils.configure(viewById(R.id.settings_rating), getString(R.string.settings_rating),
                getString(R.string.settings_rating_summary), -1);
        viewById(R.id.settings_rating_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startPlayStore();
            }
        });

        // Facebook
        vh = HolderUtils.configure(viewById(R.id.settings_links_facebook), getString(R.string.settings_links_facebook),
                getString(R.string.settings_links_facebook_summary), -1);
        viewById(R.id.settings_links_facebook_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActionUtils.startWebBrowser(getActivity(), getString(R.string.settings_links_facebook_url));
            }
        });

        // About
        vh = HolderUtils.configure(viewById(R.id.settings_about), getString(R.string.version_number),
                AboutFragment.getVersionNumber(getActivity()), -1);
        viewById(R.id.settings_about_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AboutFragment.with(getActivity()).displayAsDialog();
            }
        });

        recreate();

        return getRootView();
    }

    public void onCreate(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

        mdmManager = MDMManager.getInstance(getActivity());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        recreate();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL
    // ///////////////////////////////////////////////////////////////////////////
    private void recreate()
    {
        account = getAccount();

        // Accounts
        List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(getActivity());
        View accountView;
        LinearLayout accountContainer = (LinearLayout) viewById(R.id.settings_accounts_container);
        accountContainer.removeAllViews();
        TwoLinesViewHolder vh;
        for (AlfrescoAccount account : accounts)
        {
            accountView = LayoutInflater.from(getActivity()).inflate(R.layout.row_two_lines_borderless,
                    accountContainer, false);
            accountView.setTag(account.getId());
            vh = HolderUtils.configure(accountView, account.getUsername(), account.getTitle(),
                    R.drawable.ic_account_circle_grey);
            AccountsAdapter.displayAvatar(getActivity(), account, R.drawable.ic_account_light, vh.icon);

            if (SessionManager.getInstance(getActivity()).getSession(account.getId()) != null)
            {
                vh.choose.setVisibility(View.VISIBLE);
                vh.choose.setImageResource(R.drawable.ic_validate);
            }

            accountView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    if (mdmManager.hasConfig())
                    {
                        AlfrescoAccount selectedAccount = AlfrescoAccountManager.getInstance(getActivity())
                                .retrieveAccount((Long) v.getTag());
                        AccountSignInFragment.with(getActivity()).account(selectedAccount).display();
                    }
                    else
                    {
                        AccountSettingsFragment.with(getActivity()).accountId((Long) v.getTag()).display();
                    }
                }
            });
            accountContainer.addView(accountView);
        }

        // Add Account
        if (!mdmManager.hasConfig())
        {
            HolderUtils.configure(viewById(R.id.settings_accounts_create), getString(R.string.action_add_account),
                    R.drawable.ic_add);
            viewById(R.id.settings_accounts_create_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent i = new Intent(getActivity(), WelcomeActivity.class);
                    i.putExtra(WelcomeActivity.EXTRA_ADD_ACCOUNT, true);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(i);
                }
            });
        }
        else
        {
            hide(R.id.settings_accounts_create_container);
        }

        // Preferences
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // DATA PROTECTION
        dataProtectionVH = HolderUtils.configure(viewById(R.id.settings_privatefolder),
                getString(R.string.settings_privatefolder_title), getString(R.string.settings_privatefolder_summary),
                -1);

        if (!sharedPref.getBoolean(HAS_ACCESSED_PAID_SERVICES, false))
        {
            viewById(R.id.settings_privatefolder_container).setFocusable(false);
            viewById(R.id.settings_privatefolder_container).setClickable(false);
            viewById(R.id.settings_privatefolder_container).setEnabled(false);
            dataProtectionVH.bottomText.setText(R.string.data_protection_unavailable);
            DataProtectionManager.getInstance(getActivity()).setDataProtectionEnable(false);
        }
        else
        {
            viewById(R.id.settings_privatefolder_container).setFocusable(true);
            viewById(R.id.settings_privatefolder_container).setClickable(true);
            viewById(R.id.settings_privatefolder_container).setEnabled(true);
            dataProtectionVH.bottomText.setText(DataProtectionManager.getInstance(getActivity())
                    .hasDataProtectionEnable() ? R.string.data_protection_on : R.string.data_protection_off);
        }

        viewById(R.id.settings_privatefolder_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final File folder = AlfrescoStorageManager.getInstance(getActivity()).getPrivateFolder("", null);
                if (folder != null)
                {
                    DataProtectionUserDialogFragment.newInstance(false).show(getActivity().getSupportFragmentManager(),
                            DataProtectionUserDialogFragment.TAG);
                }
                else
                {
                    AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(
                            getString(R.string.sdinaccessible));
                }

            }
        });

        // PASSCODE
        passcodeVH = HolderUtils.configure(viewById(R.id.passcode_preference), getString(R.string.passcode_title),
                getString(R.string.passcode_preference), -1);

        // PASSCODE
        Boolean passcodeEnable = sharedPref.getBoolean(PasscodePreferences.KEY_PASSCODE_ENABLE, false);
        boolean isActivate = sharedPref.getBoolean(HAS_ACCESSED_PAID_SERVICES, false);
        viewById(R.id.passcode_preference_container).setFocusable(isActivate);
        viewById(R.id.passcode_preference_container).setClickable(isActivate);
        viewById(R.id.passcode_preference_container).setEnabled(isActivate);
        passcodeVH.bottomText.setText(passcodeEnable ? R.string.passcode_enable : R.string.passcode_disable);
        viewById(R.id.passcode_preference_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PasscodePreferences.with(getActivity()).display();
            }
        });

        // In case of MDM we disable all enterprise feature
        if (mdmManager.hasConfig())
        {
            viewById(R.id.settings_privatefolder_container).setEnabled(false);
            dataProtectionVH.bottomText.setText(R.string.mdm_managed);
            viewById(R.id.passcode_preference_container).setEnabled(false);
            passcodeVH.bottomText.setText(R.string.mdm_managed);
        }
    }

    private void startPlayStore()
    {
        ActionUtils.startPlayStore(getActivity(), getString(R.string.settings_rating_packagename));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshDataProtection()
    {
        if (dataProtectionVH != null)
        {
            dataProtectionVH.bottomText.setText(DataProtectionManager.getInstance(getActivity())
                    .hasDataProtectionEnable() ? R.string.data_protection_on : R.string.data_protection_off);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.clear();
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
