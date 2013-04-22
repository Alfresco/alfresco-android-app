package org.alfresco.mobile.android.application.fragments.browser;

import java.util.List;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.OperationContentProvider;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.integration.utils.NodePlaceHolder;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

public class ProgressNodeAdapter extends NodeAdapter implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = ProgressNodeAdapter.class.getName();

    protected Node parentNode;

    public ProgressNodeAdapter(Activity context, int textViewResourceId, Node parentNode, List<Node> listItems,
            List<Node> selectedItems, int mode)
    {
        super(context, textViewResourceId, listItems, selectedItems, mode);
        this.parentNode = parentNode;
        context.getLoaderManager().restartLoader(parentNode.hashCode(), null, this);
    }

    public ProgressNodeAdapter(Activity context, int textViewResourceId, List<Node> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

    // /////////////////////////////////////////////////////////////
    // ITEM LINE
    // ////////////////////////////////////////////////////////////
    @Override
    protected void updateTopText(GenericViewHolder vh, Node item)
    {
        ProgressBar progressView = (ProgressBar) ((View) vh.topText.getParent()).findViewById(R.id.status_progress);

        if (item instanceof NodePlaceHolder)
        {
            vh.topText.setText(item.getName());
            vh.topText.setEnabled(false);
            long totalSize = ((NodePlaceHolder) item).getLength();
            progressView.setVisibility(View.VISIBLE);
            progressView.setIndeterminate(false);
            if (totalSize == -1)
            {
                progressView.setMax(100);
                progressView.setProgress(0);
            }
            else
            {
                long progress = ((NodePlaceHolder) item).getProgress();
                float value = (((float) progress / ((float) totalSize)) * 100);
                int percentage = Math.round(value);

                if (percentage == 100)
                {
                    progressView.setIndeterminate(true);
                }
                else
                {
                    progressView.setIndeterminate(false);
                    progressView.setMax(100);
                    progressView.setProgress(percentage);
                }
            }
        }
        else
        {
            progressView.setVisibility(View.GONE);
            super.updateTopText(vh, item);
        }
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Node item)
    {
        if (item instanceof NodePlaceHolder)
        {
            vh.bottomText.setEnabled(false);
            vh.bottomText.setVisibility(View.GONE);
        }
        else
        {
            vh.bottomText.setVisibility(View.VISIBLE);
            super.updateBottomText(vh, item);
        }
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Node item)
    {
        if (item instanceof NodePlaceHolder)
        {
            UIUtils.setBackground(((View) vh.icon), null);
            vh.icon.setImageResource(MimeTypeManager.getIcon(item.getName()));
        }
        else
        {
            super.updateIcon(vh, item);
        }
    }

    // /////////////////////////////////////////////////////////////
    // INLINE PROGRESS
    // ////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri baseUri = OperationContentProvider.CONTENT_URI;

        return new CursorLoader(getContext(), baseUri, OperationSchema.COLUMN_ALL, OperationSchema.COLUMN_PARENT_ID
                + "=\"" + parentNode.getIdentifier() + "\" AND " + OperationSchema.COLUMN_REQUEST_TYPE + " IN("
                + CreateDocumentRequest.TYPE_ID + ")", null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        Log.d(TAG, "Count : " + cursor.getCount());
        while (cursor.moveToNext())
        {
            int status = cursor.getInt(OperationSchema.COLUMN_STATUS_ID);
            String name = cursor.getString(OperationSchema.COLUMN_NOTIFICATION_TITLE_ID);

            switch (status)
            {
                case Operation.STATUS_PENDING:
                    // Add Node if not present
                    if (!hasNode(name))
                    {
                        replaceNode(new NodePlaceHolder(name));
                    }
                    break;
                case Operation.STATUS_RUNNING:
                    // Update node if not present
                    long progress = cursor.getLong(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID);
                    long totalSize = cursor.getLong(OperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                    replaceNode(new NodePlaceHolder(name, totalSize, progress));
                    break;
                default:
                    if (hasNode(name) && getNode(name) instanceof NodePlaceHolder)
                    {
                        remove(name);
                    }
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        // TODO Auto-generated method stub
    }
}
