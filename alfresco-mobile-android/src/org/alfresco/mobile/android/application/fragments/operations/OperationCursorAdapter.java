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
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.OperationContentProvider;
import org.alfresco.mobile.android.application.integration.OperationManager;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;

public class OperationCursorAdapter extends BaseCursorLoader<ProgressViewHolder>
{
    //private static final String TAG = OperationCursorAdapter.class.getName();

    public OperationCursorAdapter(Context context, Cursor c, int layoutResourceId)
    {
        super(context, c, layoutResourceId);
        vhClassName = ProgressViewHolder.class.getCanonicalName();
    }

    protected void updateIcon(ProgressViewHolder vh, Cursor cursor)
    {
        vh.icon.setImageResource(MimeTypeManager.getIcon(cursor.getString(OperationSchema.COLUMN_NOTIFICATION_TITLE_ID)));
    }

    protected void updateBottomText(ProgressViewHolder vh, final Cursor cursor)
    {
        int status = cursor.getInt(OperationSchema.COLUMN_STATUS_ID);
        String statusValue = null;
        vh.progress.setVisibility(View.GONE);
        vh.choose.setTag(R.id.operation_id, cursor.getInt(OperationSchema.COLUMN_ID_ID));
        vh.choose.setTag(R.id.operation_status, cursor.getInt(OperationSchema.COLUMN_STATUS_ID));
        switch (status)
        {
            case Operation.STATUS_PENDING:
                vh.choose.setImageResource(R.drawable.ic_cancel);
                statusValue = context.getString(R.string.status_pending);
                break;
            case Operation.STATUS_RUNNING:
                vh.choose.setImageResource(R.drawable.ic_cancel);
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
                    vh.progress.setIndeterminate(false);
                    vh.progress.setMax(100);
                    vh.progress.setProgress(percentage);
                }
                statusValue = context.getString(R.string.status_running);
                break;
            case Operation.STATUS_PAUSED:
                vh.choose.setImageResource(R.drawable.ic_retry);
                statusValue = context.getString(R.string.status_paused);
                break;
            case Operation.STATUS_SUCCESSFUL:
                vh.choose.setImageResource(R.drawable.ic_validate);
                statusValue = context.getString(R.string.status_successful);
                break;
            case Operation.STATUS_FAILED:
                vh.choose.setImageResource(R.drawable.ic_retry);
                statusValue = context.getString(R.string.status_failed);
                break;
            case Operation.STATUS_CANCEL:
                vh.choose.setImageResource(R.drawable.ic_retry);
                statusValue = context.getString(R.string.status_cancelled);
                break;
            default:
                break;
        }

        vh.choose.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                int status = (Integer) v.getTag(R.id.operation_status);
                int id = (Integer) v.getTag(R.id.operation_id);
                switch (status)
                {
                    case Operation.STATUS_PENDING:
                    case Operation.STATUS_RUNNING:
                        //Cancel operation
                        OperationManager.forceStop(v.getContext(), id);
                        break;
                    case Operation.STATUS_PAUSED:
                    case Operation.STATUS_FAILED:
                    case Operation.STATUS_CANCEL:
                        //Retry
                        OperationManager.getInstance(context).retry(v.getTag(R.id.operation_id)+"");
                        break;
                    case Operation.STATUS_SUCCESSFUL:
                        //Remove operation
                        Uri uri = Uri.parse(OperationContentProvider.CONTENT_URI + "/" + v.getTag(R.id.operation_id));
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
        vh.topText.setText(cursor.getString(OperationSchema.COLUMN_NOTIFICATION_TITLE_ID));
    }
}

final class ProgressViewHolder extends GenericViewHolder
{

    public ProgressBar progress;

    public ProgressViewHolder(View v)
    {
        super(v);
        this.progress = (ProgressBar) v.findViewById(R.id.status_progress);
    }
}
