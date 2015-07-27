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
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.config.MenuConfigFragment;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.security.DataProtectionUserDialogFragment;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.mdm.MDMManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesCheckboxViewHolder;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private TwoLinesViewHolder dataProtectionVH, passcodeVH, menuCustomizationVH;

    private TwoLinesCheckboxViewHolder syncFavoritesVH;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public GeneralPreferences()
    {
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

        // Links
        // Alfresco Website
        TwoLinesViewHolder vh;
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

        recreate();

        return getRootView();
    }

    public void onCreate(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

        mdmManager = MDMManager.getInstance(getActivity());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL
    // ///////////////////////////////////////////////////////////////////////////
    private void recreate()
    {

        account = getAccount();

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

        // CUSTOMIZATION
        menuCustomizationVH = HolderUtils.configure(viewById(R.id.settings_custom_menu_manage),
                getString(R.string.settings_custom_menu_manage), getString(R.string.settings_custom_menu_summary), -1);

        if (ConfigManager.getInstance(getActivity()).hasRemoteConfig(account.getId())
                && ConfigManager.getInstance(getActivity()).getRemoteConfig(account.getId()).hasViewConfig())
        {
            viewById(R.id.settings_custom_menu_manage_container).setEnabled(false);
            menuCustomizationVH.bottomText.setText(R.string.settings_custom_menu_disable);
        }
        else
        {
            viewById(R.id.settings_custom_menu_manage_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    MenuConfigFragment.with(getActivity()).display();
                }
            });
        }

        // SYNC
        Boolean syncEnable = SyncContentManager.getInstance(getActivity()).hasActivateSync(account);
        Boolean syncWifiEnable = SyncContentManager.getInstance(getActivity()).hasWifiOnlySync(account);

        if (!syncEnable)
        {
            viewById(R.id.settings_sync_container).setVisibility(View.GONE);
        }
        else if (syncEnable)
        {
            viewById(R.id.settings_sync_container).setVisibility(View.VISIBLE);
            syncFavoritesVH = HolderUtils.configure(viewById(R.id.favorite_sync_wifi),
                    getString(R.string.settings_favorite_sync_data),
                    getString(R.string.settings_favorite_sync_data_all), !syncWifiEnable);

            syncFavoritesVH.choose.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    SyncContentManager.getInstance(getActivity()).setWifiOnlySync(account,
                            syncFavoritesVH.choose.isChecked());
                }
            });
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
