package org.alfresco.mobile.android.application.fragments.favorites;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadRequest;
import org.alfresco.mobile.android.application.operations.sync.node.update.SyncUpdateRequest;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.AlertDialog;
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

public class ResolveSyncConflictFragment extends DialogFragment
{
    public static final String TAG = ResolveSyncConflictFragment.class.getName();

    private OnChangeListener onFavoriteChangeListener;

    private static final String PARAM_FAVORITEID = "favoriteId";

    private Cursor favoriteCursor;

    private long favoriteId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public ResolveSyncConflictFragment()
    {
    }

    public static ResolveSyncConflictFragment newInstance(long favoriteId)
    {
        ResolveSyncConflictFragment frag = new ResolveSyncConflictFragment();
        Bundle b = new Bundle();
        b.putLong(PARAM_FAVORITEID, favoriteId);
        frag.setArguments(b);
        return frag;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getArguments() == null || !getArguments().containsKey(PARAM_FAVORITEID)) { return createErrorDialog(); }

        favoriteId = getArguments().getLong(PARAM_FAVORITEID);
        favoriteCursor = getActivity().getContentResolver().query(SynchroManager.getUri(favoriteId),
                SynchroSchema.COLUMN_ALL, null, null, null);

        if (favoriteCursor.getCount() != 1) { return createErrorDialog(); }

        if (!favoriteCursor.moveToFirst()) { return createErrorDialog(); }
        // Messages informations
        int titleId = R.string.sync_error_title;
        int iconId = R.drawable.ic_alfresco_logo;
        int messageId = R.string.sync_error_node_unfavorited;
        int positiveId = android.R.string.yes;
        int negativeId = -1;

        int reason = favoriteCursor.getInt(SynchroSchema.COLUMN_REASON_ID);

        switch (reason)
        {
            case SyncOperation.REASON_NODE_DELETED:
                messageId = R.string.sync_error_node_deleted;
                positiveId = android.R.string.ok;
                onFavoriteChangeListener = deletedFavoriteListener;
                break;
            case SyncOperation.REASON_NO_PERMISSION:
                messageId = R.string.sync_error_no_permission;
                positiveId = R.string.sync_save_action;
                negativeId = R.string.sync_override_action;
                onFavoriteChangeListener = overrideListener;
                break;
            case SyncOperation.REASON_LOCAL_MODIFICATION:
            case SyncOperation.REASON_NODE_UNFAVORITED:
                messageId = R.string.sync_error_node_unfavorited;
                positiveId = R.string.sync_save_action;
                negativeId = R.string.sync_action;
                onFavoriteChangeListener = unfavoriteListener;
                break;
            default:
                break;
        }

        String message = String.format(getString(messageId),
                favoriteCursor.getString(SynchroSchema.COLUMN_TITLE_ID));

        Builder builder = new AlertDialog.Builder(getActivity()).setIcon(iconId).setTitle(titleId)
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
        
        if (negativeId != -1){
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
            //Do Nothing
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
        String nodeIdentifier = c.getString(SynchroSchema.COLUMN_NODE_ID_ID);
        String parentIdentifier = c.getString(SynchroSchema.COLUMN_PARENT_ID_ID);
        String nodeName = c.getString(SynchroSchema.COLUMN_TITLE_ID);

        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        SyncDownloadRequest dl = new SyncDownloadRequest(parentIdentifier, nodeIdentifier);
        dl.setNotificationUri(SynchroManager.getUri(favoriteId));
        dl.setNotificationTitle(nodeName);
        group.enqueue(dl.setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
        SynchroManager.getInstance(getActivity()).enqueue(group);

        ContentValues cValues = new ContentValues();
        cValues.put(BatchOperationSchema.COLUMN_STATUS, SyncOperation.STATUS_PENDING);
        getActivity().getContentResolver().update(SynchroManager.getUri(favoriteId), cValues, null, null);

        c.close();
    }
    
    private void update(Cursor c)
    {

        String parentIdentifier = c.getString(SynchroSchema.COLUMN_PARENT_ID_ID);
        String nodeIdentifier = c.getString(SynchroSchema.COLUMN_NODE_ID_ID);
        String nodeName = c.getString(SynchroSchema.COLUMN_TITLE_ID);
        Uri localFileUri = Uri.parse(c.getString(SynchroSchema.COLUMN_LOCAL_URI_ID));
        File localFile = new File(localFileUri.getPath());

        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        SyncUpdateRequest updateRequest = new SyncUpdateRequest(parentIdentifier, nodeIdentifier, nodeName,
                new ContentFileProgressImpl(localFile), true);
        updateRequest.setNotificationUri(SynchroManager.getUri(favoriteId));
        group.enqueue(updateRequest);
        SynchroManager.getInstance(getActivity()).enqueue(group);

        ContentValues cValues = new ContentValues();
        cValues.put(BatchOperationSchema.COLUMN_STATUS, SyncOperation.STATUS_HIDDEN);
        getActivity().getContentResolver().update(SynchroManager.getUri(favoriteId), cValues, null, null);

        c.close();
    }

    private void move(Cursor c)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(BatchOperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
        getActivity().getContentResolver().update(SynchroManager.getUri(favoriteId), cValues, null, null);

        // Current File
        Uri localFileUri = Uri.parse(c.getString(SynchroSchema.COLUMN_LOCAL_URI_ID));
        File localFile = new File(localFileUri.getPath());

        // New File
        File parentFolder = StorageManager.getDownloadFolder(getActivity(), SessionUtils.getAccount(getActivity()));
        File newLocalFile = new File(parentFolder, c.getString(SynchroSchema.COLUMN_TITLE_ID));
        newLocalFile = IOUtils.createFile(newLocalFile);
        
        // Move to "Download"
        cValues.clear();
        if (localFile.renameTo(newLocalFile))
        {
            getActivity().getContentResolver().delete(SynchroManager.getUri(favoriteId), null, null);
        }
        else
        {
            cValues.put(BatchOperationSchema.COLUMN_STATUS, SyncOperation.STATUS_FAILED);
            getActivity().getContentResolver().update(SynchroManager.getUri(favoriteId), cValues, null, null);
        }
        
        // Encrypt file if necessary
        StorageManager.manageFile(getActivity(), newLocalFile);

        c.close();
    }

    private void remove(Cursor c)
    {
        getActivity().getContentResolver().delete(SynchroManager.getUri(favoriteId), null, null);
        c.close();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private Dialog createErrorDialog()
    {
        // Error !
        Builder builder = new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_alfresco_logo)
                .setTitle(R.string.sync_error_title).setMessage(R.string.error_general);
        return builder.create();
    }
}
