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
package org.alfresco.mobile.android.application.fragments.operation;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsContentProvider;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.async.node.download.DownloadRequest;
import org.alfresco.mobile.android.async.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseCursorGridFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;

public class OperationsFragment extends BaseCursorGridFragment
{
    public static final String TAG = OperationsFragment.class.getName();

    private Button cancelAll;

    private Button dismissAll;

    public OperationsFragment()
    {
        emptyListMessageId = R.string.operations_empty;
        requiredSession = false;
        checkSession = false;
        displayAsList = true;
    }

    // /////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////////////
    @Override
    public String onPrepareTitle()
    {
        return getString(R.string.operation_default);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null && getDialog() == null) { return null; }
        setRootView(inflater.inflate(R.layout.app_operations_list, container, false));

        init(getRootView(), emptyListMessageId);

        return getRootView();
    }

    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), getString(R.string.operation_default));
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Override
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected BaseAdapter onAdapterCreation()
    {
        return new OperationCursorAdapter(getActivity(), null, R.layout.row_two_lines_progress);
    }

    @Override
    protected void performRequest(ListingContext lcorigin)
    {
        getLoaderManager().initLoader(0, null, this);
    }

    // /////////////////////////////////////////////////////////////
    // UTILS
    // ////////////////////////////////////////////////////////////
    protected void init(View v, int estring)
    {
        super.init(v, estring);
        cancelAll = UIUtils.initCancel(v, R.string.cancel_all, true);
        cancelAll.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelAll();
            }
        });

        dismissAll = UIUtils.initValidation(v, R.string.dismiss_all);
        dismissAll.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismissAll();
            }
        });
    }

    protected void cancelAll()
    {
        Cursor cursor = ((CursorAdapter) adapter).getCursor();
        Uri uri = null;
        if (!cursor.isFirst())
        {
            cursor.moveToPosition(-1);
        }

        for (int i = 0; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            uri = Uri.parse(OperationsContentProvider.CONTENT_URI + "/" + cursor.getInt(OperationSchema.COLUMN_ID_ID));
            Operator.with(getActivity()).cancel(uri.toString());
        }
        cancelAll.setVisibility(View.GONE);
        dismissAll.setVisibility(View.GONE);
    }

    protected void dismissAll()
    {
        Cursor cursor = ((CursorAdapter) adapter).getCursor();
        Uri uri = null;
        if (!cursor.isFirst())
        {
            cursor.moveToPosition(-1);
        }

        for (int i = 0; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            uri = Uri.parse(OperationsContentProvider.CONTENT_URI + "/" + cursor.getInt(OperationSchema.COLUMN_ID_ID));
            getActivity().getContentResolver().delete(uri, null, null);
        }
        cancelAll.setVisibility(View.GONE);
        dismissAll.setVisibility(View.GONE);
    }

    // /////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ////////////////////////////////////////////////////////////
    private static final String UPLOAD_REQUESTS = OperationSchema.COLUMN_REQUEST_TYPE + " IN ("
            + CreateDocumentRequest.TYPE_ID + "," + UpdateContentRequest.TYPE_ID + ")";

    private static final String DOWNLOAD_REQUESTS = OperationSchema.COLUMN_REQUEST_TYPE + " IN ("
            + DownloadRequest.TYPE_ID + ")";

    private static final String ALL_REQUESTS = OperationSchema.COLUMN_REQUEST_TYPE + " IN (" + DownloadRequest.TYPE_ID
            + "," + CreateDocumentRequest.TYPE_ID + "," + UpdateContentRequest.TYPE_ID + ")";

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);

        int filter = -1;
        if (getArguments() != null)
        {
            filter = getArguments().getInt(PrivateIntent.EXTRA_OPERATIONS_TYPE);
            getArguments().remove(PrivateIntent.EXTRA_OPERATIONS_TYPE);
        }

        String request = ALL_REQUESTS;
        switch (filter)
        {
            case AlfrescoNotificationManager.CHANNEL_DOWNLOAD:
                request = DOWNLOAD_REQUESTS;
                break;
            case AlfrescoNotificationManager.CHANNEL_UPLOAD:
                request = UPLOAD_REQUESTS;
                break;
            default:
                break;
        }

        if (SessionUtils.getAccount(getActivity()) != null)
        {
            request = OperationsContentProvider.getAccountFilter(SessionUtils.getAccount(getActivity())) + " AND "
                    + request;
        }

        Uri baseUri = OperationsContentProvider.CONTENT_URI;

        return new CursorLoader(getActivity(), baseUri, OperationSchema.COLUMN_ALL, request, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        super.onLoadFinished(arg0, cursor);
        if (cursor.getCount() == 0)
        {
            dismissAll.setVisibility(View.GONE);
            ev.setVisibility(View.VISIBLE);
            displayEmptyView();
        }
        else
        {
            dismissAll.setVisibility(View.VISIBLE);
            if (!cursor.isFirst())
            {
                cursor.moveToPosition(-1);
            }

            boolean isVisible = false;
            while (cursor.moveToNext())
            {
                if (cursor.getInt(OperationSchema.COLUMN_STATUS_ID) == Operation.STATUS_RUNNING
                        || cursor.getInt(OperationSchema.COLUMN_STATUS_ID) == Operation.STATUS_PENDING)
                {
                    isVisible = true;
                    break;
                }
            }

            cancelAll.setVisibility(View.GONE);
            if (isVisible)
            {
                cancelAll.setVisibility(View.VISIBLE);
            }
        }
        refreshHelper.setRefreshComplete();
    }

    // /////////////////////////////////////////////////////////////
    // MAIN MENU DISPLAY ITEM
    // ////////////////////////////////////////////////////////////
    public static boolean canDisplay(Context context, AlfrescoAccount account)
    {
        if (account == null) { return false; }
        Cursor operationsToDisplay = null;
        boolean result = false;
        try
        {
            operationsToDisplay = context.getContentResolver().query(OperationsContentProvider.CONTENT_URI,
                    OperationSchema.COLUMN_ALL,
                    OperationsContentProvider.getAccountFilter(account) + " AND " + ALL_REQUESTS, null, null);
            result = operationsToDisplay.getCount() > 0;
        }
        catch (Exception e)
        {
            // Do Nothing
        }
        finally
        {
            CursorUtils.closeCursor(operationsToDisplay);
        }
        return result;
    }

    @Override
    public void refresh()
    {
        onPrepareRefresh();
        isFullLoad = Boolean.FALSE;
        hasmore = Boolean.FALSE;
        skipCount = 0;
        adapter = null;
        getLoaderManager().restartLoader(loaderId, getArguments(), this);
        getLoaderManager().getLoader(loaderId).forceLoad();
    }
}
