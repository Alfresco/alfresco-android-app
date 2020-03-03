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
package org.alfresco.mobile.android.application.fragments.sync;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.view.SyncConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions;
import org.alfresco.mobile.android.application.fragments.actions.NodeIdActions;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.node.sync.SyncNodeEvent;
import org.alfresco.mobile.android.async.node.update.UpdateNodeEvent;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.provider.MapUtil;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentProvider;
import org.alfresco.mobile.android.sync.SyncContentScanEvent;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.SyncScanInfo;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.ui.GridFragment;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.RefreshFragment;
import org.alfresco.mobile.android.ui.SelectableFragment;
import org.alfresco.mobile.android.ui.fragments.BaseCursorGridFragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SyncFragment extends BaseCursorGridFragment
        implements RefreshFragment, ListingModeFragment, GridFragment, SyncStatusObserver, SelectableFragment
{
    public static final String TAG = SyncFragment.class.getName();

    private static final String ARGUMENT_FOLDER_ID = "FolderId";

    private static final String ARGUMENT_FOLDER_NAME = "FolderName";

    protected static final String ARGUMENT_HIDE = "doesHideList";

    protected List<String> selectedItems = new ArrayList<String>(1);

    private NodeIdActions nActions;

    private boolean hasSynchroActive = false;

    private MenuItem mi;

    private Date downloadDateTime;

    private Date decryptDateTime;

    private AlfrescoAccount acc;

    private SyncScanInfo info;

    private Object mContentProviderHandle;

    private Boolean stopRefresh = false;

    private Runnable refreshTimer;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public SyncFragment()
    {
        super();
        mode = MODE_LISTING;
        checkSession = false;
        setHasOptionsMenu(true);
        displayAsList = true;
        screenName = AnalyticsManager.SCREEN_SYNCED_CONTENT;
    }

    protected static SyncFragment newInstanceByTemplate(Bundle b)
    {
        SyncFragment cbf = new SyncFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        acc = SessionUtils.getAccount(getActivity());

        adapter = onAdapterCreation();
        gv.setAdapter(adapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart()
    {
        acc = SessionUtils.getAccount(getActivity());
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }

    @Override
    public String onPrepareTitle()
    {
        if (getFolderName() != null)
        {
            title = getFolderName();
        }
        else
        {
            title = getString(R.string.synced_content);
        }
        return title;
    }

    @Override
    public void onResume()
    {
        getLoaderManager().restartLoader(0, null, this);

        hasSynchroActive = getMode() == MODE_PROGRESS
                || SyncContentManager.getInstance(getActivity()).hasActivateSync(acc);

        mContentProviderHandle = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE,
                this);
        if (isSyncActive())
        {
            startRefresh();
        }

        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        ContentResolver.removeStatusChangeListener(mContentProviderHandle);
    }

    @Override
    public void onStop()
    {
        if (nActions != null)
        {
            nActions.finish();
        }
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != RequestCode.SAVE_BACK && requestCode != RequestCode.DECRYPTED) { return; }

        final File dlFile = null;

        if (dlFile == null) { return; }

        long datetime = dlFile.lastModified();
        Date d = new Date(datetime);
        boolean modified;
        final Uri lUri = null;

        switch (requestCode)
        {
            case RequestCode.SAVE_BACK:

                modified = (d != null && downloadDateTime != null) ? d.after(downloadDateTime) : false;

                if (modified)
                {
                    // Update to Pending
                    ContentValues cValues = new ContentValues();
                    cValues.put(SyncContentSchema.COLUMN_STATUS, Operation.STATUS_PENDING);
                    getActivity().getContentResolver().update(lUri, cValues, null, null);

                    // Start sync if possible
                    if (SyncContentManager.getInstance(getActivity()).canSync(acc))
                    {
                        SyncContentManager.getInstance(getActivity()).sync(AnalyticsManager.LABEL_SYNC_SAVE_BACK, acc);
                    }
                }
                break;
            case RequestCode.DECRYPTED:

                modified = (d != null && decryptDateTime != null) ? d.after(decryptDateTime) : false;
                if (modified)
                {
                    // If modified by user, we flag the uri
                    // The next sync will update the content.
                    ContentValues cValues = new ContentValues();
                    cValues.put(SyncContentSchema.COLUMN_STATUS, SyncContentStatus.STATUS_MODIFIED);
                    getActivity().getContentResolver().update(lUri, cValues, null, null);
                }

                DataProtectionManager.getInstance(getActivity()).checkEncrypt(acc, dlFile);

                break;
            default:
                break;
        }
    }

    @Override
    protected BaseAdapter onAdapterCreation()
    {
        super.onAdapterCreation();
        if (adapter == null)
        {
            adapter = new SyncCursorAdapter(this, null, R.layout.row_two_lines_progress, selectedItems, getMode());
        }
        return adapter;
    }

    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setImageResource(R.drawable.ic_empty_folder_rw);
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        firstEmptyMessage.setText(R.string.sync_empty_title);
        secondEmptyMessage.setVisibility(View.VISIBLE);
        secondEmptyMessage.setText(R.string.sync_empty_description);
    }

    // /////////////////////////////////////////////////////////////
    // SYNC OBSERVER
    // ////////////////////////////////////////////////////////////
    private boolean isSyncActive()
    {
        try
        {
            return ContentResolver.isSyncActive(AlfrescoAccountManager.getInstance(getActivity())
                    .getAndroidAccount(SessionUtils.getAccount(getActivity()).getId()), SyncContentProvider.AUTHORITY);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public void onStatusChanged(int which)
    {
        updateRefresh(isSyncActive());
    }

    // Since onStatusChanged() is not called from the main thread
    // I need to update the ui in the ui-thread.
    private void updateRefresh(final boolean isSyncing)
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (isSyncing)
                {
                    // Log.d(TAG, "[Refresh] START");
                    // Start timing Refresh
                    startRefresh();
                }
                else
                {
                    // Stop Refresh
                    stopRefresh = true;
                    // Log.d(TAG, "[Refresh] STOP");

                    Bundle b = new Bundle();
                    b.putBoolean(ARGUMENT_HIDE, false);
                    getLoaderManager().restartLoader(0, b, SyncFragment.this);
                }
            }
        });
    }

    public void startRefresh()
    {
        refreshHandler.removeCallbacks(refreshTimer);
        stopRefresh = false;
        refreshTimer = new Runnable()
        {
            @Override
            public void run()
            {
                if (!isAdded()) { return; }
                Bundle b = new Bundle();
                b.putBoolean(ARGUMENT_HIDE, false);
                getLoaderManager().restartLoader(0, b, SyncFragment.this);

                if (stopRefresh != null && !stopRefresh)
                {
                    refreshHandler.postDelayed(this, 1000);
                }
            }
        };

        // start it with:
        refreshHandler.post(refreshTimer);
    }

    private Handler refreshHandler = new Handler();

    // /////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        if (args == null || !args.containsKey(ARGUMENT_HIDE))
        {
            setListShown(false);
        }
        StringBuilder selection = new StringBuilder();
        if (acc == null)
        {
            acc = getAccount();
        }
        if (acc != null)
        {
            selection.append(SyncContentProvider.getAccountFilter(acc));
        }

        if (selection.length() > 0)
        {
            selection.append(" AND ");
        }

        if (getFolderId() != null)
        {
            selection.append(SyncContentSchema.COLUMN_PARENT_ID).append(" == '").append(getFolderId()).append("'");
        }
        else
        {
            selection.append(SyncContentSchema.COLUMN_IS_SYNC_ROOT + " == '" + SyncContentProvider.FLAG_SYNC_SET + "'");
            selection.append(" OR ");
            selection.append(SyncContentSchema.COLUMN_STATUS + " == '" + SyncContentStatus.STATUS_REQUEST_USER + "'");
        }

        if (selection.length() > 0)
        {
            selection.append(" AND ");
        }

        selection.append(SyncContentSchema.COLUMN_STATUS + " NOT IN (" + SyncContentStatus.STATUS_HIDDEN + ")");

        return new CursorLoader(getActivity(), SyncContentProvider.CONTENT_URI, SyncContentSchema.COLUMN_ALL,
                selection.toString(), null, SyncContentSchema.COLUMN_TITLE + " COLLATE NOCASE ASC");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String nodeId = cursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID);
        String documentName = cursor.getString(SyncContentSchema.COLUMN_TITLE_ID);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
        }

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).equals(nodeId);
        }
        l.setItemChecked(position, true);

        if (nActions != null)
        {
            nActions.selectNode(nodeId);
            if (selectedItems.size() == 0)
            {
                hideDetails = true;
            }
        }
        else
        {
            selectedItems.clear();
            if (!hideDetails && DisplayUtils.hasCentralPane(getActivity()))
            {
                selectedItems.add(nodeId);
            }
        }

        if (hideDetails)
        {
            selectedItems.clear();
            displayTitle();
        }
        else if (nActions == null)
        {
            if (SyncContentManager.isFolder(cursor))
            {
                selectedItems.clear();
                if (SyncContentManager.getInstance(getActivity()).hasActivateSync(acc))
                {
                    // GO TO Local subfolder
                    SyncFragment.with(getActivity()).mode(getMode()).folderIdentifier(nodeId).folderName(documentName)
                            .display();
                }
                else
                {
                    DocumentFolderBrowserFragment.with(getActivity()).folderIdentifier(nodeId).shortcut(true).display();
                }
            }
            else
            {
                if (!ConnectivityUtils.hasInternetAvailable(getActivity()))
                {
                    NodeDetailsFragment.with(getActivity()).node(getOfflineNode(nodeId)).isFavorite(true).display();
                }
                else
                {
                    NodeDetailsFragment.with(getActivity()).nodeId(nodeId).isFavorite(true).display();
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    public boolean onListItemLongClick(GridView l, View v, int position, long id)
    {
        if (nActions != null || getFolderId() != null) { return false; }

        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String documentId = cursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID);

        selectedItems.clear();
        selectedItems.add(documentId);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new NodeIdActions(SyncFragment.this, selectedItems);
        nActions.setOnFinishModeListener(new AbstractActions.onFinishModeListener()
        {
            @Override
            public void onFinish()
            {
                nActions = null;
                selectedItems.clear();
                adapter.notifyDataSetChanged();
                ((SyncCursorAdapter) adapter).refresh();
                gv.setAdapter(adapter);
                displayFab(-1, null);
            }
        });

        displayFab(R.drawable.ic_done_all_white, onMultiSelectionFabClickListener());
        getActivity().startActionMode(nActions);
        adapter.notifyDataSetChanged();

        return true;
    }

    @Override
    public void selectAll()
    {
        if (nActions != null && adapter != null)
        {
            displayFab(R.drawable.ic_close_dark, onCancelMultiSelectionFabClickListener());
            nActions.selectNodes(((SyncCursorAdapter) adapter).getNodes());
            adapter.notifyDataSetChanged();
        }
    }

    protected View.OnClickListener onMultiSelectionFabClickListener()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectAll();
            }
        };
    }

    protected View.OnClickListener onCancelMultiSelectionFabClickListener()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (nActions != null)
                {
                    nActions.finish();
                }
            }
        };
    }

    private void displayFab(int iconId, View.OnClickListener listener)
    {
        if (listener != null)
        {
            fab.setVisibility(View.VISIBLE);
            fab.setImageResource(iconId);
            fab.setOnClickListener(listener);
            fab.show(true);
        }
        else
        {
            fab.setVisibility(View.GONE);
        }
    }

    public int getMode()
    {
        return getArguments().containsKey(ARGUMENT_MODE) ? getArguments().getInt(ARGUMENT_MODE) : mode;
    }

    public String getFolderId()
    {
        Bundle b = getArguments();
        return b.getString(ARGUMENT_FOLDER_ID);
    }

    public String getFolderName()
    {
        Bundle b = getArguments();
        return b.getString(ARGUMENT_FOLDER_NAME);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public void setDownloadDateTime(Date downloadDateTime)
    {
        this.downloadDateTime = downloadDateTime;
    }

    public void setDecryptDateTime(Date decryptDateTime)
    {
        this.decryptDateTime = decryptDateTime;
    }

    private Node getOfflineNode(String nodeIdentifier)
    {
        Node syncedNode = null;
        try
        {
            SyncContentManager syncManager = SyncContentManager.getInstance(getActivity());
            // Retrieve Sync Cursor for the specified node
            Uri localUri = syncManager.getUri(acc, nodeIdentifier);
            Cursor syncCursor = getActivity().getContentResolver().query(localUri, SyncContentSchema.COLUMN_ALL, null,
                    null, null);
            if (syncCursor.getCount() == 1 && syncCursor.moveToFirst())
            {
                Map<String, Serializable> properties = retrievePropertiesMap(syncCursor);
                syncedNode = new NodeSyncPlaceHolder(properties);
            }
            CursorUtils.closeCursor(syncCursor);
        }
        catch (Exception e)
        {
            // Do Nothing
        }

        return syncedNode;
    }

    protected Map<String, Serializable> retrievePropertiesMap(Cursor cursor)
    {
        // PROPERTIES
        String rawProperties = cursor.getString(OperationSchema.COLUMN_PROPERTIES_ID);
        if (rawProperties != null)
        {
            return MapUtil.stringToMap(rawProperties);
        }
        else
        {
            return new HashMap<>();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        info = SyncScanInfo.getLastSyncScanData(getActivity(), acc);
        if (info != null && (info.hasWarning() && !info.hasResponse()))
        {
            mi = menu.add(Menu.NONE, R.id.menu_sync_warning, Menu.FIRST, R.string.sync_warning);
            mi.setIcon(R.drawable.ic_warning);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        MenuFragmentHelper.getMenu(getActivity(), menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_sync_warning:
                displayWarning();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void awaitNextSync()
    {
        if (mi != null)
        {
            getActivity().invalidateOptionsMenu();
            info = null;
            mi.setActionView(R.layout.app_spinning);
        }
    }

    @Override
    public void refresh()
    {
        if (!ConnectivityUtils.hasNetwork(getActivity()))
        {
            if (mi != null)
            {
                mi.setActionView(null);
            }
            if (!ConnectivityUtils.hasNetwork(getActivity()))
            {
                Crouton.cancelAllCroutons();
                Crouton.showText(getActivity(),
                        Html.fromHtml(getString(org.alfresco.mobile.android.foundation.R.string.error_session_nodata)),
                        Style.INFO, (ViewGroup) (getRootView().getParent()));
            }
            refreshHelper.setRefreshComplete();
            return;
        }

        SyncContentManager.getInstance(getActivity()).sync(AnalyticsManager.LABEL_SYNC_REFRESH, acc);
        if (mi != null)
        {
            // Display spinning wheel instead of refresh
            mi.setActionView(R.layout.app_spinning);
        }

        if (adapter != null)
        {
            ((SyncCursorAdapter) adapter).refresh();
            gv.setAdapter(adapter);
        }
    }

    public void displayWarning()
    {
        if (info != null && info.hasWarning())
        {
            ErrorSyncDialogFragment.newInstance().show(getActivity().getSupportFragmentManager(),
                    ErrorSyncDialogFragment.TAG);
        }
    }

    public void select(Node updatedNode)
    {
        selectedItems.add(updatedNode.getIdentifier());
    }

    public void highLight(Node updatedNode)
    {
        selectedItems.clear();
        selectedItems.add(updatedNode.getIdentifier());
        adapter.notifyDataSetChanged();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onSyncCompleted(SyncContentScanEvent event)
    {
        if (mi != null)
        {
            // Hide spinning wheel
            mi.setActionView(null);
            info = SyncScanInfo.getLastSyncScanData(getActivity(), acc);
            getActivity().invalidateOptionsMenu();

            if (info.hasWarning())
            {
                ErrorSyncDialogFragment.newInstance().show(getActivity().getSupportFragmentManager(),
                        ErrorSyncDialogFragment.TAG);
            }
        }
    }

    @Subscribe
    public void onSyncNodeEvent(SyncNodeEvent event)
    {
        refreshSilently();
    }

    @Subscribe
    public void onDocumentUpdated(UpdateNodeEvent event)
    {
        if (event.hasException) { return; }
        Node updatedNode = event.data;
        if (updatedNode == null) { return; }

        Cursor syncCursor = null;
        try
        {
            syncCursor = getActivity().getContentResolver()
                    .query(SyncContentProvider.CONTENT_URI, SyncContentSchema.COLUMN_ALL,
                            SyncContentProvider.getAccountFilter(acc) + " AND " + SyncContentSchema.COLUMN_NODE_ID
                                    + " LIKE '" + NodeRefUtils.getCleanIdentifier(updatedNode.getIdentifier()) + "%'",
                            null, null);
            boolean hasSynced = (syncCursor.getCount() == 1);
            if (hasSynced && !hasSynchroActive)
            {
                syncCursor.moveToFirst();
                ContentValues cValues = new ContentValues();
                cValues.put(SyncContentSchema.COLUMN_NODE_ID, updatedNode.getIdentifier());
                cValues.put(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP,
                        updatedNode.getModifiedAt().getTimeInMillis());
                getActivity().getContentResolver().update(
                        SyncContentManager.getUri(syncCursor.getLong(SyncContentSchema.COLUMN_ID_ID)), cValues, null,
                        null);
            }
        }
        catch (Exception e)
        {
            // Do nothing
        }
        finally
        {
            CursorUtils.closeCursor(syncCursor);
        }
    }

    protected Fragment getFragment(String tag)
    {
        return getActivity().getSupportFragmentManager().findFragmentByTag(tag);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            viewConfigModel = new SyncConfigModel(configuration);
            sessionRequired = false;
            templateArguments = new String[] {};
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder mode(int mode)
        {
            extraConfiguration.putInt(ARGUMENT_MODE, mode);
            return this;
        }

        public Builder folderIdentifier(String folderIdentifier)
        {
            extraConfiguration.putString(ARGUMENT_FOLDER_ID, folderIdentifier);
            return this;
        }

        public Builder folderName(String folderName)
        {
            extraConfiguration.putString(ARGUMENT_FOLDER_NAME, folderName);
            return this;
        }
    }

    @Override
    protected void performRequest(ListingContext lcorigin)
    {
        getLoaderManager().initLoader(0, null, this);
    }
}
