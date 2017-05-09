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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.configuration.features.PasscodeConfigFeature;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.application.security.PassCodeDialogFragment;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.SingleLineSwitchViewHolder;
import org.alfresco.mobile.android.ui.holder.SingleLineViewHolder;
import org.alfresco.mobile.android.ui.holder.TwoLinesCheckboxViewHolder;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import com.afollestad.materialdialogs.MaterialDialog;

import android.content.Context;
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
public class PasscodePreferences extends AlfrescoFragment
{
    public static final String TAG = PasscodePreferences.class.getName();

    public static final String KEY_PASSCODE_ENABLE = "PasscodeEnabled";

    public static final String KEY_PASSCODE_VALUE = "PasscodeValue";

    public static final String KEY_PASSCODE_TIMEOUT = "PasscodeTimeOut";

    public static final String KEY_PASSCODE_ACTIVATED_AT = "PasscodeActivatedAt";

    public static final String KEY_PASSCODE_ATTEMPT = "PasscodeAttempt";

    public static final String KEY_PASSCODE_MAX_ATTEMPT = "PasscodeMaxAttempt";

    private static final String DEFAULT_TIMEOUT = "300000";

    private static final int ONE_MINUTE = 60000;

    private static final long DEFAULT_ACTIVATION_TIME = -1;

    private boolean valueHasChanged;

    private boolean passcodeEnable;

    private boolean editionEnable;

    private boolean maxAttemptActivated;

    private SharedPreferences sharedPref;

    private TwoLinesViewHolder passcodeTimeoutVH;

    private TwoLinesCheckboxViewHolder passcodeDataVH;

    private SingleLineViewHolder passcodeChangeVH;

    private SingleLineSwitchViewHolder passcodeEnableVH;

    private ArrayList<String> timeOutValues;

    private int index;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public PasscodePreferences()
    {
        requiredSession = false;
        screenName = AnalyticsManager.SCREEN_SETTINGS_PASSCODE;
    }

