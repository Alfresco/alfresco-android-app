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

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.io.IOUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

public class ResolveConflictSyncDialogFragment extends DialogFragment
{
    public static final String TAG = ResolveConflictSyncDialogFragment.class.getName();

    private OnChangeListener onFavoriteChangeListener;

    private static final String ARGUMENT_SYNCID = "syncId";

    private Cursor syncCursor;

    private long syncId;

    private SyncFragment syncF;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public ResolveConflictSyncDialogFragment()
    {
    }

    public static ResolveConflictSyncDialogFragment newInstance(long favoriteId)
    {
        ResolveConflictSyncDialogFragment frag = new ResolveConflictSyncDialogFragment();
        Bundle b = new Bundle();
        b.putLong(ARGUMENT_SYNCID, favoriteId);
        frag.setArguments(b);
        return frag;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getArguments() == null || !getArguments().containsKey(ARGUMENT_SYNCID)) { return createErrorDialog(); }

        syncId = getArguments().getLong(ARGUMENT_SYNCID);
        syncCursor = getActivity().getContentResolver().query(SyncContentManager.getUri(syncId),
                SyncContentSchema.COLUMN_ALL, null, null, null);

        syncF = (SyncFragment) getActivity().getSupportFragmentManager().findFragmentByTag(SyncFragment.TAG);

        if (syncCursor.getCount() != 1) { return createErrorDialog(); }

        if (!syncCursor.moveToFirst()) { return createErrorDialog(); }
        // Messages informations
        int titleId = R.string.sync_error_title;
        int iconId = R.drawable.ic_application_logo;
        int messageId = R.string.sync_error_node_deleted;
        int positiveId = android.R.string.ok;
        int negativeId = -1;

        int reason = syncCursor.getInt(SyncContentSchema.COLUMN_REASON_ID);

        switch (reason)
        {
            case SyncContentStatus.REASON_NODE_DELETED:
                messageId = R.string.sync_error_node_deleted;
                positiveId = android.R.string.ok;
                onFavoriteChangeListener = deletedFavoriteListener;
                break;
            case SyncContentStatus.REASON_NO_PERMISSION:
                messageId = R.string.sync_error_no_permission;
                positiveId = R.string.sync_save_action;
                negativeId = R.string.sync_override_action;
                onFavoriteChangeListener = overrideListener;
                break;
            case SyncContentStatus.REASON_LOCAL_MODIFICATION:
                messageId = R.string.sync_error_node_local_modification;
                positiveId = android.R.string.ok;
                onFavoriteChangeListener = overrideListener;
                break;
            case SyncContentStatus.REASON_NODE_UNFAVORITED:
                messageId = R.string.sync_error_node_unfavorited;
                positiveId = R.string.sync_save_action;
                negativeId = R.string.sync_action;
                onFavoriteChangeListener = unfavoriteListener;
                break;
            default:
                onFavoriteChangeListener = deletedFavoriteListener;
                break;
        }

