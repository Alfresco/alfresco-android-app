/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.security;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

public class DataProtectionUserDialogFragment extends DialogFragment
{
    public static final String TAG = DataProtectionUserDialogFragment.class.getName();

    private static final String PARAM_FIRST_TIME = "firstTime";

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
        b.putBoolean(PARAM_FIRST_TIME, firstTime);
        frag.setArguments(b);
        return frag;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getArguments() != null && getArguments().containsKey(PARAM_FIRST_TIME))
        {
            firstTime = getArguments().getBoolean(PARAM_FIRST_TIME);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        checked = prefs.getBoolean(GeneralPreferences.PRIVATE_FOLDERS, false);

        // Messages informations
        int titleId = R.string.data_protection;
        int iconId = R.drawable.ic_alfresco_logo;
        int messageId = (firstTime) ? R.string.data_protection_blurb : (checked ? R.string.unprotect_question
                : R.string.protect_question);
        int positiveId = android.R.string.yes;
        int negativeId = android.R.string.no;
        onDataProtectionListener = dataProtectionListener;

        String message = getString(messageId);

        Builder builder = new AlertDialog.Builder(getActivity()).setIcon(iconId).setTitle(titleId)
                .setMessage(Html.fromHtml(message)).setCancelable(false)
                .setPositiveButton(positiveId, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        if (onDataProtectionListener != null)
                        {
                            onDataProtectionListener.onPositive();
                        }
                        dialog.dismiss();
                    }
                });

        if (negativeId != -1)
        {
            builder.setNegativeButton(negativeId, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    if (onDataProtectionListener != null)
                    {
                        onDataProtectionListener.onNegative();
                    }
                    dialog.dismiss();
                }
            });
        }

        return builder.create();
    }

    @Override
    public void onResume()
    {
        if (getDialog() != null)
        {
            TextView messageText = (TextView) getDialog().findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
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
            int localMessageId  = R.string.decryption_title;
            if (firstTime)
            {
                prefs.edit().putBoolean(GeneralPreferences.ENCRYPTION_USER_INTERACTION, true).commit();
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();
                DataProtectionManager.getInstance(getActivity()).encrypt(SessionUtils.getAccount(getActivity()));
                localMessageId = R.string.encryption_title;
            }
            else
            {
                if (checked)
                {
                    DataProtectionManager.getInstance(getActivity()).decrypt(SessionUtils.getAccount(getActivity()));
                    localMessageId = R.string.decryption_title;
                }
                else
                {
                    DataProtectionManager.getInstance(getActivity()).encrypt(SessionUtils.getAccount(getActivity()));
                    localMessageId = R.string.encryption_title;
                }
            }
            if (getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
            {
                WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(R.string.data_protection, localMessageId, false);
                dialog.show(getActivity().getFragmentManager(), WaitingDialogFragment.TAG);
            }
        }

        @Override
        public void onNegative()
        {
            if (firstTime)
            {
                prefs.edit().putBoolean(GeneralPreferences.ENCRYPTION_USER_INTERACTION, true).commit();
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();
            }
        }
    };
}
