/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.fragments.sync;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.io.IOUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncStatus;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

public class ResolveConflictSyncDialogFragment extends DialogFragment
{
    public static final String TAG = ResolveConflictSyncDialogFragment.class.getName();

    private OnChangeListener onFavoriteChangeListener;

    private static final String ARGUMENT_FAVORITEID = "favoriteId";

    private Cursor favoriteCursor;

    private long favoriteId;

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
        b.putLong(ARGUMENT_FAVORITEID, favoriteId);
        frag.setArguments(b);
        return frag;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getArguments() == null || !getArguments().containsKey(ARGUMENT_FAVORITEID)) { return createErrorDialog(); }

        favoriteId = getArguments().getLong(ARGUMENT_FAVORITEID);
        favoriteCursor = getActivity().getContentResolver().query(FavoritesSyncManager.getUri(favoriteId),
                FavoritesSyncSchema.COLUMN_ALL, null, null, null);

        if (favoriteCursor.getCount() != 1) { return createErrorDialog(); }

        if (!favoriteCursor.moveToFirst()) { return createErrorDialog(); }
        // Messages informations
        int titleId = R.string.sync_error_title;
        int iconId = R.drawable.ic_application_logo;
        int messageId = R.string.sync_error_node_unfavorited;
        int positiveId = android.R.string.yes;
        int negativeId = -1;

        int reason = favoriteCursor.getInt(FavoritesSyncSchema.COLUMN_REASON_ID);

        switch (reason)
        {
            case FavoriteSyncStatus.REASON_NODE_DELETED:
                messageId = R.string.sync_error_node_deleted;
                positiveId = android.R.string.ok;
                onFavoriteChangeListener = deletedFavoriteListener;
                break;
            case FavoriteSyncStatus.REASON_NO_PERMISSION:
                messageId = R.string.sync_error_no_permission;
                positiveId = R.string.sync_save_action;
                negativeId = R.string.sync_override_action;
                onFavoriteChangeListener = overrideListener;
                break;
            case FavoriteSyncStatus.REASON_LOCAL_MODIFICATION:
            case FavoriteSyncStatus.REASON_NODE_UNFAVORITED:
                messageId = R.string.sync_error_node_unfavorited;
                positiveId = R.string.sync_save_action;
                negativeId = R.string.sync_action;
                onFavoriteChangeListener = unfavoriteListener;
                break;
            default:
                break;
        }

        String message = String.format(getString(messageId),
                favoriteCursor.getString(FavoritesSyncSchema.COLUMN_TITLE_ID));

        Builder builder = new Builder(getActivity()).setIcon(iconId).setTitle(titleId)
                .setMessage(Html.fromHtml(message)).setCancelable(false)
                .setPositiveButton(positiveId, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        if (onFavoriteChangeListener != null)
                        {
                            onFavoriteChangeListener.onPositive(favoriteCursor);
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
                        onFavoriteChangeListener.onNegative(favoriteCursor);
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
        String nodeIdentifier = c.getString(FavoritesSyncSchema.COLUMN_NODE_ID_ID);

        FavoritesSyncManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()), nodeIdentifier);

        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, FavoriteSyncStatus.STATUS_PENDING);
        getActivity().getContentResolver().update(FavoritesSyncManager.getUri(favoriteId), cValues, null, null);

        c.close();
    }

    private void update(Cursor c)
    {
        String nodeIdentifier = c.getString(FavoritesSyncSchema.COLUMN_NODE_ID_ID);

        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_REASON, FavoriteSyncStatus.STATUS_TO_UPDATE);
        getActivity().getContentResolver().update(FavoritesSyncManager.getUri(favoriteId), cValues, null, null);

        FavoritesSyncManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()), nodeIdentifier);

        c.close();
    }

    private void move(Cursor c)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
        getActivity().getContentResolver().update(FavoritesSyncManager.getUri(favoriteId), cValues, null, null);

        // Current File
        Uri localFileUri = Uri.parse(c.getString(FavoritesSyncSchema.COLUMN_LOCAL_URI_ID));
        File localFile = new File(localFileUri.getPath());

        // New File
        File parentFolder = AlfrescoStorageManager.getInstance(getActivity()).getDownloadFolder(
                SessionUtils.getAccount(getActivity()));
        File newLocalFile = new File(parentFolder, c.getString(FavoritesSyncSchema.COLUMN_TITLE_ID));
        newLocalFile = IOUtils.createFile(newLocalFile);

        // Move to "Download"
        cValues.clear();
        if (localFile.renameTo(newLocalFile))
        {
            getActivity().getContentResolver().delete(FavoritesSyncManager.getUri(favoriteId), null, null);
        }
        else
        {
            cValues.put(OperationSchema.COLUMN_STATUS, FavoriteSyncStatus.STATUS_FAILED);
            getActivity().getContentResolver().update(FavoritesSyncManager.getUri(favoriteId), cValues, null, null);
        }

        // Encrypt file if necessary
        AlfrescoStorageManager.getInstance(getActivity()).manageFile(newLocalFile);

        c.close();
    }

    private void remove(Cursor c)
    {
        getActivity().getContentResolver().delete(FavoritesSyncManager.getUri(favoriteId), null, null);
        c.close();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private Dialog createErrorDialog()
    {
        // Error !
        Builder builder = new Builder(getActivity()).setIcon(R.drawable.ic_application_logo)
                .setTitle(R.string.sync_error_title).setMessage(R.string.error_general);
        return builder.create();
    }
}
