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

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

public class SimpleAlertDialogFragment extends DialogFragment
{

    public static final String TAG = "SimpleAlertDialogFragment";

    /** Associated Value must be a String id (int value). */
    public static final String ARGUMENT_TITLE = "alertDialogFragment_TitleId";

    /** Associated Value must be a Drawable id (int value). */
    public static final String ARGUMENT_ICON = "alertDialogFragment_IconId";

    /** Associated Value must be a String id (int value). */
    public static final String ARGUMENT_POSITIVE_BUTTON = "alertDialogFragment_PositiveId";

    /** Associated Value must be a String id (int value). */
    public static final String ARGUMENT_MESSAGE = "alertDialogFragment_MessageId";

    /** Associated Value must be a String (String value). */
    public static final String ARGUMENT_MESSAGE_STRING = "alertDialogFragment_MessageString";

    public static SimpleAlertDialogFragment newInstance(Bundle b)
    {
        SimpleAlertDialogFragment frag = new SimpleAlertDialogFragment();
        if (b != null)
        {
            frag.setArguments(b);
        }
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getArguments() == null) { return null; }

        int titleId = getArguments().getInt(ARGUMENT_TITLE);
        int iconId = getArguments().getInt(ARGUMENT_ICON);
        int positiveId = getArguments().getInt(ARGUMENT_POSITIVE_BUTTON);
        String message = "";
        if (getArguments().containsKey(ARGUMENT_MESSAGE))
        {
            message = getString(getArguments().getInt(ARGUMENT_MESSAGE));
        }
        else if (getArguments().containsKey(ARGUMENT_MESSAGE_STRING))
        {
            message = getArguments().getString(ARGUMENT_MESSAGE_STRING);
        }

        Builder builder = new Builder(getActivity()).setIcon(iconId).setTitle(titleId)
                .setMessage(Html.fromHtml(message)).setPositiveButton(positiveId, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        dialog.dismiss();
                    }
                });

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
}