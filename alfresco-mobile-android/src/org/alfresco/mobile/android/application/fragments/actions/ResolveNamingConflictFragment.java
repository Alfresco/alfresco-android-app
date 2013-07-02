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
package org.alfresco.mobile.android.application.fragments.actions;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.node.download.DownloadRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

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
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, document);
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, folder);
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
        int iconId = R.drawable.ic_download_dark;
        int messageId = R.string.error_duplicate_description;
        int positiveId = android.R.string.yes;
        int negativeId = android.R.string.no;
        onFavoriteChangeListener = downloadListener;

        String message = getString(messageId);

        Builder builder = new AlertDialog.Builder(getActivity()).setIcon(iconId).setTitle(titleId)
                .setMessage(Html.fromHtml(message)).setCancelable(false)
                .setPositiveButton(positiveId, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        if (onFavoriteChangeListener != null)
                        {
                            onFavoriteChangeListener.onPositive();
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
                    if (onFavoriteChangeListener != null)
                    {
                        onFavoriteChangeListener.onNegative();
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
        if (getArguments() != null && getArguments().containsKey(IntentIntegrator.EXTRA_DOCUMENT)
                && getArguments().containsKey(IntentIntegrator.EXTRA_FOLDER))
        {
            OperationsRequestGroup group = new OperationsRequestGroup(getActivity(),
                    SessionUtils.getAccount(getActivity()));
            group.enqueue(new DownloadRequest((Folder) getArguments().getParcelable(IntentIntegrator.EXTRA_FOLDER),
                    (Document) getArguments().getParcelable(IntentIntegrator.EXTRA_DOCUMENT), true));
            BatchOperationManager.getInstance(getActivity()).enqueue(group);
        }
    }
}
