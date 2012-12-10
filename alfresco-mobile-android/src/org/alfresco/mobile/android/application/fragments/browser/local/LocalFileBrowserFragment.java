/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.browser.local;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.NodeChildrenLoader;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.browser.local.FileActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.ui.filebrowser.LocalFileExplorerFragment;
import org.alfresco.mobile.android.ui.filebrowser.LocalFileExplorerLoader;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;
import org.alfresco.mobile.android.ui.manager.StorageManager;
import org.alfresco.mobile.android.ui.manager.ActionManager.ActionManagerListener;

import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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
        return newInstance(folder, null, MODE_LISTING);
    }

    public static LocalFileBrowserFragment newInstance(File folder, int displayMode)
    {
        return newInstance(folder, null, displayMode);
    }

    public static LocalFileBrowserFragment newInstance(String folderPath)
    {
        return newInstance(null, folderPath, MODE_LISTING);
    }

    public static LocalFileBrowserFragment newInstance(File parentFolder, String pathFolder, int displayMode)
    {
        LocalFileBrowserFragment bf = new LocalFileBrowserFragment();
        ListingContext lc = new ListingContext();
        lc.setSortProperty(DocumentFolderService.SORT_PROPERTY_NAME);
        lc.setIsSortAscending(true);
        Bundle b = new Bundle(createBundleArgs(lc, LOAD_AUTO));
        b.putAll(createBundleArgs(parentFolder, pathFolder));
        b.putInt(MODE, displayMode);
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
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_alfresco);
        }
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        File file = (File) l.getItemAtPosition(position);

        if (getMode() == MODE_LISTING)
        {
            if (file.isDirectory())
            {
                // Browse
                ((MainActivity) getActivity()).addLocalFileNavigationFragment(file);
            }
            else
            {
                // Show properties
                ActionManager.actionView(getActivity(), file, MimeTypeManager.getMIMEType(file.getName()),
                        new ActionManagerListener()
                        {
                            @Override
                            public void onActivityNotFoundException(ActivityNotFoundException e)
                            {
                                Bundle b = new Bundle();
                                b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_unable_open_file_title);
                                b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE, R.string.error_unable_open_file);
                                b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
                                ActionManager.actionDisplayDialog(getActivity(), b);
                            }
                        });
            }
        }
        else if (getMode() == MODE_PICK_FILE)
        {
            if (file.isFile() && getFragmentManager().findFragmentByTag(ChildrenBrowserFragment.TAG) != null)
            {
                ((ChildrenBrowserFragment) getFragmentManager().findFragmentByTag(ChildrenBrowserFragment.TAG))
                        .createFile(file);
            }
        }

        if (getDialog() != null)
        {
            dismiss();
        }
    }

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
