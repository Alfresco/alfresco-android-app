/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.favorites;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
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

public class SyncErrorDialogFragment extends DialogFragment
{
    public static final String TAG = SyncErrorDialogFragment.class.getName();

    private OnSelectionListener onFavoriteChangeListener;

    private SyncScanInfo info;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public SyncErrorDialogFragment()
    {
    }

    public static SyncErrorDialogFragment newInstance()
    {
        SyncErrorDialogFragment frag = new SyncErrorDialogFragment();
        Bundle b = new Bundle();
        frag.setArguments(b);
        return frag;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        info = SyncScanInfo.getLastSyncScanData(getActivity(), SessionUtils.getAccount(getActivity()));

        // Messages informations
        String title = info.getErrorTitleMessage(getActivity());
        int iconId = R.drawable.ic_warning;
        String message = info.getErrorMessage(getActivity());
        int positiveId = android.R.string.yes;
        int negativeId = android.R.string.no;
        onFavoriteChangeListener = downloadListener;

        Builder builder = new AlertDialog.Builder(getActivity()).setIcon(iconId).setTitle(title)
                .setMessage(Html.fromHtml(message)).setCancelable(false);

        if (info.hasError())
        {
            negativeId = R.string.ok;
        }
        else
        {
            builder.setPositiveButton(positiveId, new DialogInterface.OnClickListener()
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
        }

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
    public interface OnSelectionListener
    {
        void onPositive();

        void onNegative();
    }

    private OnSelectionListener downloadListener = new OnSelectionListener()
    {
        @Override
        public void onPositive()
        {
            SynchroManager.getInstance(getActivity()).runPendingOperationGroup();
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public void onNegative()
        {
            if (info.hasWarning() && !info.hasError())
            {
                SyncScanInfo.getLastSyncScanData(getActivity(), SessionUtils.getAccount(getActivity())).waitSync(
                        getActivity(), SessionUtils.getAccount(getActivity()));
            }
            getActivity().invalidateOptionsMenu();
        }
    };
}
