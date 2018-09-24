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
package org.alfresco.mobile.android.application.fragments.operation;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsContentProvider;
import org.alfresco.mobile.android.async.OperationsFactory;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.async.node.create.CreateFolderRequest;
import org.alfresco.mobile.android.async.node.delete.DeleteNodeRequest;
import org.alfresco.mobile.android.async.node.download.DownloadRequest;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.async.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.async.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.async.session.LoadSessionRequest;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.ui.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.ui.holder.TwoLinesProgressViewHolder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @since 1.2
 * @author Jean Marie Pascal
 */
public class OperationCursorAdapter extends BaseCursorLoader<TwoLinesProgressViewHolder>
{
    // private static final String TAG = OperationCursorAdapter.class.getName();

    public OperationCursorAdapter(Context context, Cursor c, int layoutResourceId)
    {
        super(context, c, layoutResourceId);
        vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
    }

    protected void updateIcon(TwoLinesProgressViewHolder vh, Cursor cursor)
    {
        vh.icon.setImageResource(
                MimeTypeManager.getInstance(context).getIcon(cursor.getString(OperationSchema.COLUMN_TITLE_ID)));
    }

    protected void updateBottomText(TwoLinesProgressViewHolder vh, final Cursor cursor)
    {
        int status = cursor.getInt(OperationSchema.COLUMN_STATUS_ID);
        String statusValue = displayType(cursor.getInt(OperationSchema.COLUMN_REQUEST_TYPE_ID)) + " : ";
        vh.progress.setVisibility(View.GONE);
        vh.choose.setTag(R.id.operation_id, cursor.getInt(OperationSchema.COLUMN_ID_ID));
        vh.choose.setTag(R.id.operation_status, cursor.getInt(OperationSchema.COLUMN_STATUS_ID));
        switch (status)
        {
            case Operation.STATUS_PENDING:
                vh.choose.setVisibility(View.VISIBLE);
                vh.choose.setImageResource(R.drawable.ic_cancel);
                statusValue += context.getString(R.string.status_pending);
                break;
            case Operation.STATUS_RUNNING:
                vh.choose.setVisibility(View.GONE);
                // vh.choose.setImageResource(R.drawable.ic_cancel);
                vh.progress.setVisibility(View.VISIBLE);
                long totalSize = cursor.getLong(OperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                if (totalSize == -1)
                {
                    vh.progress.setIndeterminate(true);
                }
                else
                {
                    long progress = cursor.getLong(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID);
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
                vh.choose.setVisibility(View.VISIBLE);
                vh.choose.setImageResource(R.drawable.ic_retry);
                statusValue += context.getString(R.string.status_paused);
                break;
            case Operation.STATUS_SUCCESSFUL:
                vh.choose.setVisibility(View.VISIBLE);
                vh.choose.setImageResource(R.drawable.ic_validate);
                statusValue += context.getString(R.string.status_successful);
                break;
            case Operation.STATUS_FAILED:
                vh.choose.setVisibility(View.VISIBLE);
                vh.choose.setImageResource(R.drawable.ic_retry);
                statusValue += context.getString(R.string.status_failed);
                break;
            case Operation.STATUS_CANCEL:
                vh.choose.setVisibility(View.VISIBLE);
                vh.choose.setImageResource(R.drawable.ic_retry);
                statusValue += context.getString(R.string.status_cancelled);
                break;
            default:
                break;
        }

        try
        {
            vh.choose.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    int status = (Integer) v.getTag(R.id.operation_status);
                    long id = (Integer) v.getTag(R.id.operation_id);
                    Uri uri = Uri.parse(OperationsContentProvider.CONTENT_URI + "/" + v.getTag(R.id.operation_id));
                    switch (status)
                    {
                        case Operation.STATUS_PENDING:
                        case Operation.STATUS_RUNNING:
                            // Cancel operation
                            Operator.with(context).cancel(uri.toString());
                            break;
                        case Operation.STATUS_PAUSED:
                        case Operation.STATUS_FAILED:
                        case Operation.STATUS_CANCEL:
                            // Retry
                            if (OperationsFactory.getRequest(context, uri) == null) {
                                v.getContext().getContentResolver().delete(uri, null, null);
                            } else {
                                Operator.with(context).retry(uri);
                            }
                            break;
                        case Operation.STATUS_SUCCESSFUL:
                            // Remove operation
                            v.getContext().getContentResolver().delete(uri, null, null);
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        catch (Exception e)
        {
            Log.d("Operations", Log.getStackTraceString(e));
        }

        vh.bottomText.setText(statusValue);
    }

    protected void updateTopText(TwoLinesProgressViewHolder vh, Cursor cursor)
    {
        vh.topText.setText(cursor.getString(OperationSchema.COLUMN_TITLE_ID));
        vh.topText.setFocusable(true);
    }

    protected String displayType(int typeId)
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
