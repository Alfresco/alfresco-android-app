/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.browser.local;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.NodeChildrenLoader;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.browser.local.FileActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.filebrowser.LocalFileExplorerFragment;
import org.alfresco.mobile.android.ui.filebrowser.LocalFileExplorerLoader;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;
import org.alfresco.mobile.android.ui.manager.StorageManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class LocalFileBrowserFragment extends LocalFileExplorerFragment
{

    public static final String TAG = "LocalFileNavigationFragment";

    public LocalFileBrowserFragment()
    {
        loaderId = NodeChildrenLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_download;
        initLoader = false;
        checkSession = false;
    }

    public static LocalFileBrowserFragment newInstance(File folder)
    {
        return newInstance(folder, null);
    }

    public static LocalFileBrowserFragment newInstance(String folderPath)
    {
        return newInstance(null, folderPath);
    }

    public static LocalFileBrowserFragment newInstance(File parentFolder, String pathFolder)
    {
        LocalFileBrowserFragment bf = new LocalFileBrowserFragment();
        ListingContext lc = new ListingContext();
        lc.setSortProperty(DocumentFolderService.SORT_PROPERTY_NAME);
        lc.setIsSortAscending(true);
        Bundle b = new Bundle(createBundleArgs(lc, LOAD_AUTO));
        b.putAll(createBundleArgs(parentFolder, pathFolder));
        bf.setArguments(b);
        return bf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle b = getArguments();
        if (b == null)
        {
            Account acc = ((MainActivity) getActivity()).getAccount();
            b = createBundleArgs(StorageManager.getDownloadFolder(getActivity(), acc.getUrl(), acc.getUsername()));
        }

        getLoaderManager().initLoader(LocalFileExplorerLoader.ID, b, this);
        getLoaderManager().getLoader(LocalFileExplorerLoader.ID).forceLoad();

    }

    @Override
    public void onStart()
    {
        DisplayUtils.setLeftTitle(getActivity(), title);
        getActivity().invalidateOptionsMenu();
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        File file = (File) l.getItemAtPosition(position);
        if (file.isDirectory())
        {
            // Browse
            ((MainActivity) getActivity()).addLocalFileNavigationFragment(file);
        }
        else
        {
            // Show properties
            ActionManager.actionView(getActivity(), file, MimeTypeManager.getMIMEType(file.getName()));
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // ACTION BAR ITEM
    // //////////////////////////////////////////////////////////////////////

    /*public void refresh(){
        Bundle b = getArguments();
        if (b == null)
        {
            Account acc = ((MainActivity) getActivity()).getAccount();
            b = createBundleArgs(StorageManager.getDownloadFolder(getActivity(), acc.getUrl(), acc.getUsername()));
        }
        getLoaderManager().restartLoader(LocalFileExplorerLoader.ID, b, this);
        getLoaderManager().getLoader(LocalFileExplorerLoader.ID).forceLoad();
    }*/
    
    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    private FileActions nActions;

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        if (nActions != null) { return false; }

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new FileActions(LocalFileBrowserFragment.this, (File) l.getItemAtPosition(position));
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
        v.setSelected(true);
        v.findViewById(R.id.choose).setVisibility(View.VISIBLE);
        selectedItems.add((File) l.getItemAtPosition(position));
        return true;
    };

    @Override
    public void onStop()
    {
        if (nActions != null)
        {
            nActions.finish();
        }
        super.onStop();
    }
}
