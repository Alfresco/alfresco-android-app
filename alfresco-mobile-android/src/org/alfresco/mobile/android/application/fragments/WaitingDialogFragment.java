/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments;

import org.alfresco.mobile.android.application.R;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

public class WaitingDialogFragment extends DialogFragment
{
    public static final String TAG = "WaitingDialogFragment";

    private int messageId = R.string.wait_message;

    private int titleId = R.string.wait_title;

    private boolean isCancelable = false;

    private static final String PARAM_TITLEID = "titleId";

    private static final String PARAM_MESSAGEID = "messageId";

    private static final String PARAM_CANCELABLE = "cancelable";

    public WaitingDialogFragment()
    {
    }

    public static WaitingDialogFragment newInstance(int titleId, int messageId, boolean cancelable)
    {
        WaitingDialogFragment fragment = new WaitingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(PARAM_CANCELABLE, cancelable);
        bundle.putInt(PARAM_TITLEID, titleId);
        bundle.putInt(PARAM_MESSAGEID, messageId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        if (getArguments() != null)
        {
            titleId = getArguments().getInt(PARAM_TITLEID);
            messageId = getArguments().getInt(PARAM_MESSAGEID);
            isCancelable = getArguments().getBoolean(PARAM_CANCELABLE);
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
