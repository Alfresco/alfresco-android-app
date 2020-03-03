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
package org.alfresco.mobile.android.application.security;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.WaitingDialogFragment;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

public class DataProtectionUserDialogFragment extends DialogFragment
{
    public static final String TAG = DataProtectionUserDialogFragment.class.getName();

    private static final String ARGUMENT_FIRST_TIME = "firstTime";

    private onDataProtectionListener onDataProtectionListener;

    private SharedPreferences prefs;

    private boolean firstTime = false;

    private boolean checked = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public DataProtectionUserDialogFragment()
    {
    }

    public static DataProtectionUserDialogFragment newInstance(boolean firstTime)
    {
        DataProtectionUserDialogFragment frag = new DataProtectionUserDialogFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARGUMENT_FIRST_TIME, firstTime);
        frag.setArguments(b);
        return frag;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getArguments() != null && getArguments().containsKey(ARGUMENT_FIRST_TIME))
        {
            firstTime = getArguments().getBoolean(ARGUMENT_FIRST_TIME);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        checked = DataProtectionManager.getInstance(getActivity()).hasDataProtectionEnable();

        // Messages informations
        int titleId = R.string.data_protection;
        int iconId = R.drawable.ic_application_logo;
        int messageId = (firstTime) ? R.string.data_protection_blurb
                : (checked ? R.string.unprotect_question : R.string.protect_question);
        int positiveId = android.R.string.yes;
        int negativeId = android.R.string.no;
        onDataProtectionListener = dataProtectionListener;

        String message = getString(messageId);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).iconRes(iconId).title(titleId)
                .content(Html.fromHtml(message)).positiveText(positiveId).negativeText(negativeId)
                .callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        if (onDataProtectionListener != null)
                        {
                            onDataProtectionListener.onPositive();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog)
                    {
                        if (onDataProtectionListener != null)
                        {
                            onDataProtectionListener.onNegative();
                        }
                        dialog.dismiss();
                    }
                });
        return builder.show();
    }

    @Override
    public void onResume()
    {
        if (getDialog() != null)
        {
            TextView messageText = ((MaterialDialog) getDialog()).getContentView();
            if (messageText != null)
            {
                messageText.setGravity(Gravity.CENTER);
            }
            getDialog().show();
        }
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTENERS
    // ///////////////////////////////////////////////////////////////////////////
    public interface onDataProtectionListener
    {
        void onPositive();

        void onNegative();
    }

    private onDataProtectionListener dataProtectionListener = new onDataProtectionListener()
    {
        @Override
        public void onPositive()
        {
            int localMessageId;
            if (firstTime)
            {
                DataProtectionManager.getInstance(getActivity()).setDataProtectionUserRequested(true);
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).apply();
                localMessageId = R.string.encryption_title;
            }
            else
            {
                if (checked)
                {
                    localMessageId = R.string.decryption_title;
                }
                else
                {
                    localMessageId = R.string.encryption_title;
                }
            }

            // Display Dialog
            if (getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
            {
                WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(R.string.data_protection,
                        localMessageId, false);
                dialog.show(getActivity().getSupportFragmentManager(), WaitingDialogFragment.TAG);
            }

            // Execute encryption / decryption
            if (firstTime)
            {
                DataProtectionManager.getInstance(getActivity()).encrypt(SessionUtils.getAccount(getActivity()));
            }
            else
            {
                if (checked)
                {
                    DataProtectionManager.getInstance(getActivity()).decrypt(SessionUtils.getAccount(getActivity()));
                }
                else
                {
                    DataProtectionManager.getInstance(getActivity()).encrypt(SessionUtils.getAccount(getActivity()));
                }
            }
        }

        @Override
        public void onNegative()
        {
            if (firstTime)
            {
                DataProtectionManager.getInstance(getActivity()).setDataProtectionUserRequested(true);
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).apply();
            }
        }
    };
}
