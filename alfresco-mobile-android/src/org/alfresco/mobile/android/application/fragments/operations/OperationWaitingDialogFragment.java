/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.operations;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationContentProvider;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class OperationWaitingDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String TAG = OperationWaitingDialogFragment.class.getName();

    private String message = "";

    private String title = "";

    private Node parent;

    private static final String PARAM_TITLEID = "titleId";

    private static final String PARAM_MESSAGEID = "messageId";

    private static final String PARAM_NODEID = "nodeId";

    private static final String PARAM_ICONID = "iconId";

    private static final String PARAM_TYPEID = "typeId";

    private static final String PARAM_SIZE = "nbItems";
    
    private static final String PARAM_FINISH = "nbItems";

    private boolean canDismiss = false;

    private Integer iconId;

    private Integer operationType;

    private Integer nbItems;

    public OperationWaitingDialogFragment()
    {
    }

    public static OperationWaitingDialogFragment newInstance(int operationType, int iconId, String title,
            String message, Node parent, int nbItems)
    {
        OperationWaitingDialogFragment fragment = new OperationWaitingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_TYPEID, operationType);
        bundle.putInt(PARAM_ICONID, iconId);
        bundle.putInt(PARAM_SIZE, nbItems);
        bundle.putString(PARAM_TITLEID, title);
        bundle.putString(PARAM_MESSAGEID, message);
        bundle.putParcelable(PARAM_NODEID, parent);
        fragment.setArguments(bundle);
        return fragment;
    }
    
    public static OperationWaitingDialogFragment newInstance(int operationType, int iconId, String title,
            String message, Node parent, int nbItems, boolean finishActivity)
    {
        OperationWaitingDialogFragment fragment = new OperationWaitingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_TYPEID, operationType);
        bundle.putInt(PARAM_ICONID, iconId);
        bundle.putInt(PARAM_SIZE, nbItems);
        bundle.putString(PARAM_TITLEID, title);
        bundle.putString(PARAM_MESSAGEID, message);
        bundle.putParcelable(PARAM_NODEID, parent);
        fragment.setArguments(bundle);
        return fragment;
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        setRetainInstance(true);
        if (getArguments() != null)
        {
            operationType = getArguments().getInt(PARAM_TYPEID);
            iconId = getArguments().getInt(PARAM_ICONID);
            title = getArguments().getString(PARAM_TITLEID);
            message = getArguments().getString(PARAM_MESSAGEID);
            parent = getArguments().getParcelable(PARAM_NODEID);
            nbItems = getArguments().getInt(PARAM_SIZE);
        }

        ProgressDialog dialog = new ProgressDialog(getActivity());
        if (iconId == 0)
        {
            iconId = R.drawable.ic_alfresco_logo;
        }
        dialog.setIcon(iconId);
        dialog.setTitle(title);
        if (message == null)
        {
            message = getString(R.string.waiting_operations);
        }
        dialog.setMessage(message);
        boolean indeterminate = true;
        if (nbItems > 0)
        {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.setMax(nbItems);
            indeterminate = false;
        }
        else
        {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        getActivity().getLoaderManager().restartLoader(this.hashCode(), null, this);

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

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        BatchOperationManager.getInstance(getActivity()).forceStop();
        super.onDismiss(dialog);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri baseUri = BatchOperationContentProvider.CONTENT_URI;

        if (parent != null)
        {
            return new CursorLoader(getActivity(), baseUri, BatchOperationSchema.COLUMN_ALL,
                    BatchOperationSchema.COLUMN_PARENT_ID + "=\"" + parent.getIdentifier() + "\" AND "
                            + BatchOperationSchema.COLUMN_REQUEST_TYPE + "=" + operationType, null, null);
        }
        else
        {
            return new CursorLoader(getActivity(), baseUri, BatchOperationSchema.COLUMN_ALL,
                    BatchOperationSchema.COLUMN_REQUEST_TYPE + "=" + operationType, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        if (getDialog() == null) { return; }
        int progress = 0;
        while (cursor.moveToNext())
        {
            if (cursor.getInt(BatchOperationSchema.COLUMN_STATUS_ID) == Operation.STATUS_SUCCESSFUL)
            {
                progress++;
            }
        }
        ((ProgressDialog) getDialog()).setProgress(progress);
        if (canDismiss)
        {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(IntentIntegrator.ACTION_OPERATIONS_COMPLETED);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        // DO Nothing
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (receiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
    }

    @Override
    public void onResume()
    {
        setCancelable(false);
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_OPERATIONS_COMPLETED);
        if (receiver == null)
        {
            receiver = new OperationReceiver();
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
    }

    private OperationReceiver receiver;

    public class OperationReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (canDismiss && IntentIntegrator.ACTION_OPERATIONS_COMPLETED.equals(intent.getAction()))
            {
                dismiss();
                return;
            }

            if (IntentIntegrator.ACTION_OPERATIONS_COMPLETED.equals(intent.getAction()))
            {
                canDismiss = true;
                dismiss();
            }
        }
    }
}
