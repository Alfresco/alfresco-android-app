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
package org.alfresco.mobile.android.application.fragments.sync;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncScanInfo;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

public class ErrorSyncDialogFragment extends DialogFragment
{
    public static final String TAG = ErrorSyncDialogFragment.class.getName();

    private OnSelectionListener onFavoriteChangeListener;

    private SyncScanInfo info;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public ErrorSyncDialogFragment()
    {
    }

    public static ErrorSyncDialogFragment newInstance()
    {
        ErrorSyncDialogFragment frag = new ErrorSyncDialogFragment();
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

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).iconRes(iconId).title(title)
                .content(Html.fromHtml(message)).cancelable(false);

        if (info.hasError())
        {
            builder.negativeText(R.string.ok).callback(new MaterialDialog.ButtonCallback()
            {
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
        }
        else
        {
            builder.positiveText(positiveId).negativeText(negativeId).callback(new MaterialDialog.ButtonCallback()
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
        }

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
            SyncContentManager.getInstance(getActivity())
                    .runPendingOperationGroup(SessionUtils.getAccount(getActivity()));
            getActivity().invalidateOptionsMenu();

            if (getFragmentManager().findFragmentByTag(SyncFragment.TAG) != null)
            {
                ((SyncFragment) getFragmentManager().findFragmentByTag(SyncFragment.TAG)).awaitNextSync();
            }
        }

        @Override
        public void onNegative()
        {
            if (info.hasWarning() && !info.hasError())
            {
                SyncScanInfo.getLastSyncScanData(getActivity(), SessionUtils.getAccount(getActivity()))
                        .waitSync(getActivity(), SessionUtils.getAccount(getActivity()));
            }
            getActivity().invalidateOptionsMenu();
        }
    };
}
