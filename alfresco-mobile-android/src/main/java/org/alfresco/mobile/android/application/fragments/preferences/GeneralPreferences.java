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
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.configuration.features.DataProtectionConfigFeature;
import org.alfresco.mobile.android.application.configuration.features.PasscodeConfigFeature;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.account.AccountsAdapter;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.signin.AccountSignInFragment;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.extensions.AnalyticHelper;
import org.alfresco.mobile.android.application.security.DataProtectionUserDialogFragment;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.extensions.DevToolsManager;
import org.alfresco.mobile.android.platform.intent.PrivateRequestCode;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.mdm.MDMManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesCheckboxViewHolder;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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

    public static final String HAS_SHOWN_SHUTTING_DOWN_ALERT = "show_cloud_shutting_down";

    private static final String PRIVATE_FOLDERS_BUTTON = "privatefoldersbutton";

    private AlfrescoAccount account;

    private MDMManager mdmManager;

    private TwoLinesViewHolder dataProtectionVH, passcodeVH;

    private TwoLinesCheckboxViewHolder diagnosticVH;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public GeneralPreferences()
    {
        requiredSession = false;
        setHasOptionsMenu(true);
        screenName = AnalyticsManager.SCREEN_SETTINGS_DETAILS;
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
    public String onPrepareTitle()
    {
        return getString(R.string.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.fr_settings, container, false));

        TwoLinesViewHolder vh;

        // Feedback - Email
        vh = HolderUtils.configure(viewById(R.id.settings_feedback_email_container),
                getString(R.string.settings_feedback_email), null, -1);
        // HolderUtils.makeMultiLine(vh.bottomText, 3);
        viewById(R.id.settings_feedback_email_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActionUtils.actionSendFeedbackEmail(GeneralPreferences.this);
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

        if (DevToolsManager.getInstance(getActivity()) != null)
        {
            show(R.id.settings_dev_tools_container);
            DevToolsManager.getInstance(getActivity()).generateMenu(getActivity(),
                    (ViewGroup) viewById(R.id.settings_dev_tools_items));
        }
        else
        {
            hide(R.id.settings_dev_tools_container);
        }

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
            accountView = LayoutInflater.from(getActivity()).inflate(R.layout.row_two_lines_borderless_rounded,
                    accountContainer, false);
            accountView.setTag(account.getId());
            vh = HolderUtils.configure(accountView, account.getUsername(), account.getTitle(),
                    R.drawable.ic_account_circle_grey);
            AccountsAdapter.displayAvatar(getActivity(), account, R.drawable.ic_account_light, vh.icon);

            if (SessionManager.getInstance(getActivity()).getSession(account.getId()) != null)
            {
                vh.choose.setVisibility(View.VISIBLE);
                vh.choose.setTag(account);
                vh.choose.setImageResource(R.drawable.ic_validate);
                vh.choose.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        AlfrescoAccount acc = (AlfrescoAccount) v.getTag();
                        SessionManager.getInstance(getActivity()).saveAccount(acc);
                        if (getActivity() instanceof PrivateDialogActivity)
                        {
                            getActivity().setResult(PrivateRequestCode.RESULT_REFRESH_SESSION);
                            getActivity().finish();
                        }
                        else if (getActivity() instanceof MainActivity)
                        {
                            EventBusManager.getInstance().post(new RequestSessionEvent(acc, true));
                        }
                    }
                });
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
        HolderUtils.makeMultiLine(dataProtectionVH.bottomText, 2);

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
            dataProtectionVH.bottomText
                    .setText(DataProtectionManager.getInstance(getActivity()).hasDataProtectionEnable()
                            ? R.string.data_protection_on : R.string.data_protection_off);
        }

        DataProtectionConfigFeature feature = new DataProtectionConfigFeature(getActivity());
        if (feature.isProtected())
        {
            viewById(R.id.settings_privatefolder_container).setFocusable(false);
            viewById(R.id.settings_privatefolder_container).setClickable(false);
            viewById(R.id.settings_privatefolder_container).setEnabled(false);
            dataProtectionVH.bottomText.setText(R.string.mdm_managed);
        }
        else if (sharedPref.getBoolean(HAS_ACCESSED_PAID_SERVICES, false))
        {
            viewById(R.id.settings_privatefolder_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final File folder = AlfrescoStorageManager.getInstance(getActivity()).getPrivateFolder("", null);
                    if (folder != null)
                    {
                        DataProtectionUserDialogFragment.newInstance(false)
                                .show(getActivity().getSupportFragmentManager(), DataProtectionUserDialogFragment.TAG);
                    }
                    else
                    {
                        AlfrescoNotificationManager.getInstance(getActivity())
                                .showLongToast(getString(R.string.sdinaccessible));
                    }

                }
            });
        }

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

        PasscodeConfigFeature passcodeConfig = new PasscodeConfigFeature(getActivity());
        if (passcodeConfig.isProtected())
        {
            passcodeVH.bottomText.setText(R.string.mdm_managed);
        }

        // In case of MDM we disable all enterprise feature
        if (mdmManager.hasConfig())
        {
            viewById(R.id.settings_privatefolder_container).setEnabled(false);
            dataProtectionVH.bottomText.setText(R.string.mdm_managed);
            viewById(R.id.passcode_preference_container).setEnabled(false);
            passcodeVH.bottomText.setText(R.string.mdm_managed);
        }

        // Feedback - Analytics
        if (AnalyticsManager.getInstance(getActivity()) == null
                || AnalyticsManager.getInstance(getActivity()).isBlocked())
        {
            boolean isEnable = AnalyticsManager.getInstance(getActivity()) != null
                    && AnalyticsManager.getInstance(getActivity()).isEnable();

            diagnosticVH = HolderUtils.configure(viewById(R.id.settings_diagnostic),
                    getString(R.string.settings_feedback_diagnostic), getString(R.string.settings_custom_menu_disable),
                    isEnable);
            HolderUtils.makeMultiLine(diagnosticVH.bottomText, 3);
            diagnosticVH.choose.setVisibility(View.GONE);
            diagnosticVH.choose.setEnabled(false);
        }
        else
        {
            boolean isEnable = AnalyticsManager.getInstance(getActivity()) != null
                    && AnalyticsManager.getInstance(getActivity()).isEnable();

            diagnosticVH = HolderUtils.configure(viewById(R.id.settings_diagnostic),
                    getString(R.string.settings_feedback_diagnostic),
                    getString(R.string.settings_feedback_diagnostic_summary), isEnable);
            HolderUtils.makeMultiLine(diagnosticVH.bottomText, 4);
            diagnosticVH.choose.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (diagnosticVH.choose.isChecked())
                    {
                        AnalyticHelper.optIn(getActivity(), getAccount());
                    }
                    else
                    {
                        AnalyticHelper.optOut(getActivity(), getAccount());
                    }
                }
            });
        }
    }

    private void retrieveServerConfigFeature()
    {

    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshDataProtection()
    {
        if (dataProtectionVH != null)
        {
            dataProtectionVH.bottomText
                    .setText(DataProtectionManager.getInstance(getActivity()).hasDataProtectionEnable()
                            ? R.string.data_protection_on : R.string.data_protection_off);
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
