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
package org.alfresco.mobile.android.ui.fragments;

import org.alfresco.mobile.android.foundation.R;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class WaitingDialogFragment extends DialogFragment
{
    public static final String TAG = "WaitingDialogFragment";

    private int messageId = R.string.wait_message;

    private int titleId = R.string.wait_title;

    private boolean isCancelable = false;

    private static final String ARGUMENT_TITLEID = "titleId";

    private static final String ARGUMENT_MESSAGEID = "messageId";

    private static final String ARGUMENT_CANCELABLE = "cancelable";

    public WaitingDialogFragment()
    {
    }

    public static WaitingDialogFragment newInstance(int titleId, int messageId, boolean cancelable)
    {
        WaitingDialogFragment fragment = new WaitingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARGUMENT_CANCELABLE, cancelable);
        bundle.putInt(ARGUMENT_TITLEID, titleId);
        bundle.putInt(ARGUMENT_MESSAGEID, messageId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        if (getArguments() != null)
        {
            titleId = getArguments().getInt(ARGUMENT_TITLEID);
            messageId = getArguments().getInt(ARGUMENT_MESSAGEID);
            isCancelable = getArguments().getBoolean(ARGUMENT_CANCELABLE);
        }

        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(titleId);
        dialog.setMessage(getString(messageId));
        dialog.setIndeterminate(true);
        dialog.setCancelable(isCancelable);
        return dialog;
    }

    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance())
        {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
