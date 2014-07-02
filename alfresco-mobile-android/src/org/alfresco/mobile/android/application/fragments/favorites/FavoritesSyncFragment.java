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
package org.alfresco.mobile.android.application.fragments.favorites;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.BaseCursorGridAdapterHelper;
import org.alfresco.mobile.android.application.fragments.BaseCursorGridFragment;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.GridFragment;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.fragments.actions.NodeIdActions;
import org.alfresco.mobile.android.application.fragments.favorites.ActivateSyncDialogFragment.OnSyncChangeListener;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

public class FavoritesSyncFragment extends BaseCursorGridFragment implements RefreshFragment, ListingModeFragment,
        GridFragment
{
    public static final String TAG = FavoritesSyncFragment.class.getName();

    private static final String PARAM_FOLDER_ID = "FolderId";

    private static final String PARAM_FOLDER_NAME = "FolderName";

    protected List<String> selectedItems = new ArrayList<String>(1);

    private NodeIdActions nActions;

    private boolean hasSynchroActive = false;

    private MenuItem mi;

    private FavoriteSyncReceiver receiver;

    private Date downloadDateTime;

    private File localFile;

    private Uri favoriteUri;

    private Date decryptDateTime;

    private Account acc;

    private SyncScanInfo info;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FavoritesSyncFragment()
    {
        super();
        emptyListMessageId = R.string.empty_favorites;
        checkSession = false;
    }

    public static FavoritesSyncFragment newInstance(int mode)
    {
        FavoritesSyncFragment bf = new FavoritesSyncFragment();
        Bundle settings = new Bundle();
        settings.putInt(PARAM_MODE, mode);
        bf.setArguments(settings);
        return bf;
    }

    public static FavoritesSyncFragment newInstance(int mode, String folderId, String folderName)
    {
        FavoritesSyncFragment bf = new FavoritesSyncFragment();
        Bundle settings = new Bundle();
        settings.putInt(PARAM_MODE, mode);
        settings.putString(PARAM_FOLDER_ID, folderId);
        settings.putString(PARAM_FOLDER_NAME, folderName);
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

        int[] layouts = BaseCursorGridAdapterHelper.getGridLayoutId(getActivity(), this);
        adapter = new FavoriteCursorAdapter(this, null, layouts[0], selectedItems, getMode());
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

        if (receiver == null)
        {
            IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_SYNC_SCAN_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_DECRYPT_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_ENCRYPT_COMPLETED);
            receiver = new FavoriteSyncReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
        }

        if (getMode() != MODE_PROGRESS)
        {
            hasSynchroActive = GeneralPreferences.hasActivateSync(getActivity(), acc);
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

        super.onResume();
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

    public void onDestroy()
    {
        if (receiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != PublicIntent.REQUESTCODE_SAVE_BACK && requestCode != PublicIntent.REQUESTCODE_DECRYPTED) { return; }

        final File dlFile = localFile;

        if (dlFile == null) { return; }

        long datetime = dlFile.lastModified();
        Date d = new Date(datetime);
        boolean modified = false;
        final Uri lUri = favoriteUri;

        switch (requestCode)
        {
            case PublicIntent.REQUESTCODE_SAVE_BACK:

                modified = (d != null && downloadDateTime != null) ? d.after(downloadDateTime) : false;

                if (modified)
                {
                    // Update to Pending
                    ContentValues cValues = new ContentValues();
                    cValues.put(SynchroSchema.COLUMN_STATUS, Operation.STATUS_PENDING);
                    getActivity().getContentResolver().update(lUri, cValues, null, null);

                    // Start sync if possible
                    if (SynchroManager.getInstance(getActivity()).canSync(acc))
                    {
                        SynchroManager.getInstance(getActivity()).sync(acc);
                    }
                }
                break;
            case PublicIntent.REQUESTCODE_DECRYPTED:

                modified = (d != null && decryptDateTime != null) ? d.after(decryptDateTime) : false;
                if (modified)
                {
                    // If modified by user, we flag the uri
                    // The next sync will update the content.
                    ContentValues cValues = new ContentValues();
                    cValues.put(SynchroSchema.COLUMN_STATUS, SyncOperation.STATUS_MODIFIED);
                    getActivity().getContentResolver().update(lUri, cValues, null, null);
                }

                DataProtectionManager.getInstance(getActivity()).checkEncrypt(acc, dlFile);

                break;
            default:
                break;
        }
    }

    // /////////////////////////////////////////////////////////////
    // UTILS
    // ////////////////////////////////////////////////////////////
    private void displayActivateDialog()
    {
        if (!GeneralPreferences.hasDisplayedActivateSync(getActivity()) && getMode() != MODE_PROGRESS)
        {
            ActivateSyncDialogFragment.newInstance(new OnSyncChangeListener()
            {
                @Override
                public void onPositive()
                {
                    GeneralPreferences.setActivateSync(getActivity(), true);
                    hasSynchroActive = true;
                    refresh();
                }

                @Override
                public void onNegative()
                {
                    hasSynchroActive = false;
                    GeneralPreferences.setActivateSync(getActivity(), false);
                    refresh();
                }
            }).show(getActivity().getFragmentManager(), ActivateSyncDialogFragment.TAG);
            GeneralPreferences.setDisplayActivateSync(getActivity(), true);
        }
    }

    // /////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);
        StringBuilder selection = new StringBuilder();
        if (acc != null)
        {
            selection.append(SynchroProvider.getAccountFilter(acc));
        }

        if (selection.length() > 0)
        {
            selection.append(" AND ");
        }

        if (getFolderId() != null)
        {
            selection.append(SynchroSchema.COLUMN_PARENT_ID + " == '" + getFolderId() + "'");
        }
        else
        {
            selection.append(SynchroSchema.COLUMN_IS_FAVORITE + " == '" + SynchroProvider.FLAG_FAVORITE + "'");
            selection.append(" OR ");
            selection.append(SynchroSchema.COLUMN_STATUS + " == '" + SyncOperation.STATUS_REQUEST_USER + "'");
        }

        if (selection.length() > 0)
        {
            selection.append(" AND ");
        }

        selection.append(SynchroSchema.COLUMN_STATUS + " NOT IN (" + SyncOperation.STATUS_HIDDEN + ")");

        Log.d(TAG, selection.toString());

        return new CursorLoader(getActivity(), SynchroProvider.CONTENT_URI, SynchroSchema.COLUMN_ALL,
                selection.toString(), null, SynchroSchema.COLUMN_TITLE + " COLLATE NOCASE ASC");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String nodeId = cursor.getString(SynchroSchema.COLUMN_NODE_ID_ID);
        String documentName = cursor.getString(SynchroSchema.COLUMN_TITLE_ID);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
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
            if (SynchroManager.isFolder(cursor))
            {
                selectedItems.clear();
                if (SynchroManager.getInstance(getActivity()).hasActivateSync(acc))
                {
                    // GO TO Local subfolder
                    Fragment syncFrag = FavoritesSyncFragment.newInstance(getMode(), nodeId, documentName);
                    FragmentDisplayer.replaceFragment(getActivity(), syncFrag,
                            DisplayUtils.getLeftFragmentId(getActivity()), FavoritesSyncFragment.TAG, true);
                }
                else
                {
                    ((MainActivity) getActivity()).addNavigationFragmentById(nodeId);
                }
            }
            else
            {
                // Show properties
                ((MainActivity) getActivity()).addPropertiesFragment(true, nodeId);
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public boolean onItemLongClick(GridView l, View v, int position, long id)
    {
        if (nActions != null) { return false; }

        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String documentId = cursor.getString(SynchroSchema.COLUMN_NODE_ID_ID);

        selectedItems.clear();
        selectedItems.add(documentId);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new NodeIdActions(FavoritesSyncFragment.this, selectedItems);
        nActions.setOnFinishModeListerner(new onFinishModeListerner()
        {
            @Override
            public void onFinish()
            {
                nActions = null;
                selectedItems.clear();
                adapter.notifyDataSetChanged();
                ((FavoriteCursorAdapter) adapter).refresh();
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
        return b.getInt(PARAM_MODE);
    }

    public String getFolderId()
    {
        Bundle b = getArguments();
        return b.getString(PARAM_FOLDER_ID);
    }

    public String getFolderName()
    {
        Bundle b = getArguments();
        return b.getString(PARAM_FOLDER_NAME);
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
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_SYNC_WARNING, Menu.FIRST + MenuActionItem.MENU_SYNC_WARNING,
                    R.string.sync_warning);
            mi.setIcon(R.drawable.ic_warning);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, MenuActionItem.MENU_SYNC_WARNING
                + MenuActionItem.MENU_REFRESH, R.string.refresh);
        mi.setIcon(R.drawable.ic_refresh);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public void refresh()
    {
        if (!ConnectivityUtils.hasNetwork((BaseActivity) getActivity()))
        {
            mi.setActionView(null);
            return;
        }

        SynchroManager.getInstance(getActivity()).sync(acc);
        if (mi != null)
        {
            // Display spinning wheel instead of refresh
            mi.setActionView(R.layout.app_spinning);
        }
        ((FavoriteCursorAdapter) adapter).refresh();
        gv.setAdapter(adapter);
    }

    public void displayWarning()
    {
        if (info != null && info.hasWarning())
        {
            SyncErrorDialogFragment.newInstance().show(getActivity().getFragmentManager(), SyncErrorDialogFragment.TAG);
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
    private class FavoriteSyncReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());

            if (intent.getAction() == null) { return; }

            if (mi != null && getActivity() != null && intent.getAction().equals(IntentIntegrator.ACTION_SYNC_SCAN_COMPLETED))
            {
                // Hide spinning wheel
                mi.setActionView(null);
                info = SyncScanInfo.getLastSyncScanData(context, acc);
                getActivity().invalidateOptionsMenu();

                if (info.hasWarning())
                {
                    SyncErrorDialogFragment.newInstance().show(getActivity().getFragmentManager(),
                            SyncErrorDialogFragment.TAG);
                    return;
                }
            }

            if (acc == null) { return; }

            if (intent.getAction().equals(IntentIntegrator.ACTION_UPDATE_COMPLETED))
            {
                Bundle b = intent.getExtras().getParcelable(IntentIntegrator.EXTRA_DATA);
                Node updatedNode = (Node) b.getParcelable(IntentIntegrator.EXTRA_UPDATED_NODE);
                if (updatedNode == null) { return; }

                Cursor favoriteCursor = context.getContentResolver().query(
                        SynchroProvider.CONTENT_URI,
                        SynchroSchema.COLUMN_ALL,
                        SynchroProvider.getAccountFilter(acc) + " AND " + SynchroSchema.COLUMN_NODE_ID + " LIKE '"
                                + NodeRefUtils.getCleanIdentifier(updatedNode.getIdentifier()) + "%'", null, null);
                boolean hasFavorite = (favoriteCursor.getCount() == 1);
                if (hasFavorite && !hasSynchroActive)
                {
                    favoriteCursor.moveToFirst();
                    ContentValues cValues = new ContentValues();
                    cValues.put(SynchroSchema.COLUMN_NODE_ID, updatedNode.getIdentifier());
                    cValues.put(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, updatedNode.getModifiedAt()
                            .getTimeInMillis());
                    context.getContentResolver().update(
                            SynchroManager.getUri(favoriteCursor.getLong(SynchroSchema.COLUMN_ID_ID)), cValues, null,
                            null);
                }
                favoriteCursor.close();
            }
        }
    }

    protected Fragment getFragment(String tag)
    {
        return getActivity().getFragmentManager().findFragmentByTag(tag);
    }
}
