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

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.BaseCursorListFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationContentProvider;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.operations.batch.node.download.DownloadRequest;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class OperationsFragment extends BaseCursorListFragment
{
    public static final String TAG = OperationsFragment.class.getName();

    private Button cancelAll;

    private Button dismissAll;

    public OperationsFragment()
    {
        emptyListMessageId = R.string.operations_empty;
        layoutId = R.layout.app_operations_list;
        title = R.string.operation_default;
    }

    // /////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        adapter = new OperationCursorAdapter(getActivity(), null, R.layout.app_list_operation_row);
        lv.setAdapter(adapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), getString(R.string.operation_default));
        super.onResume();
    }

    // /////////////////////////////////////////////////////////////
    // LIST MANAGEMENT
    // ////////////////////////////////////////////////////////////
    public void onListItemClick(ListView l, View v, int position, long id)
    {
    }

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        return false;
    }

    // /////////////////////////////////////////////////////////////
    // UTILS
    // ////////////////////////////////////////////////////////////
    protected void init(View v, int estring)
    {
        super.init(v, estring);
        cancelAll = (Button) v.findViewById(R.id.cancel_all);
        cancelAll.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelAll();
            }
        });

        dismissAll = (Button) v.findViewById(R.id.dismiss_all);
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
        BatchOperationManager.getInstance(getActivity()).cancelAll(getActivity());
    }

    protected void dismissAll()
    {
        Cursor cursor = adapter.getCursor();
        Uri uri = null;
        if (!cursor.isFirst())
        {
            cursor.moveToPosition(-1);
        }

        for (int i = 0; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            uri = Uri.parse(BatchOperationContentProvider.CONTENT_URI + "/"
                    + cursor.getInt(BatchOperationSchema.COLUMN_ID_ID));
            getActivity().getContentResolver().delete(uri, null, null);
        }
        cancelAll.setVisibility(View.GONE);
        dismissAll.setVisibility(View.GONE);
    }

    // /////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ////////////////////////////////////////////////////////////
    private static final String UPLOAD_REQUESTS = BatchOperationSchema.COLUMN_REQUEST_TYPE + " IN ("
            + CreateDocumentRequest.TYPE_ID + "," + UpdateContentRequest.TYPE_ID + ")";

    private static final String DOWNLOAD_REQUESTS = BatchOperationSchema.COLUMN_REQUEST_TYPE + " IN ("
            + DownloadRequest.TYPE_ID + ")";

    private static final String ALL_REQUESTS = BatchOperationSchema.COLUMN_REQUEST_TYPE + " IN ("
            + DownloadRequest.TYPE_ID + "," + CreateDocumentRequest.TYPE_ID + "," + UpdateContentRequest.TYPE_ID + ")";

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);

        int filter = -1;
        if (getArguments() != null)
        {
            filter = getArguments().getInt(IntentIntegrator.EXTRA_OPERATIONS_TYPE);
            getArguments().remove(IntentIntegrator.EXTRA_OPERATIONS_TYPE);
        }

        String request = ALL_REQUESTS;
        switch (filter)
        {
            case NotificationHelper.DOWNLOAD_NOTIFICATION_ID:
                request = DOWNLOAD_REQUESTS;
                break;
            case NotificationHelper.UPLOAD_NOTIFICATION_ID:
                request = UPLOAD_REQUESTS;
                break;
            default:
                break;
        }

        if (SessionUtils.getAccount(getActivity()) != null)
        {
            request = BatchOperationContentProvider.getAccountFilter(SessionUtils.getAccount(getActivity())) + " AND "
                    + request;
        }

        Uri baseUri = BatchOperationContentProvider.CONTENT_URI;

        return new CursorLoader(getActivity(), baseUri, BatchOperationSchema.COLUMN_ALL, request, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        super.onLoadFinished(arg0, cursor);
        if (cursor.getCount() == 0)
        {
            dismissAll.setVisibility(View.GONE);
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
                if (cursor.getInt(BatchOperationSchema.COLUMN_STATUS_ID) == Operation.STATUS_RUNNING
                        || cursor.getInt(BatchOperationSchema.COLUMN_STATUS_ID) == Operation.STATUS_PENDING)
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
    }
}
