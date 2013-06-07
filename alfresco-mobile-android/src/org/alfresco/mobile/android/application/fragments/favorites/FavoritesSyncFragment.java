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
package org.alfresco.mobile.android.application.fragments.favorites;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.BaseCursorListFragment;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.actions.NodeIdActions;
import org.alfresco.mobile.android.application.fragments.actions.NodeIdActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.fragments.favorites.ActivateSyncDialogFragment.onFavoriteChangeListener;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class FavoritesSyncFragment extends BaseCursorListFragment implements RefreshFragment, ListingModeFragment
{
    public static final String TAG = FavoritesSyncFragment.class.getName();

    protected List<String> selectedItems = new ArrayList<String>(1);

    private NodeIdActions nActions;

    private boolean hasSynchroActive = false;

    private MenuItem mi;

    private FavoriteSyncReceiver receiver;

    private Date downloadDateTime;

    private File localFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FavoritesSyncFragment()
    {
        super();
        emptyListMessageId = R.string.empty_favorites;
    }

    public static FavoritesSyncFragment newInstance(int mode)
    {
        FavoritesSyncFragment bf = new FavoritesSyncFragment();
        Bundle settings = new Bundle();
        settings.putInt(PARAM_MODE, mode);
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

        adapter = new FavoriteCursorAdapter(this, null, R.layout.app_list_progress_row, selectedItems, getMode());
        lv.setAdapter(adapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart()
    {
        getActivity().setTitle(R.string.menu_favorites);
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
            receiver = new FavoriteSyncReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
        }
        
        if (getMode() != MODE_PROGRESS)
        {
            hasSynchroActive = GeneralPreferences
                    .hasActivateSync(getActivity(), SessionUtils.getAccount(getActivity()));
        }
        else
        {
            hasSynchroActive = true;
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
        switch (requestCode)
        {
            case PublicIntent.REQUESTCODE_SAVE_BACK:
                final File dlFile = localFile;

                long datetime = dlFile.lastModified();
                Date d = new Date(datetime);

                boolean modified = (d != null && downloadDateTime != null) ? d.after(downloadDateTime) : false;

                if (modified && SynchroManager.getInstance(getActivity()).canSync(SessionUtils.getAccount(getActivity())))
                {
                    SynchroManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()));
                }
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
            ActivateSyncDialogFragment.newInstance(new onFavoriteChangeListener()
            {
                @Override
                public void onPositive()
                {
                    refresh();
                    GeneralPreferences.setActivateSync(getActivity(), true);
                }

                @Override
                public void onNegative()
                {
                    GeneralPreferences.setActivateSync(getActivity(), false);
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
        return new CursorLoader(getActivity(), SynchroProvider.CONTENT_URI, SynchroSchema.COLUMN_ALL, null, null,
                SynchroSchema.COLUMN_TITLE + " COLLATE NOCASE ASC");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String documentId = cursor.getString(SynchroSchema.COLUMN_NODE_ID_ID);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
        }

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).equals(documentId);
        }
        l.setItemChecked(position, true);

        if (nActions != null)
        {
            nActions.selectNode(documentId);
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
                selectedItems.add(documentId);
            }
        }

        if (hideDetails)
        {
            selectedItems.clear();
        }
        else if (nActions == null)
        {

            localFile = null;
            Uri localFileUri = Uri.parse(cursor.getString(SynchroSchema.COLUMN_LOCAL_URI_ID));
            if (localFileUri != null && !localFileUri.getPath().isEmpty())
            {
                localFile = new File(localFileUri.getPath());
            }

            if (hasSynchroActive)
            {
                long datetime = localFile.lastModified();
                setDownloadDateTime(new Date(datetime));
                ActionManager.openIn(this, localFile, MimeTypeManager.getMIMEType(localFile.getName()),
                        PublicIntent.REQUESTCODE_SAVE_BACK);
            }
            else
            {
                // Show properties
                ((MainActivity) getActivity()).addPropertiesFragment(documentId);
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public boolean onItemLongClick(ListView l, View v, int position, long id)
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
                refreshListView();
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

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public void setDownloadDateTime(Date downloadDateTime)
    {
        this.downloadDateTime = downloadDateTime;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public void getMenu(Menu menu)
    {
        mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, Menu.FIRST + MenuActionItem.MENU_REFRESH,
                R.string.refresh);
        mi.setIcon(R.drawable.ic_refresh);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public void refresh()
    {
        SynchroManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()), true);
        if (mi != null)
        {
            // Display spinning wheel instead of refresh
            mi.setActionView(R.layout.spinning);
        }
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

            if (mi != null && intent.getAction().equals(IntentIntegrator.ACTION_SYNC_SCAN_COMPLETED))
            {
                // Hide spinning wheel
                mi.setActionView(null);
            }
        }
    }
}
