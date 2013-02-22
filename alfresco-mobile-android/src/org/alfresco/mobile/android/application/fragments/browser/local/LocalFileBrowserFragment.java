/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.browser.local.FileActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.filebrowser.LocalFileExplorerFragment;
import org.alfresco.mobile.android.ui.filebrowser.LocalFileExplorerLoader;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.ActionManager.ActionManagerListener;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * LocalFileBrowserFragment is responsible to display the content of Download
 * Folder.
 * 
 * @author Jean Marie Pascal
 */
public class LocalFileBrowserFragment extends LocalFileExplorerFragment
{
    public static final String TAG = "LocalFileBrowserFragment";

    public static final String FRAGMENT_TAG = "fragmentTag";

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
        return newInstance(folder, null, MODE_LISTING, null);
    }

    public static LocalFileBrowserFragment newInstance(File folder, int displayMode)
    {
        return newInstance(folder, null, displayMode, null);
    }

    public static BaseFragment newInstance(File folder, int displayMode, String fragmentTag)
    {
        return newInstance(folder, null, displayMode, fragmentTag);
    }

    public static LocalFileBrowserFragment newInstance(String folderPath)
    {
        return newInstance(null, folderPath, MODE_LISTING, null);
    }

    public static LocalFileBrowserFragment newInstance(File parentFolder, String pathFolder, int displayMode,
            String fragmentTag)
    {
        LocalFileBrowserFragment bf = new LocalFileBrowserFragment();
        ListingContext lc = new ListingContext();
        lc.setSortProperty(DocumentFolderService.SORT_PROPERTY_NAME);
        lc.setIsSortAscending(true);
        Bundle b = new Bundle(createBundleArgs(lc, LOAD_AUTO));
        b.putAll(createBundleArgs(parentFolder, pathFolder));
        b.putInt(MODE, displayMode);
        b.putString(FRAGMENT_TAG, fragmentTag);
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
            if (acc != null)
            {
                File folder = StorageManager.getDownloadFolder(getActivity(), acc.getUrl(), acc.getUsername());
                if (folder != null)
                {
                    b = createBundleArgs(folder);
                }
                else
                {
                    MessengerManager.showLongToast(getActivity(), getString(R.string.sdinaccessible));
                    return;
                }
            }
            else
            {
                MessengerManager.showLongToast(getActivity(), getString(R.string.loginfirst));
                return;
            }
        }
        getLoaderManager().initLoader(LocalFileExplorerLoader.ID, b, this);
        getLoaderManager().getLoader(LocalFileExplorerLoader.ID).forceLoad();
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
                ActionManager.actionView(this, file, new ActionManagerListener()
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
            if (file.isFile() && getArguments().getString(FRAGMENT_TAG) != null)
            {
                String fragmentTag = getArguments().getString(FRAGMENT_TAG);

                if (ChildrenBrowserFragment.TAG.equals(fragmentTag))
                {
                    ((ChildrenBrowserFragment) getFragmentManager().findFragmentByTag(ChildrenBrowserFragment.TAG))
                            .createFile(file);
                }
                else if (DetailsFragment.TAG.equals(fragmentTag))
                {
                    ((DetailsFragment) getFragmentManager().findFragmentByTag(DetailsFragment.TAG)).update(file);
                }
            }
        }

        if (getDialog() != null)
        {
            dismiss();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // ACTION MODE
    // //////////////////////////////////////////////////////////////////////
    private FileActions nActions;

    private File createFile;

    private long lastModifiedDate;

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

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu)
    {
        if (getMode() == MODE_LISTING)
        {
            MenuItem mi = menu.add(Menu.NONE, MenuActionItem.MENU_CREATE_DOCUMENT, Menu.FIRST
                    + MenuActionItem.MENU_CREATE_DOCUMENT, R.string.create_document);
            mi.setIcon(android.R.drawable.ic_menu_add);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // If the fragment is resumed after user content creation action, we
        // have to check if the file has been modified or not. Depending on
        // result we prompt the upload dialog or we do nothing (no modification
        // / blank file)
        if (createFile != null)
        {
            if (createFile.length() > 0 && lastModifiedDate < createFile.lastModified())
            {
                refresh();
            }
            else
            {
                createFile.delete();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case PublicIntent.REQUESTCODE_CREATE:
                if (createFile != null)
                {
                    if (createFile.length() > 0 && lastModifiedDate < createFile.lastModified())
                    {
                        refresh();
                    }
                    else
                    {
                        createFile.delete();
                    }
                }
                break;
            default:
                break;
        }
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

    public void setCreateFile(File newFile)
    {
        this.createFile = newFile;
        this.lastModifiedDate = newFile.lastModified();
    }

}
