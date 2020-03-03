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
package org.alfresco.mobile.android.application.fragments.utils;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.download.DownloadRequest;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

public class ResolveNamingConflictFragment extends DialogFragment
{
    public static final String TAG = ResolveNamingConflictFragment.class.getName();

    private OnNameChangeListener onFavoriteChangeListener;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public ResolveNamingConflictFragment()
    {
    }

    public static ResolveNamingConflictFragment newInstance(Folder folder, Document document)
    {
        ResolveNamingConflictFragment frag = new ResolveNamingConflictFragment();
        Bundle b = new Bundle();
        b.putParcelable(PrivateIntent.EXTRA_DOCUMENT, document);
        b.putParcelable(PrivateIntent.EXTRA_FOLDER, folder);
        frag.setArguments(b);
        return frag;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Messages informations
        int titleId = R.string.error_duplicate_title;
        int messageId = R.string.error_duplicate_description;
        int positiveId = android.R.string.yes;
        int negativeId = android.R.string.no;
        onFavoriteChangeListener = downloadListener;

        String message = getString(messageId);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .iconRes(R.drawable.ic_application_logo).title(titleId).content(Html.fromHtml(message))
                .positiveText(positiveId).negativeText(negativeId).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        if (onFavoriteChangeListener != null)
                        {
                            onFavoriteChangeListener.onPositive();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog)
                    {
                        if (onFavoriteChangeListener != null)
                        {
                            onFavoriteChangeListener.onNegative();
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
    public interface OnNameChangeListener
    {
        void onPositive();

        void onNegative();
    }

    private OnNameChangeListener downloadListener = new OnNameChangeListener()
    {
        @Override
        public void onPositive()
        {
            download();
        }

        @Override
        public void onNegative()
        {
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void download()
    {
        if (getArguments() != null && getArguments().containsKey(PrivateIntent.EXTRA_DOCUMENT)
                && getArguments().containsKey(PrivateIntent.EXTRA_FOLDER))
        {
            Operator.with(getActivity())
                    .load(new DownloadRequest.Builder((Folder) getArguments().getParcelable(PrivateIntent.EXTRA_FOLDER),
                            (Document) getArguments().getParcelable(PrivateIntent.EXTRA_DOCUMENT), true));
        }
    }
}
