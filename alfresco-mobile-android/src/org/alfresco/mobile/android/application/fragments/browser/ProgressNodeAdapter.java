/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.impl.publicapi.PublicAPIPropertyIds;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.commons.utils.AndroidVersion;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.manager.AccessibilityHelper;
import org.alfresco.mobile.android.application.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationContentProvider;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.operations.batch.node.download.DownloadRequest;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.application.operations.batch.utils.NodePlaceHolder;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.utils.CursorUtils;
import org.alfresco.mobile.android.application.utils.ProgressViewHolder;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;

/**
 * @since 1.2
 * @author Jean Marie Pascal
 */
public class ProgressNodeAdapter extends NodeAdapter implements LoaderManager.LoaderCallbacks<Cursor>,
        OnMenuItemClickListener
{
    private static final String TAG = ProgressNodeAdapter.class.getName();

    private static final int LOADER_OPERATION_ID = 1;

    private static final int LOADER_SYNC_ID = 2;

    private static final int MAX_PROGRESS = 100;

    protected Node parentNode;

    private List<Node> selectedOptionItems = new ArrayList<Node>();

    private Map<String, FavoriteInfo> favoriteInfos;

    private boolean hasFavorite = false;

    private boolean isSyncFolder;

    public ProgressNodeAdapter(Activity context, int textViewResourceId, Node parentNode, List<Node> listItems,
            List<Node> selectedItems, int mode)
    {
        super(context, textViewResourceId, listItems, selectedItems, mode);
        vhClassName = ProgressViewHolder.class.getCanonicalName();
        this.parentNode = parentNode;
        if (parentNode != null)
        {
            context.getLoaderManager().restartLoader(LOADER_OPERATION_ID, null, this);
            context.getLoaderManager().restartLoader(LOADER_SYNC_ID, null, this);
            hasParentFavorite();
        }
    }

    public ProgressNodeAdapter(Activity context, int textViewResourceId, List<Node> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

    public ProgressNodeAdapter(Activity context, int textViewResourceId, Node parentNode, List<Node> listItems,
            Map<String, Document> selectedItems)
    {
        super(context, textViewResourceId, listItems, selectedItems);
        vhClassName = ProgressViewHolder.class.getCanonicalName();
        this.parentNode = parentNode;
        if (parentNode != null)
        {
            context.getLoaderManager().restartLoader(LOADER_OPERATION_ID, null, this);
            context.getLoaderManager().restartLoader(LOADER_SYNC_ID, null, this);
            hasParentFavorite();
        }
    }

    // /////////////////////////////////////////////////////////////
    // ITEM LINE
    // ////////////////////////////////////////////////////////////
    @Override
    protected void updateTopText(ProgressViewHolder vh, Node item)
    {
        ProgressBar progressView = (ProgressBar) ((View) vh.topText.getParent()).findViewById(R.id.status_progress);

        if (item instanceof NodePlaceHolder)
        {
            vh.topText.setText(item.getName());
            vh.topText.setEnabled(false);
            long totalSize = ((NodePlaceHolder) item).getLength();

            if ((Integer) item.getPropertyValue(PublicAPIPropertyIds.REQUEST_STATUS) == Operation.STATUS_PAUSED)
            {
                progressView.setVisibility(View.GONE);
            }
            else
            {
                progressView.setVisibility(View.VISIBLE);
                progressView.setIndeterminate(false);
                if (totalSize == -1)
                {
                    progressView.setMax(MAX_PROGRESS);
                    progressView.setProgress(0);
                }
                else
                {
                    long progress = ((NodePlaceHolder) item).getProgress();
                    float value = (((float) progress / ((float) totalSize)) * MAX_PROGRESS);
                    int percentage = Math.round(value);

                    if (percentage == MAX_PROGRESS)
                    {
                        if ((Integer) item.getPropertyValue(PublicAPIPropertyIds.REQUEST_TYPE) == DownloadRequest.TYPE_ID)
                        {
                            progressView.setVisibility(View.GONE);
                            super.updateTopText(vh, item);
                            vh.bottomText.setVisibility(View.VISIBLE);
                            super.updateBottomText(vh, item);
                            super.updateIcon(vh, item);
                        }
                        else
                        {
                            progressView.setIndeterminate(true);
                        }
                    }
                    else
                    {
                        progressView.setIndeterminate(false);
                        progressView.setMax(MAX_PROGRESS);
                        progressView.setProgress(percentage);
                    }
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
    protected void updateBottomText(ProgressViewHolder vh, Node item)
    {
        if (hasFavorite && favoriteInfos.containsKey(item.getIdentifier()))
        {
            FavoriteInfo favoriteInfo = favoriteInfos.get(item.getIdentifier());
            if (favoriteInfo.isFavorite)
            {
                vh.favoriteIcon.setVisibility(View.VISIBLE);
                vh.favoriteIcon.setImageResource(R.drawable.ic_favorite_dark);
            }
            else
            {
                vh.favoriteIcon.setVisibility(View.GONE);
            }

            if (SynchroManager.getInstance(getContext()).hasActivateSync(SessionUtils.getAccount(getContext())))
            {
                switch (favoriteInfo.status)
                {
                    case SyncOperation.STATUS_PENDING:
                        displayStatut(vh, R.drawable.sync_status_pending);
                        break;
                    case SyncOperation.STATUS_RUNNING:
                        displayStatut(vh, R.drawable.sync_status_loading);
                        break;
                    case SyncOperation.STATUS_PAUSED:
                        displayStatut(vh, R.drawable.sync_status_pending);
                        break;
                    case SyncOperation.STATUS_MODIFIED:
                        displayStatut(vh, R.drawable.sync_status_pending);
                        break;
                    case SyncOperation.STATUS_SUCCESSFUL:
                        displayStatut(vh, R.drawable.sync_status_success);
                        break;
                    case SyncOperation.STATUS_FAILED:
                        displayStatut(vh, R.drawable.sync_status_failed);
                        break;
                    case SyncOperation.STATUS_CANCEL:
                        displayStatut(vh, R.drawable.sync_status_failed);
                        break;
                    case SyncOperation.STATUS_REQUEST_USER:
                        displayStatut(vh, R.drawable.sync_status_failed);
                        break;
                    default:
                        vh.favoriteIcon.setVisibility(View.GONE);
                        vh.iconBottomRight.setVisibility(View.GONE);
                        break;
                }
            } else {
                vh.iconBottomRight.setVisibility(View.GONE);
            }
        }
        else
        {
            vh.favoriteIcon.setVisibility(View.GONE);
            vh.iconBottomRight.setVisibility(View.GONE);
        }

        if (item instanceof NodePlaceHolder)
        {
            vh.bottomText.setEnabled(false);
            int status = (Integer) item.getPropertyValue(PublicAPIPropertyIds.REQUEST_STATUS);
            if (status == Operation.STATUS_PAUSED || status == Operation.STATUS_PENDING)
            {
                vh.bottomText.setVisibility(View.VISIBLE);
                int resId = R.string.download_await;
                switch ((Integer) item.getPropertyValue(PublicAPIPropertyIds.REQUEST_TYPE))
                {
                    case DownloadRequest.TYPE_ID:
                        resId = R.string.download_await;
                        break;
                    case CreateDocumentRequest.TYPE_ID:
                    case UpdateContentRequest.TYPE_ID:
                        resId = R.string.upload_await;
                        break;

                    default:
                        break;
                }
                vh.bottomText.setText(resId);
            }
            else
            {
                vh.bottomText.setVisibility(View.GONE);
            }
        }
        else
        {
            vh.bottomText.setVisibility(View.VISIBLE);
            super.updateBottomText(vh, item);
        }
    }

    @Override
    protected void updateIcon(ProgressViewHolder vh, Node item)
    {
        if (item instanceof NodePlaceHolder)
        {
            UIUtils.setBackground(((View) vh.icon), null);
            vh.icon.setImageResource(MimeTypeManager.getIcon(context, item.getName()));
        }
        else
        {
            super.updateIcon(vh, item);
        }

        if (item.isFolder())
        {
            vh.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.mime_256_folder));

            if (mode == ListingModeFragment.MODE_IMPORT) { return; }
            if (mode == ListingModeFragment.MODE_PICK) { return; }

            UIUtils.setBackground(((View) vh.choose),
                    context.getResources().getDrawable(R.drawable.quickcontact_badge_overlay_light));

            vh.choose.setVisibility(View.VISIBLE);
            AccessibilityHelper.addContentDescription(vh.choose, String.format(context.getString(R.string.more_options_folder), item.getName()));
            vh.choose.setTag(R.id.node_action, item);
            vh.choose.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Node item = (Node) v.getTag(R.id.node_action);
                    selectedOptionItems.add(item);
                    PopupMenu popup = new PopupMenu(context, v);
                    getMenu(popup.getMenu(), item);

                    if (AndroidVersion.isICSOrAbove())
                    {
                        popup.setOnDismissListener(new OnDismissListener()
                        {
                            @Override
                            public void onDismiss(PopupMenu menu)
                            {
                                selectedOptionItems.clear();
                            }
                        });
                    }

                    popup.setOnMenuItemClickListener(ProgressNodeAdapter.this);

                    popup.show();
                }
            });
        }
        else
        {
            UIUtils.setBackground(((View) vh.choose), null);
        }
    }

    // /////////////////////////////////////////////////////////////
    // INLINE PROGRESS
    // ////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        if (id == LOADER_OPERATION_ID)
        {
            return new CursorLoader(context, BatchOperationContentProvider.CONTENT_URI,
                    BatchOperationSchema.COLUMN_ALL, BatchOperationSchema.COLUMN_PARENT_ID + "=\""
                            + parentNode.getIdentifier() + "\" AND " + BatchOperationSchema.COLUMN_REQUEST_TYPE
                            + " IN(" + CreateDocumentRequest.TYPE_ID + " , " + DownloadRequest.TYPE_ID + " , "
                            + UpdateContentRequest.TYPE_ID + ")", null, null);
        }
        else if (id == LOADER_SYNC_ID) { return new CursorLoader(context, SynchroProvider.CONTENT_URI,
                SynchroSchema.COLUMN_ALL, SynchroSchema.COLUMN_PARENT_ID + " LIKE '" + parentNode.getIdentifier()
                        + "' AND " + SynchroSchema.COLUMN_STATUS + " != " + SyncOperation.STATUS_HIDDEN, null, null); }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        switch (loader.getId())
        {
            case LOADER_SYNC_ID:
                hasFavorite = (cursor.getCount() > 0);
                if (favoriteInfos == null)
                {
                    favoriteInfos = new HashMap<String, FavoriteInfo>(cursor.getCount());
                }
                favoriteInfos.clear();
                if (hasFavorite)
                {
                    while (cursor.moveToNext())
                    {
                        favoriteInfos.put(cursor.getString(SynchroSchema.COLUMN_NODE_ID_ID), new FavoriteInfo(cursor));
                    }
                }
                notifyDataSetChanged();
                break;

            case LOADER_OPERATION_ID:
                while (cursor.moveToNext())
                {
                    int status = cursor.getInt(BatchOperationSchema.COLUMN_STATUS_ID);
                    String name = cursor.getString(BatchOperationSchema.COLUMN_TITLE_ID);
                    int type = cursor.getInt(BatchOperationSchema.COLUMN_REQUEST_TYPE_ID);

                    switch (status)
                    {
                        case Operation.STATUS_PAUSED:
                        case Operation.STATUS_PENDING:
                            // Add Node if not present
                            if (name != null && !hasNode(name))
                            {
                                replaceNode(new NodePlaceHolder(name, type, status));
                            }
                            break;
                        case Operation.STATUS_RUNNING:
                            // Update node if not present
                            long progress = cursor.getLong(BatchOperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID);
                            long totalSize = cursor.getLong(BatchOperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                            replaceNode(new NodePlaceHolder(name, type, status, totalSize, progress));
                            break;
                        case Operation.STATUS_SUCCESSFUL:
                            // Update node if not present
                            if (type != DownloadRequest.TYPE_ID && hasNode(name)
                                    && getNode(name) instanceof NodePlaceHolder)
                            {
                                notifyDataSetChanged();
                            }
                            break;
                        default:
                            if (hasNode(name) && getNode(name) instanceof NodePlaceHolder)
                            {
                                remove(name);
                            }
                            break;
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        // DO Nothing
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public void getMenu(Menu menu, Node node)
    {
        MenuItem mi;

        Permissions permission = SessionUtils.getSession(context).getServiceRegistry().getDocumentFolderService()
                .getPermissions(node);

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_DETAILS, Menu.FIRST + MenuActionItem.MENU_DETAILS,
                R.string.action_view_properties);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (permission.canEdit())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT,
                    R.string.action_edit_properties);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (permission.canDelete())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE_FOLDER, Menu.FIRST + MenuActionItem.MENU_DELETE_FOLDER,
                    R.string.delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_DETAILS:
                onMenuItemClick = true;
                ((MainActivity) context).addPropertiesFragment(selectedOptionItems.get(0));
                selectedItems.add(selectedOptionItems.get(0));
                notifyDataSetChanged();
                break;
            case MenuActionItem.MENU_EDIT:
                onMenuItemClick = true;
                NodeActions.edit((Activity) context, (Folder) parentNode, selectedOptionItems.get(0));
                break;
            case MenuActionItem.MENU_DELETE_FOLDER:
                onMenuItemClick = true;
                Fragment fr = ((Activity) context).getFragmentManager().findFragmentByTag(ChildrenBrowserFragment.TAG);
                NodeActions.delete((Activity) context, fr, selectedOptionItems.get(0));
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FAVORITES
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshOperations(){
        context.getLoaderManager().restartLoader(LOADER_OPERATION_ID, null, this);
        context.getLoaderManager().restartLoader(LOADER_SYNC_ID, null, this);
        notifyDataSetChanged();
    }
    
    private static class FavoriteInfo
    {
        long id;

        String nodeIdentifier;

        int status;

        boolean isFavorite;

        public FavoriteInfo(Cursor favoriteCursor)
        {
            this.id = favoriteCursor.getLong(SynchroSchema.COLUMN_NODE_ID_ID);
            this.nodeIdentifier = favoriteCursor.getString(SynchroSchema.COLUMN_NODE_ID_ID);
            this.status = favoriteCursor.getInt(SynchroSchema.COLUMN_STATUS_ID);
            this.isFavorite = favoriteCursor.getInt(SynchroSchema.COLUMN_IS_FAVORITE_ID) > 0;
        }
    }

    public boolean hasParentFavorite()
    {
        Cursor parentCursorId = null;
        isSyncFolder = false;
        try
        {
            parentCursorId = SynchroManager.getCursorForId(context, SessionUtils.getAccount(getContext()),
                    parentNode.getIdentifier());
            if (parentCursorId.getCount() == 1 && parentCursorId.moveToFirst())
            {
                isSyncFolder = true;
            }
        }
        catch (Exception e)
        {
            // do nothing
        }
        finally
        {
            CursorUtils.closeCursor(parentCursorId);
        }
        return isSyncFolder;
    }

    protected void displayStatut(ProgressViewHolder vh, int imageResource)
    {
        if (vh.iconBottomRight != null)
        {
            vh.iconBottomRight.setVisibility(View.VISIBLE);
            vh.iconBottomRight.setImageResource(imageResource);
        }
    }

}
