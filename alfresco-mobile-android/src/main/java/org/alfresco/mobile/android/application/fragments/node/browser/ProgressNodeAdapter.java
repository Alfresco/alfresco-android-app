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
package org.alfresco.mobile.android.application.fragments.node.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.impl.publicapi.PublicAPIPropertyIds;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsContentProvider;
import org.alfresco.mobile.android.async.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.async.node.download.DownloadRequest;
import org.alfresco.mobile.android.async.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.async.utils.NodePlaceHolder;
import org.alfresco.mobile.android.platform.favorite.FavoritesProvider;
import org.alfresco.mobile.android.platform.favorite.FavoritesSchema;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentProvider;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.holder.TwoLinesProgressViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

    private static final int LOADER_FAVORITE_ID = 3;

    private static final int MAX_PROGRESS = 100;

    protected Node parentNode;

    private List<Node> selectedOptionItems = new ArrayList<Node>();

    private Map<String, Node> placeHolderMap = new HashMap<String, Node>();

    private Map<String, SyncInfo> syncInfos;

    private List<String> favoritesNodeIndex;

    private boolean hasSync = false, hasFavorited = false;

    public ProgressNodeAdapter(Fragment fr, int textViewResourceId, Node parentNode, List<Node> listItems,
            List<Node> selectedItems, int mode)
    {
        super(fr, textViewResourceId, listItems, selectedItems, mode);
        vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
        this.parentNode = parentNode;
        if (parentNode != null)
        {
            fr.getActivity().getLoaderManager().restartLoader(LOADER_OPERATION_ID, null, this);
            fr.getActivity().getLoaderManager().restartLoader(LOADER_SYNC_ID, null, this);
            fr.getActivity().getLoaderManager().restartLoader(LOADER_FAVORITE_ID, null, this);
            hasParentFavorite();
        }
    }

    public ProgressNodeAdapter(Activity context, int textViewResourceId, List<Node> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

    public ProgressNodeAdapter(Activity context, int textViewResourceId, Node parentNode, List<Node> listItems,
            Map<String, Node> selectedItems)
    {
        super(context, textViewResourceId, listItems, selectedItems);
        vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
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
    protected void updateTopText(TwoLinesProgressViewHolder vh, Node item)
    {
        ProgressBar progressView = vh.progress;

        if (item instanceof NodePlaceHolder)
        {
            vh.topText.setText(item.getName());
            vh.topText.setEnabled(false);
            long totalSize = ((NodePlaceHolder) item).getLength();

            if ((Integer) item.getPropertyValue(PublicAPIPropertyIds.REQUEST_STATUS) == Operation.STATUS_PAUSED
                    || (Integer) item.getPropertyValue(PublicAPIPropertyIds.REQUEST_STATUS) == Operation.STATUS_PENDING)
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
    protected void updateBottomText(TwoLinesProgressViewHolder vh, Node item)
    {
        if (favoritesNodeIndex != null && favoritesNodeIndex.contains(item.getIdentifier()))
        {
            vh.favoriteIcon.setVisibility(View.VISIBLE);
            vh.favoriteIcon.setImageResource(R.drawable.ic_favorite_light);
        }
        else
        {
            vh.favoriteIcon.setVisibility(View.GONE);
        }

        vh.syncIcon.setVisibility(View.GONE);
        if (hasSync && syncInfos.containsKey(item.getIdentifier()))
        {
            SyncInfo syncInfo = syncInfos.get(item.getIdentifier());

            if (syncInfo.isRoot == 1)
            {
                vh.syncIcon.setVisibility(View.VISIBLE);
                vh.syncIcon.setImageResource(R.drawable.ic_sync_light);
            }
            else
            {
                vh.syncIcon.setVisibility(View.GONE);
            }

            if (SyncContentManager.getInstance(getContext()).hasActivateSync(SessionUtils.getAccount(getContext())))
            {
                switch (syncInfo.status)
                {
                    case SyncContentStatus.STATUS_PENDING:
                        displayStatut(vh, R.drawable.sync_status_pending);
                        break;
                    case SyncContentStatus.STATUS_RUNNING:
                        displayStatut(vh, R.drawable.sync_status_loading);
                        break;
                    case SyncContentStatus.STATUS_PAUSED:
                        displayStatut(vh, R.drawable.sync_status_pending);
                        break;
                    case SyncContentStatus.STATUS_MODIFIED:
                        displayStatut(vh, R.drawable.sync_status_pending);
                        break;
                    case SyncContentStatus.STATUS_SUCCESSFUL:
                        displayStatut(vh, R.drawable.sync_status_success);
                        break;
                    case SyncContentStatus.STATUS_FAILED:
                        displayStatut(vh, R.drawable.sync_status_failed);
                        break;
                    case SyncContentStatus.STATUS_CANCEL:
                        displayStatut(vh, R.drawable.sync_status_failed);
                        break;
                    case SyncContentStatus.STATUS_REQUEST_USER:
                        displayStatut(vh, R.drawable.sync_status_failed);
                        break;
                    default:
                        vh.iconRight.setVisibility(View.GONE);
                        break;
                }
            }
            else
            {
                vh.iconRight.setVisibility(View.GONE);
            }
        }
        else
        {
            vh.iconRight.setVisibility(View.GONE);
        }

        if (item instanceof NodePlaceHolder)
        {
            vh.bottomText.setEnabled(false);
            int status = item.getPropertyValue(PublicAPIPropertyIds.REQUEST_STATUS);
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
    protected void updateIcon(TwoLinesProgressViewHolder vh, Node item)
    {
        if (item instanceof NodePlaceHolder)
        {
            UIUtils.setBackground(vh.icon, null);
            vh.icon.setImageResource(MimeTypeManager.getInstance(getActivity()).getIcon(item.getName()));
            vh.choose.setVisibility(View.GONE);
            return;
        }
        else
        {
            super.updateIcon(vh, item);
        }

        if (item.isFolder())
        {
            vh.icon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.mime_256_folder));

            if (mode == ListingModeFragment.MODE_IMPORT) { return; }
            if (mode == ListingModeFragment.MODE_PICK) { return; }

            vh.choose.setImageResource(R.drawable.ic_more_options);
            vh.choose.setBackgroundResource(R.drawable.alfrescohololight_list_selector_holo_light);
            vh.choose.setVisibility(View.VISIBLE);
            AccessibilityUtils.addContentDescription(vh.choose,
                    String.format(getActivity().getString(R.string.more_options_folder), item.getName()));
            vh.choose.setTag(R.id.node_action, item);
            vh.choose.setOnClickListener(new OnClickListener()
            {

                @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                @Override
                public void onClick(View v)
                {
                    Node item = (Node) v.getTag(R.id.node_action);
                    selectedOptionItems.add(item);
                    PopupMenu popup = new PopupMenu(getActivity(), v);
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
            UIUtils.setBackground(vh.choose, null);
        }
    }

    // /////////////////////////////////////////////////////////////
    // INLINE PROGRESS
    // ////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        switch (id)
        {
            case LOADER_OPERATION_ID:
                return new CursorLoader(getActivity(), OperationsContentProvider.CONTENT_URI,
                        OperationSchema.COLUMN_ALL, OperationSchema.COLUMN_PARENT_ID + "=\""
                                + parentNode.getIdentifier() + "\" AND " + OperationSchema.COLUMN_REQUEST_TYPE + " IN("
                                + CreateDocumentRequest.TYPE_ID + " , " + DownloadRequest.TYPE_ID + " , "
                                + UpdateContentRequest.TYPE_ID + ")", null, null);

            case LOADER_SYNC_ID:
                return new CursorLoader(
                        getActivity(),
                        SyncContentProvider.CONTENT_URI,
                        SyncContentSchema.COLUMN_ALL,
                        SyncContentSchema.COLUMN_PARENT_ID + " =\"" + parentNode.getIdentifier() + "\" AND "
                                + SyncContentSchema.COLUMN_STATUS + " NOT IN (" + SyncContentStatus.STATUS_HIDDEN + ")",
                        null, null);

            case LOADER_FAVORITE_ID:
                return new CursorLoader(getActivity(), FavoritesProvider.CONTENT_URI, FavoritesSchema.COLUMN_ALL,
                        FavoritesSchema.COLUMN_PARENT_ID + " =\"" + parentNode.getIdentifier() + "\"", null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        switch (loader.getId())
        {
            case LOADER_SYNC_ID:
                hasSync = (cursor.getCount() > 0);
                if (syncInfos == null)
                {
                    syncInfos = new HashMap<String, SyncInfo>(cursor.getCount());
                }
                syncInfos.clear();
                if (hasSync)
                {
                    while (cursor.moveToNext())
                    {
                        syncInfos.put(cursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID), new SyncInfo(cursor));
                    }
                }
                notifyDataSetChanged();
                break;

            case LOADER_FAVORITE_ID:
                hasFavorited = (cursor.getCount() > 0);
                if (favoritesNodeIndex == null)
                {
                    favoritesNodeIndex = new ArrayList<>(cursor.getCount());
                }
                favoritesNodeIndex.clear();
                if (hasFavorited)
                {
                    while (cursor.moveToNext())
                    {
                        favoritesNodeIndex.add(cursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID));
                    }
                }
                notifyDataSetChanged();
                break;

            case LOADER_OPERATION_ID:
                // Case where 0 ==> check if placeHolder present
                if (cursor.getCount() == 0)
                {
                    for (Entry<String, Node> entryNode : placeHolderMap.entrySet())
                    {
                        remove(entryNode.getValue());
                    }
                    placeHolderMap.clear();
                }

                Node placeHolder = null;
                while (cursor.moveToNext())
                {
                    int status = cursor.getInt(OperationSchema.COLUMN_STATUS_ID);
                    String name = cursor.getString(OperationSchema.COLUMN_TITLE_ID);
                    int type = cursor.getInt(OperationSchema.COLUMN_REQUEST_TYPE_ID);

                    // Log.d("UI", "[Update]" + name + "/" + status);

                    switch (status)
                    {
                        case Operation.STATUS_PAUSED:
                        case Operation.STATUS_PENDING:
                            // Add Node if not present
                            if (name != null && !hasNode(name))
                            {
                                // Log.d("UI", "[Init Placeholder]" + name);
                                placeHolder = new NodePlaceHolder(name, type, status);
                                placeHolderMap.put(name, placeHolder);
                                replaceNode(placeHolder);
                            }
                            break;
                        case Operation.STATUS_RUNNING:
                            // Update node if not present
                            long progress = cursor.getLong(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID);
                            long totalSize = cursor.getLong(OperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                            if (name != null)
                            {
                                // Log.d("UI", "[Update Placeholder]" + name +
                                // " : " + progress + "/" + totalSize);
                                placeHolder = new NodePlaceHolder(name, type, status, totalSize, progress);
                                placeHolderMap.put(name, placeHolder);
                                replaceNode(placeHolder);
                            }
                            break;
                        case Operation.STATUS_SUCCESSFUL:
                            Log.d("UI", "[Update Sucess]" + name);
                            // Update node if not present
                            if (type != DownloadRequest.TYPE_ID && hasNode(name)
                                    && getNode(name) instanceof NodePlaceHolder)
                            {
                                notifyDataSetChanged();
                            }
                            else if (hasNode(name) && getNode(name) instanceof NodePlaceHolder)
                            {
                                remove(name);
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

        Permissions permission = SessionUtils.getSession(getActivity()).getServiceRegistry().getDocumentFolderService()
                .getPermissions(node);

        mi = menu.add(Menu.NONE, R.id.menu_node_details, Menu.FIRST, R.string.action_view_properties);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (permission.canEdit())
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_edit, Menu.FIRST + 50, R.string.action_edit_properties);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (permission.canDelete())
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_delete_folder, Menu.FIRST + 1000, R.string.delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        switch (item.getItemId())
        {
            case R.id.menu_node_details:
                onMenuItemClick = true;
                NodeDetailsFragment.with(getActivity()).node(selectedOptionItems.get(0)).display();
                if (DisplayUtils.hasCentralPane(getActivity()))
                {
                    selectedItems.add(selectedOptionItems.get(0));
                }
                notifyDataSetChanged();
                break;
            case R.id.menu_action_edit:
                onMenuItemClick = true;
                NodeActions.edit(getActivity(), (Folder) parentNode, selectedOptionItems.get(0));
                break;
            case R.id.menu_action_delete_folder:
                onMenuItemClick = true;
                Fragment fr = getActivity().getFragmentManager().findFragmentByTag(DocumentFolderBrowserFragment.TAG);
                NodeActions.delete(getActivity(), fr, selectedOptionItems.get(0));
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
    public void refreshOperations()
    {
        getActivity().getLoaderManager().restartLoader(LOADER_OPERATION_ID, null, this);
        getActivity().getLoaderManager().restartLoader(LOADER_SYNC_ID, null, this);
        getActivity().getLoaderManager().restartLoader(LOADER_FAVORITE_ID, null, this);
        notifyDataSetChanged();
    }

    private static class SyncInfo
    {
        long id;

        String nodeIdentifier;

        int status;

        int isRoot;

        public SyncInfo(Cursor syncCursor)
        {
            this.id = syncCursor.getLong(SyncContentSchema.COLUMN_NODE_ID_ID);
            this.nodeIdentifier = syncCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID);
            this.status = syncCursor.getInt(SyncContentSchema.COLUMN_STATUS_ID);
            this.isRoot = syncCursor.getInt(SyncContentSchema.COLUMN_IS_SYNC_ROOT_ID);
        }
    }

    public boolean hasParentFavorite()
    {
        Cursor parentCursorId = null;
        boolean isSyncFolder = false;
        try
        {
            parentCursorId = SyncContentManager.getCursorForId(getActivity(), SessionUtils.getAccount(getContext()),
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

    protected void displayStatut(TwoLinesProgressViewHolder vh, int imageResource)
    {
        if (vh.iconRight != null)
        {
            vh.iconRight.setVisibility(View.VISIBLE);
            vh.iconRight.setImageResource(imageResource);
        }
    }

}
