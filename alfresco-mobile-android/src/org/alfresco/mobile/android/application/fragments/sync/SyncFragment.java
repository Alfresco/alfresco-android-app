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
package org.alfresco.mobile.android.application.fragments.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.GridAdapterHelper;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.fragments.actions.NodeIdActions;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.ProgressNodeAdapter;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.sync.EnableSyncDialogFragment.OnSyncChangeListener;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeEvent;
import org.alfresco.mobile.android.async.node.update.UpdateNodeEvent;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.FavoritesSyncProvider;
import org.alfresco.mobile.android.sync.FavoritesSyncScanEvent;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;
import org.alfresco.mobile.android.sync.SyncScanInfo;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncStatus;
import org.alfresco.mobile.android.ui.GridFragment;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.RefreshFragment;
import org.alfresco.mobile.android.ui.fragments.BaseCursorGridFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

public class SyncFragment extends BaseCursorGridFragment implements RefreshFragment, ListingModeFragment, GridFragment,
        SyncStatusObserver
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

    private File localFile;

    private Uri favoriteUri;

    private Date decryptDateTime;

    private AlfrescoAccount acc;

    private SyncScanInfo info;

    private Object mContentProviderHandle;

    private boolean stopRefresh = false;

    private Runnable refreshTimer;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public SyncFragment()
    {
        super();
        emptyListMessageId = R.string.empty_favorites;
        checkSession = false;
        setHasOptionsMenu(true);
    }

    public static SyncFragment newInstance(int mode)
    {
        SyncFragment bf = new SyncFragment();
        Bundle settings = new Bundle();
        settings.putInt(ARGUMENT_MODE, mode);
        bf.setArguments(settings);
        return bf;
    }

    public static SyncFragment newInstance(int mode, String folderId, String folderName)
    {
        SyncFragment bf = new SyncFragment();
        Bundle settings = new Bundle();
        settings.putInt(ARGUMENT_MODE, mode);
        settings.putString(ARGUMENT_FOLDER_ID, folderId);
        settings.putString(ARGUMENT_FOLDER_NAME, folderName);
        bf.setArguments(settings);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        acc = SessionUtils.getAccount(getActivity());

        int[] layouts = GridAdapterHelper.getGridLayoutId(getActivity(), this);
        adapter = new SyncCursorAdapter(this, null, layouts[0], selectedItems, getMode());
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
    public void onResume()
    {
        displayActivateDialog();

        if (getMode() != MODE_PROGRESS)
        {
            hasSynchroActive = FavoritesSyncManager.getInstance(getActivity()).hasActivateSync(acc);
        }
        else
        {
            hasSynchroActive = true;
        }

        int titleId = R.string.menu_favorites;
        if (hasSynchroActive)
        {
            titleId = R.string.synced_documents;
        }

        if (getFolderName() != null)
        {
            UIUtils.displayTitle(getActivity(), getFolderName());
        }
        else
        {
            UIUtils.displayTitle(getActivity(), getString(titleId));
        }

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

        final File dlFile = localFile;

        if (dlFile == null) { return; }

        long datetime = dlFile.lastModified();
        Date d = new Date(datetime);
        boolean modified = false;
        final Uri lUri = favoriteUri;

        switch (requestCode)
        {
            case RequestCode.SAVE_BACK:

                modified = (d != null && downloadDateTime != null) ? d.after(downloadDateTime) : false;

                if (modified)
                {
                    // Update to Pending
                    ContentValues cValues = new ContentValues();
                    cValues.put(FavoritesSyncSchema.COLUMN_STATUS, Operation.STATUS_PENDING);
                    getActivity().getContentResolver().update(lUri, cValues, null, null);

                    // Start sync if possible
                    if (FavoritesSyncManager.getInstance(getActivity()).canSync(acc))
                    {
                        FavoritesSyncManager.getInstance(getActivity()).sync(acc);
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
                    cValues.put(FavoritesSyncSchema.COLUMN_STATUS, FavoriteSyncStatus.STATUS_MODIFIED);
                    getActivity().getContentResolver().update(lUri, cValues, null, null);
                }

                DataProtectionManager.getInstance(getActivity()).checkEncrypt(acc, dlFile);

                break;
            default:
                break;
        }
    }

    // /////////////////////////////////////////////////////////////
    // SYNC OBSERVER
    // ////////////////////////////////////////////////////////////
    private boolean isSyncActive()
    {
        try
        {
            return ContentResolver.isSyncActive(
                    AlfrescoAccountManager.getInstance(getActivity()).getAndroidAccount(
                            SessionUtils.getAccount(getActivity()).getId()), FavoritesSyncProvider.AUTHORITY);
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
                    Log.d(TAG, "[Refresh] START");
                    // Start timing Refresh
                    startRefresh();
                }
                else
                {
                    // Stop Refresh
                    stopRefresh = true;
                    Log.d(TAG, "[Refresh] STOP");

                    Log.d(TAG, "Refresh");
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

                if (!stopRefresh)
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
    // UTILS
    // ////////////////////////////////////////////////////////////
    private void displayActivateDialog()
    {
        if (!FavoritesSyncManager.getInstance(getActivity()).hasDisplayedActivateSync() && getMode() != MODE_PROGRESS)
        {
            EnableSyncDialogFragment.newInstance(new OnSyncChangeListener()
            {
                @Override
                public void onPositive()
                {
                    FavoritesSyncManager.getInstance(getActivity()).setActivateSync(true);
                    hasSynchroActive = true;
                    refresh();
                }

                @Override
                public void onNegative()
                {
                    hasSynchroActive = false;
                    FavoritesSyncManager.getInstance(getActivity()).setActivateSync(false);
                    refresh();
                }
            }).show(getActivity().getFragmentManager(), EnableSyncDialogFragment.TAG);
            FavoritesSyncManager.getInstance(getActivity()).setDisplayActivateSync(true);
        }
    }

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
        if (acc != null)
        {
            selection.append(FavoritesSyncProvider.getAccountFilter(acc));
        }

        if (selection.length() > 0)
        {
            selection.append(" AND ");
        }

        if (getFolderId() != null)
        {
            selection.append(FavoritesSyncSchema.COLUMN_PARENT_ID + " == '" + getFolderId() + "'");
        }
        else
        {
            selection.append(FavoritesSyncSchema.COLUMN_IS_FAVORITE + " == '" + FavoritesSyncProvider.FLAG_FAVORITE
                    + "'");
            selection.append(" OR ");
            selection
                    .append(FavoritesSyncSchema.COLUMN_STATUS + " == '" + FavoriteSyncStatus.STATUS_REQUEST_USER + "'");
        }

        if (selection.length() > 0)
        {
            selection.append(" AND ");
        }

        selection.append(FavoritesSyncSchema.COLUMN_STATUS + " NOT IN (" + FavoriteSyncStatus.STATUS_HIDDEN + ")");

        Log.d(TAG, selection.toString());

        return new CursorLoader(getActivity(), FavoritesSyncProvider.CONTENT_URI, FavoritesSyncSchema.COLUMN_ALL,
                selection.toString(), null, FavoritesSyncSchema.COLUMN_TITLE + " COLLATE NOCASE ASC");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String nodeId = cursor.getString(FavoritesSyncSchema.COLUMN_NODE_ID_ID);
        String documentName = cursor.getString(FavoritesSyncSchema.COLUMN_TITLE_ID);

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
        }
        else if (nActions == null)
        {
            if (FavoritesSyncManager.isFolder(cursor))
            {
                selectedItems.clear();
                if (FavoritesSyncManager.getInstance(getActivity()).hasActivateSync(acc))
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
                // Show properties
                NodeDetailsFragment.with(getActivity()).nodeId(nodeId).isFavorite(true).display();
            }
        }
        adapter.notifyDataSetChanged();
    }

    public boolean onListItemLongClick(GridView l, View v, int position, long id)
    {
        if (nActions != null) { return false; }

        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String documentId = cursor.getString(FavoritesSyncSchema.COLUMN_NODE_ID_ID);

        selectedItems.clear();
        selectedItems.add(documentId);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new NodeIdActions(SyncFragment.this, selectedItems);
        nActions.setOnFinishModeListerner(new onFinishModeListerner()
        {
            @Override
            public void onFinish()
            {
                nActions = null;
                selectedItems.clear();
                adapter.notifyDataSetChanged();
                ((SyncCursorAdapter) adapter).refresh();
                gv.setAdapter(adapter);
            }
        });
        getActivity().startActionMode(nActions);
        adapter.notifyDataSetChanged();

        return true;
    };

    public int getMode()
    {
        Bundle b = getArguments();
        return b.getInt(ARGUMENT_MODE);
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

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public void getMenu(Menu menu)
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

    @Override
    public void refresh()
    {
        if (!ConnectivityUtils.hasNetwork((BaseActivity) getActivity()))
        {
            mi.setActionView(null);
            return;
        }

        FavoritesSyncManager.getInstance(getActivity()).sync(acc);
        if (mi != null)
        {
            // Display spinning wheel instead of refresh
            mi.setActionView(R.layout.app_spinning);
        }
        ((SyncCursorAdapter) adapter).refresh();
        gv.setAdapter(adapter);
    }

    public void displayWarning()
    {
        if (info != null && info.hasWarning())
        {
            ErrorSyncDialogFragment.newInstance().show(getActivity().getFragmentManager(), ErrorSyncDialogFragment.TAG);
            return;
        }
    }

    public void select(Node updatedNode)
    {
        selectedItems.add(updatedNode.getIdentifier());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onSyncCompleted(FavoritesSyncScanEvent event)
    {
        if (mi != null)
        {
            // Hide spinning wheel
            mi.setActionView(null);
            info = SyncScanInfo.getLastSyncScanData(getActivity(), acc);
            getActivity().invalidateOptionsMenu();

            if (info.hasWarning())
            {
                ErrorSyncDialogFragment.newInstance().show(getActivity().getFragmentManager(),
                        ErrorSyncDialogFragment.TAG);
                return;
            }
        }
    }

    @Subscribe
    public void onFavoriteNodeEvent(FavoriteNodeEvent event)
    {
        getLoaderManager().initLoader(0, null, this);
    }
    

    @Subscribe
    public void onDocumentUpdated(UpdateNodeEvent event)
    {
        if (event.hasException) { return; }
        Node updatedNode = event.data;
        if (updatedNode == null) { return; }

        Cursor favoriteCursor = null;
        try
        {
            favoriteCursor = getActivity().getContentResolver().query(
                    FavoritesSyncProvider.CONTENT_URI,
                    FavoritesSyncSchema.COLUMN_ALL,
                    FavoritesSyncProvider.getAccountFilter(acc) + " AND " + FavoritesSyncSchema.COLUMN_NODE_ID
                            + " LIKE '" + NodeRefUtils.getCleanIdentifier(updatedNode.getIdentifier()) + "%'", null,
                    null);
            boolean hasFavorite = (favoriteCursor.getCount() == 1);
            if (hasFavorite && !hasSynchroActive)
            {
                favoriteCursor.moveToFirst();
                ContentValues cValues = new ContentValues();
                cValues.put(FavoritesSyncSchema.COLUMN_NODE_ID, updatedNode.getIdentifier());
                cValues.put(FavoritesSyncSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, updatedNode.getModifiedAt()
                        .getTimeInMillis());
                getActivity().getContentResolver().update(
                        FavoritesSyncManager.getUri(favoriteCursor.getLong(FavoritesSyncSchema.COLUMN_ID_ID)), cValues,
                        null, null);
            }
        }
        catch (Exception e)
        {
            // Do nothing
        }
        finally
        {
            CursorUtils.closeCursor(favoriteCursor);
        }
    }

    protected Fragment getFragment(String tag)
    {
        return getActivity().getFragmentManager().findFragmentByTag(tag);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        protected int mode = ListingModeFragment.MODE_LISTING;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);

            menuIconId = R.drawable.ic_favorite;
            menuTitleId = R.string.menu_favorites;
            templateArguments = new String[] {};
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstance(mode);
        };

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
