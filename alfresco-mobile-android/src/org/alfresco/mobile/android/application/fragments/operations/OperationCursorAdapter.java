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
import org.alfresco.mobile.android.application.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationContentProvider;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.account.LoadSessionRequest;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateFolderRequest;
import org.alfresco.mobile.android.application.operations.batch.node.delete.DeleteNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.download.DownloadRequest;
import org.alfresco.mobile.android.application.operations.batch.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.application.utils.ProgressViewHolder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

public class OperationCursorAdapter extends BaseCursorLoader<ProgressViewHolder>
{
    // private static final String TAG = OperationCursorAdapter.class.getName();

    public OperationCursorAdapter(Context context, Cursor c, int layoutResourceId)
    {
        super(context, c, layoutResourceId);
        vhClassName = ProgressViewHolder.class.getCanonicalName();
    }

    protected void updateIcon(ProgressViewHolder vh, Cursor cursor)
    {
        vh.icon.setImageResource(MimeTypeManager.getIcon(cursor.getString(BatchOperationSchema.COLUMN_TITLE_ID)));
    }

    protected void updateBottomText(ProgressViewHolder vh, final Cursor cursor)
    {
        int status = cursor.getInt(BatchOperationSchema.COLUMN_STATUS_ID);
        String statusValue = displayType(vh, cursor.getInt(BatchOperationSchema.COLUMN_REQUEST_TYPE_ID)) + " : ";
        vh.progress.setVisibility(View.GONE);
        vh.choose.setTag(R.id.operation_id, cursor.getInt(BatchOperationSchema.COLUMN_ID_ID));
        vh.choose.setTag(R.id.operation_status, cursor.getInt(BatchOperationSchema.COLUMN_STATUS_ID));
        switch (status)
        {
            case Operation.STATUS_PENDING:
                vh.choose.setImageResource(R.drawable.ic_cancel);
                statusValue += context.getString(R.string.status_pending);
                break;
            case Operation.STATUS_RUNNING:
                vh.choose.setImageResource(R.drawable.ic_cancel);
                vh.progress.setVisibility(View.VISIBLE);
                long totalSize = cursor.getLong(BatchOperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                if (totalSize == -1)
                {
                    vh.progress.setIndeterminate(true);
                }
                else
                {
                    long progress = cursor.getLong(BatchOperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID);
                    float value = (((float) progress / ((float) totalSize)) * 100);
                    int percentage = Math.round(value);
                    if (percentage == 100)
                    {
                        vh.progress.setIndeterminate(true);
                    }
                    else
                    {
                        vh.progress.setIndeterminate(false);
                        vh.progress.setProgress(percentage);
                        vh.progress.setMax(100);
                    }
                }
                statusValue += context.getString(R.string.status_running);
                break;
            case Operation.STATUS_PAUSED:
                vh.choose.setImageResource(R.drawable.ic_retry);
                statusValue += context.getString(R.string.status_paused);
                break;
            case Operation.STATUS_SUCCESSFUL:
                vh.choose.setImageResource(R.drawable.ic_validate);
                statusValue += context.getString(R.string.status_successful);
                break;
            case Operation.STATUS_FAILED:
                vh.choose.setImageResource(R.drawable.ic_retry);
                statusValue += context.getString(R.string.status_failed);
                break;
            case Operation.STATUS_CANCEL:
                vh.choose.setImageResource(R.drawable.ic_retry);
                statusValue += context.getString(R.string.status_cancelled);
                break;
            default:
                break;
        }

        vh.choose.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                int status = (Integer) v.getTag(R.id.operation_status);
                long id = (Integer) v.getTag(R.id.operation_id);
                switch (status)
                {
                    case Operation.STATUS_PENDING:
                    case Operation.STATUS_RUNNING:
                        // Cancel operation
                        BatchOperationManager.getInstance(context).forceStop((int) id);
                        break;
                    case Operation.STATUS_PAUSED:
                    case Operation.STATUS_FAILED:
                    case Operation.STATUS_CANCEL:
                        // Retry
                        BatchOperationManager.getInstance(context).retry(id);
                        break;
                    case Operation.STATUS_SUCCESSFUL:
                        // Remove operation
                        Uri uri = Uri.parse(BatchOperationContentProvider.CONTENT_URI + "/"
                                + v.getTag(R.id.operation_id));
                        v.getContext().getContentResolver().delete(uri, null, null);
                        break;
                    default:
                        break;
                }
            }
        });

        vh.bottomText.setText(statusValue);
    }

    protected void updateTopText(ProgressViewHolder vh, Cursor cursor)
    {
        vh.topText.setText(cursor.getString(BatchOperationSchema.COLUMN_TITLE_ID));
    }

    protected void displayStatut(ProgressViewHolder vh, int imageResource)
    {
        vh.iconTopRight.setVisibility(View.VISIBLE);
        vh.iconTopRight.setImageResource(imageResource);
    }

    protected String displayType(ProgressViewHolder vh, int typeId)
    {

        int resId = R.string.operation_default;
        switch (typeId)
        {
            case DownloadRequest.TYPE_ID:
                resId = R.string.DownloadRequest;
                break;
            case CreateDocumentRequest.TYPE_ID:
                resId = R.string.CreateDocumentRequest;
                break;
            case UpdateContentRequest.TYPE_ID:
                resId = R.string.UpdateContentRequest;
                break;
            case DeleteNodeRequest.TYPE_ID:
                resId = R.string.DeleteNodeRequest;
                break;
            case LikeNodeRequest.TYPE_ID:
                resId = R.string.LikeNodeRequest;
                break;
            case FavoriteNodeRequest.TYPE_ID:
                resId = R.string.FavoriteNodeRequest;
                break;
            case CreateFolderRequest.TYPE_ID:
                resId = R.string.CreateFolderRequest;
                break;
            case LoadSessionRequest.TYPE_ID:
                resId = R.string.LoadSessionRequest;
                break;
            default:
                break;
        }
        return context.getString(resId);
    }

}