    protected static PasscodePreferences newInstanceByTemplate(Bundle b)
    {
        PasscodePreferences cbf = new PasscodePreferences();
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
        setRootView(inflater.inflate(R.layout.fr_settings_passcode, container, false));

        timeOutValues = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.passcode_timeout_values)));

        recreate();

        return getRootView();
    }

    public void onCreate(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL
    // ///////////////////////////////////////////////////////////////////////////
    private void recreate()
    {
        // Preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        passcodeEnable = sharedPref.getBoolean(KEY_PASSCODE_ENABLE, false);
        maxAttemptActivated = (sharedPref.getInt(KEY_PASSCODE_MAX_ATTEMPT, 0) != 0);
        long timeout = Long.parseLong(sharedPref.getString(KEY_PASSCODE_TIMEOUT, "300000"));
        index = timeOutValues.indexOf(sharedPref.getString(KEY_PASSCODE_TIMEOUT, "300000"));

        // PASSCODE CHANGE
        passcodeChangeVH = HolderUtils.configure(viewById(R.id.passcode_change), getString(R.string.passcode_change),
                -1);
        if (passcodeEnable)
        {
            viewById(R.id.passcode_change_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    editionEnable = true;
                    PassCodeDialogFragment f = PassCodeDialogFragment.modify();
                    f.show(getActivity().getSupportFragmentManager(), PassCodeDialogFragment.TAG);
                }
            });
        }

        // PASSCODE TIMEOUT
        passcodeTimeoutVH = HolderUtils.configure(viewById(R.id.passcode_timeout),
                getString(R.string.passcode_timeout_title), getString(R.string.passcode_timeout_title), -1);
        int minutes = Math.round(timeout / ONE_MINUTE);
        passcodeTimeoutVH.bottomText.setText(
                String.format(MessageFormat.format(getString(R.string.passcode_timeout_summary), minutes), minutes));
        HolderUtils.makeMultiLine(passcodeTimeoutVH.bottomText, 3);

        if (passcodeEnable)
        {
            viewById(R.id.passcode_timeout_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).cancelable(false)
                            .title(R.string.passcode_timeout_title).items(R.array.passcode_timeout_entries)
                            .itemsCallbackSingleChoice(index, new MaterialDialog.ListCallbackSingleChoice()
                    {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text)
                        {
                            String newValue = getResources().getStringArray(R.array.passcode_timeout_values)[which];
                            index = which;
                            sharedPref.edit().putString(KEY_PASSCODE_TIMEOUT, (String) newValue).apply();
                            int minutes = Math.round(Long.parseLong((String) newValue) / ONE_MINUTE);
                            passcodeTimeoutVH.bottomText.setText(String.format(
                                    MessageFormat.format(getString(R.string.passcode_timeout_summary), minutes),
                                    minutes));
                            return true;
                        }
                    }).negativeText(R.string.cancel);
                    builder.show();
                }
            });
        }

        // PASSCODE DATA
        passcodeDataVH = HolderUtils.configure(viewById(R.id.passcode_erase_data),
                getString(R.string.passcode_erase_data), getString(R.string.passcode_erase_data_summary),
                maxAttemptActivated);
        HolderUtils.makeMultiLine(passcodeDataVH.bottomText, 5);
        if (passcodeEnable)
        {
            viewById(R.id.passcode_erase_data_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    maxAttemptActivated = !maxAttemptActivated;
                    passcodeDataVH.choose.setChecked(maxAttemptActivated);
                    if (maxAttemptActivated)
                    {
                        sharedPref.edit().putInt(KEY_PASSCODE_MAX_ATTEMPT, 10).apply();
                    }
                    else
                    {
                        sharedPref.edit().remove(KEY_PASSCODE_MAX_ATTEMPT).apply();
                    }
                }
            });

            passcodeDataVH.choose.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    maxAttemptActivated = !maxAttemptActivated;
                    passcodeDataVH.choose.setChecked(maxAttemptActivated);
                    if (maxAttemptActivated)
                    {
                        sharedPref.edit().putInt(KEY_PASSCODE_MAX_ATTEMPT, 10).apply();
                    }
                    else
                    {
                        sharedPref.edit().remove(KEY_PASSCODE_MAX_ATTEMPT).apply();
                    }
                }
            });
        }

        // PASSCODE ENABLE
        passcodeEnableVH = HolderUtils.configure(viewById(R.id.passcode_enable_key),
                getString(R.string.passcode_enable_title), -1, passcodeEnable);

        PasscodeConfigFeature feature = new PasscodeConfigFeature(getActivity());
        passcodeEnableVH.switcher.setEnabled(!feature.isProtected());

        passcodeEnableVH.switcher.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Disable / Enable
                if (valueHasChanged)
                {
                    valueHasChanged = false;
                }
                PassCodeDialogFragment f = null;
                if (passcodeEnable)
                {
                    f = PassCodeDialogFragment.disable();
                }
                else
                {
                    f = PassCodeDialogFragment.enable();
                }
                f.show(getActivity().getSupportFragmentManager(), PassCodeDialogFragment.TAG);
            }
        });

        disableEnableControls(passcodeEnable, (ViewGroup) viewById(R.id.passcode_erase_data_container));
        disableEnableControls(passcodeEnable, (ViewGroup) viewById(R.id.passcode_timeout_container));
        disableEnableControls(passcodeEnable, (ViewGroup) viewById(R.id.passcode_change_container));
    }

    /**
     * This method is used to refresh passcode preferences screen.
     */
    public void refresh()
    {
        if (editionEnable)
        {
            editionEnable = false;
        }
        else
        {
            valueHasChanged = true;
        }
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        passcodeEnable = sharedPref.getBoolean(KEY_PASSCODE_ENABLE, false);
        passcodeEnableVH.switcher.setChecked(passcodeEnable);
        recreate();
    }

    /**
     * Utility method to flag when the application has been activated for the
     * last time. This time is used to check passcode timeout.
     *
     * @param context
     */
    public static void updateLastActivityDisplay(Context context)
    {
        if (context instanceof WelcomeActivity || context instanceof PassCodeActivity) { return; }
        if (context instanceof MainActivity && !((MainActivity) context).hasActivateCheckPasscode()) { return; }
        updateLastActivity(context);
    }

    public static void updateLastActivity(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(KEY_PASSCODE_ACTIVATED_AT, new Date().getTime());
        editor.apply();
    }

    /**
     * Determines if the application has passcode feature enable
     *
     * @param context
     * @return true if the passcode must be prompt.
     */
    public static boolean hasPasscodeEnable(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean passcodeActivated = sharedPref.getBoolean(KEY_PASSCODE_ENABLE, false);
        if (!passcodeActivated) { return false; }

        long activationTime = sharedPref.getLong(KEY_PASSCODE_ACTIVATED_AT, DEFAULT_ACTIVATION_TIME);
        if (activationTime == DEFAULT_ACTIVATION_TIME) { return false; }

        long durationTime = Long.parseLong(sharedPref.getString(KEY_PASSCODE_TIMEOUT, DEFAULT_TIMEOUT));
        long now = new Date().getTime();

        boolean isTimeOut = (now - activationTime) > durationTime;
        if (!isTimeOut)
        {
            updateLastActivity(context);
        }

        return isTimeOut;
    }

    public static boolean hasPasscode(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(KEY_PASSCODE_ENABLE, false);
    }

    private void disableEnableControls(boolean enable, ViewGroup vg)
    {
        for (int i = 0; i < vg.getChildCount(); i++)
        {
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            child.setFocusable(enable);
            if (child instanceof ViewGroup)
            {
                disableEnableControls(enable, (ViewGroup) child);
            }
        }
        vg.setEnabled(enable);
        vg.setFocusable(enable);
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
