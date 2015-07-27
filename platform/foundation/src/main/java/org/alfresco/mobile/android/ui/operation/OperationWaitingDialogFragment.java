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
package org.alfresco.mobile.android.ui.operation;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.BatchOperationEvent;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsContentProvider;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;

import com.squareup.otto.Subscribe;

public class OperationWaitingDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String TAG = OperationWaitingDialogFragment.class.getName();

    private static final String ACTION_OPERATIONS_COMPLETED = "org.alfresco.mobile.android.intent.ACTION_OPERATIONS_COMPLETED";

    private String message = "";

    private String title = "";

    private Node parent;

    private static final String ARGUMENT_TITLEID = "titleId";

    private static final String ARGUMENT_MESSAGEID = "messageId";

    private static final String ARGUMENT_NODEID = "nodeId";

    private static final String ARGUMENT_OPERATIONID = "operationId";

    private static final String ARGUMENT_ICONID = "iconId";

    private static final String ARGUMENT_TYPEID = "typeId";

    private static final String ARGUMENT_INTENTID = "intentId";

    private static final String ARGUMENT_SIZE = "nbItems";

    private static final String ARGUMENT_FINISH = "nbItems";

    private boolean canDismiss = false;

    private Integer iconId;

    private Integer operationType;

    private Integer nbItems;

    private OperationReceiver receiver;

    private String intentId;

    private String operationId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public OperationWaitingDialogFragment()
    {
    }

    public static OperationWaitingDialogFragment newInstance(String intentActionDismiss, int operationType, int iconId,
            String title, String message, Node parent, int nbItems)
    {
        OperationWaitingDialogFragment fragment = new OperationWaitingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_INTENTID, intentActionDismiss);
        bundle.putInt(ARGUMENT_TYPEID, operationType);
        bundle.putInt(ARGUMENT_ICONID, iconId);
        bundle.putInt(ARGUMENT_SIZE, nbItems);
        bundle.putString(ARGUMENT_TITLEID, title);
        bundle.putString(ARGUMENT_MESSAGEID, message);
        bundle.putParcelable(ARGUMENT_NODEID, parent);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static OperationWaitingDialogFragment newInstance(int operationType, int iconId, String title,
            String message, Node parent, int nbItems, String operationId)
    {
        OperationWaitingDialogFragment fragment = new OperationWaitingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARGUMENT_TYPEID, operationType);
        bundle.putInt(ARGUMENT_ICONID, iconId);
        bundle.putInt(ARGUMENT_SIZE, nbItems);
        bundle.putString(ARGUMENT_TITLEID, title);
        bundle.putString(ARGUMENT_MESSAGEID, message);
        bundle.putParcelable(ARGUMENT_NODEID, parent);
        bundle.putString(ARGUMENT_OPERATIONID, operationId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static OperationWaitingDialogFragment newInstance(int operationType, int iconId, String title,
            String message, Node parent, int nbItems, boolean finishActivity)
    {
        OperationWaitingDialogFragment fragment = new OperationWaitingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARGUMENT_TYPEID, operationType);
        bundle.putInt(ARGUMENT_ICONID, iconId);
        bundle.putInt(ARGUMENT_SIZE, nbItems);
        bundle.putString(ARGUMENT_TITLEID, title);
        bundle.putString(ARGUMENT_MESSAGEID, message);
        bundle.putParcelable(ARGUMENT_NODEID, parent);
        fragment.setArguments(bundle);
        return fragment;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        if (getArguments() != null)
        {
            operationType = getArguments().getInt(ARGUMENT_TYPEID);
            intentId = getArguments().getString(ARGUMENT_INTENTID);
            iconId = getArguments().getInt(ARGUMENT_ICONID);
            title = getArguments().getString(ARGUMENT_TITLEID);
            message = getArguments().getString(ARGUMENT_MESSAGEID);
            parent = getArguments().getParcelable(ARGUMENT_NODEID);
            nbItems = getArguments().getInt(ARGUMENT_SIZE);
            operationId = getArguments().getString(ARGUMENT_OPERATIONID);
        }

        ProgressDialog dialog = new ProgressDialog(getActivity());
        if (iconId == 0)
        {
            iconId = R.drawable.ic_application_logo;
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
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (operationId != null)
                        {
                            Operator.with(getActivity()).cancel(operationId);
                        }
                        dialog.dismiss();
                    }
                });

        getActivity().getSupportLoaderManager().restartLoader(this.hashCode(), null, this);

        return dialog;
    }

    @Override
    public void onResume()
    {
        EventBusManager.getInstance().register(this);
        setCancelable(false);
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ACTION_OPERATIONS_COMPLETED);
        if (intentId != null)
        {
            intentFilter.addAction(intentId);
        }
        if (receiver == null)
        {
            receiver = new OperationReceiver();
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
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
    public void onStop()
    {
        try
        {
            EventBusManager.getInstance().unregister(this);
        }
        catch (Exception e)
        {

        }
        super.onStop();
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
        super.onDismiss(dialog);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri baseUri = OperationsContentProvider.CONTENT_URI;

        if (parent != null)
        {
            return new CursorLoader(getActivity(), baseUri, OperationSchema.COLUMN_ALL,
                    OperationSchema.COLUMN_PARENT_ID + "=\"" + parent.getIdentifier() + "\" AND "
                            + OperationSchema.COLUMN_REQUEST_TYPE + "=" + operationType, null, null);
        }
        else
        {
            return new CursorLoader(getActivity(), baseUri, OperationSchema.COLUMN_ALL,
                    OperationSchema.COLUMN_REQUEST_TYPE + "=" + operationType, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        if (getDialog() == null) { return; }
        int progress = 0;

        // If no data, we consider the job has been done
        if (cursor.getCount() == 0)
        {
            // Log.d(TAG, "onLoadFinished dismiss");
            canDismiss = true;
        }

        // Check Progress.
        while (cursor.moveToNext())
        {
            if (cursor.getInt(OperationSchema.COLUMN_STATUS_ID) == Operation.STATUS_SUCCESSFUL)
            {
                progress++;
            }
        }
        ((ProgressDialog) getDialog()).setProgress(progress);

        // Dismiss ?
        if (canDismiss)
        {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_OPERATIONS_COMPLETED);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        // DO Nothing
    }

    // //////////////////////////////////////////////////////////////////////
    // EVENTS
    // //////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onBatchOperation(BatchOperationEvent event)
    {
        if (event != null && operationId != null && event.groupKey != null && event.groupKey.equals(operationId))
        {
            dismiss();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // //////////////////////////////////////////////////////////////////////
    public class OperationReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intentId != null && intentId.equals(intent.getAction()))
            {
                dismiss();
                return;
            }

            if (canDismiss && ACTION_OPERATIONS_COMPLETED.equals(intent.getAction()))
            {
                dismiss();
            }
        }
    }
}
