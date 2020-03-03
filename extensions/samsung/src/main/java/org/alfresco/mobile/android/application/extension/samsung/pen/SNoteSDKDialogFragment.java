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
package org.alfresco.mobile.android.application.extension.samsung.pen;

import org.alfresco.mobile.android.application.extension.samsung.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;

public class SNoteSDKDialogFragment extends DialogFragment
{
    public static final String TAG = SNoteSDKDialogFragment.class.getName();

    private static final String PARAM_SDK_ERROR = "sdkError";

    private int error;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public SNoteSDKDialogFragment()
    {
    }

    public static SNoteSDKDialogFragment newInstance(int errType)
    {
        SNoteSDKDialogFragment frag = new SNoteSDKDialogFragment();
        Bundle b = new Bundle();
        b.putInt(PARAM_SDK_ERROR, errType);
        frag.setArguments(b);
        return frag;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getArguments() != null && getArguments().containsKey(PARAM_SDK_ERROR))
        {
            error = getArguments().getInt(PARAM_SDK_ERROR);
        }
        // Messages informations
        int titleId = R.string.error_sdk_dialog;
        int iconId = R.drawable.ic_application_icon;
        int messageId = R.string.error_sdk_unsupported;
        switch (error)
        {
            case SsdkUnsupportedException.DEVICE_NOT_SUPPORTED:
                messageId = R.string.error_sdk_unsupported;
                break;
            case SsdkUnsupportedException.LIBRARY_NOT_INSTALLED:
                messageId = R.string.error_sdk_library_not_installed;
                break;
            case SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED:
                messageId = R.string.error_sdk_library_update_required;
                break;

            case SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED:
                messageId = R.string.error_sdk_library_update_recommended;
                break;

            default:
                break;
        }
        setCancelable(false);

        int positiveId = android.R.string.ok;
        int negativeId = android.R.string.cancel;

        String message = getString(messageId);

        Builder builder = new AlertDialog.Builder(getActivity()).setIcon(iconId).setTitle(titleId)
                .setMessage(Html.fromHtml(message)).setCancelable(false)
                .setPositiveButton(positiveId, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Uri uri = Uri.parse("market://details?id=" + Spen.SPEN_NATIVE_PACKAGE_NAME);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getActivity().startActivity(intent);

                        dialog.dismiss();
                        getActivity().finish();
                    }
                }).setNegativeButton(negativeId, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        getActivity().finish();
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
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().show();
        }
        super.onResume();
    }

}