        String message = String.format(getString(messageId), syncCursor.getString(SyncContentSchema.COLUMN_TITLE_ID));

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).iconRes(iconId).title(titleId)
                .cancelable(false).content(Html.fromHtml(message)).positiveText(positiveId);

        if (negativeId != -1)
        {
            builder.negativeText(negativeId).callback(new MaterialDialog.ButtonCallback()
            {
                @Override
                public void onNegative(MaterialDialog dialog)
                {
                    if (onFavoriteChangeListener != null)
                    {
                        onFavoriteChangeListener.onNegative(syncCursor);
                    }
                    dialog.dismiss();
                }

                @Override
                public void onPositive(MaterialDialog dialog)
                {
                    if (onFavoriteChangeListener != null)
                    {
                        onFavoriteChangeListener.onPositive(syncCursor);
                    }
                    dialog.dismiss();
                }
            });
        }
        else
        {
            builder.callback(new MaterialDialog.ButtonCallback()
            {
                @Override
                public void onPositive(MaterialDialog dialog)
                {
                    if (onFavoriteChangeListener != null)
                    {
                        onFavoriteChangeListener.onPositive(syncCursor);
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
    public interface OnChangeListener
    {
        void onPositive(Cursor cursor);

        void onNegative(Cursor cursor);
    }

    private OnChangeListener unfavoriteListener = new OnChangeListener()
    {
        @Override
        public void onPositive(Cursor cursor)
        {
            move(cursor);
        }

        @Override
        public void onNegative(Cursor cursor)
        {
            update(cursor);
        }
    };

    private OnChangeListener deletedFavoriteListener = new OnChangeListener()
    {
        @Override
        public void onPositive(Cursor cursor)
        {
            remove(cursor);
        }

        public void onNegative(Cursor cursor)
        {
            // Do Nothing
        }
    };

    private OnChangeListener overrideListener = new OnChangeListener()
    {
        @Override
        public void onPositive(Cursor cursor)
        {
            move(cursor);
        }

        @Override
        public void onNegative(Cursor cursor)
        {
            download(cursor);
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void download(Cursor c)
    {
        String nodeIdentifier = c.getString(SyncContentSchema.COLUMN_NODE_ID_ID);

        SyncContentManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()), nodeIdentifier);

        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, SyncContentStatus.STATUS_PENDING);
        getActivity().getContentResolver().update(SyncContentManager.getUri(syncId), cValues, null, null);
        refreshSyncFragment();

        c.close();
    }

    private void update(Cursor c)
    {
        String nodeIdentifier = c.getString(SyncContentSchema.COLUMN_NODE_ID_ID);

        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_REASON, SyncContentStatus.STATUS_TO_UPDATE);
        getActivity().getContentResolver().update(SyncContentManager.getUri(syncId), cValues, null, null);

        SyncContentManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()), nodeIdentifier);
        refreshSyncFragment();

        c.close();
    }

    private void move(Cursor c)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
        getActivity().getContentResolver().update(SyncContentManager.getUri(syncId), cValues, null, null);

        // Current File
        Uri localFileUri = Uri.parse(c.getString(SyncContentSchema.COLUMN_LOCAL_URI_ID));
        File localFile = new File(localFileUri.getPath());
        String nodeIdentifier = c.getString(SyncContentSchema.COLUMN_NODE_ID_ID);

        // New File
        File parentFolder = AlfrescoStorageManager.getInstance(getActivity())
                .getDownloadFolder(SessionUtils.getAccount(getActivity()));
        File newLocalFile = new File(parentFolder, c.getString(SyncContentSchema.COLUMN_TITLE_ID));
        newLocalFile = IOUtils.createFile(newLocalFile);

        // Move to "Download"
        cValues.clear();
        if (localFile.renameTo(newLocalFile))
        {
            getActivity().getContentResolver().delete(SyncContentManager.getUri(syncId), null, null);
        }
        else
        {
            cValues.put(OperationSchema.COLUMN_STATUS, SyncContentStatus.STATUS_FAILED);
            getActivity().getContentResolver().update(SyncContentManager.getUri(syncId), cValues, null, null);
        }

        SyncContentManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()), nodeIdentifier);

        // Encrypt file if necessary
        AlfrescoStorageManager.getInstance(getActivity()).manageFile(newLocalFile);
        refreshSyncFragment();

        c.close();
    }

    private void remove(Cursor c)
    {
        getActivity().getContentResolver().delete(SyncContentManager.getUri(syncId), null, null);
        refreshSyncFragment();
        c.close();
    }

    private void refreshSyncFragment()
    {
        if (syncF != null)
        {
            syncF.onSyncNodeEvent(null);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private Dialog createErrorDialog()
    {
        // Error !
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .iconRes(R.drawable.ic_application_logo).title(R.string.sync_error_title)
                .content(R.string.error_general).positiveText(android.R.string.ok);
        return builder.show();
    }
}
