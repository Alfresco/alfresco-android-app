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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.NodeChildrenLoader;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.ActionManager.ActionManagerListener;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * LocalFileBrowserFragment is responsible to display the content of Download Folder.
 * 
 * @author Jean Marie Pascal
 */
public class FileExplorerFragment extends AbstractFileExplorerFragment
{
    public static final String TAG = FileExplorerFragment.class.getName();

    private FileExplorerReceiver receiver;

    private File parent;

    private View vroot;
    
    private FileActions nActions;

    private File createFile;

    private long lastModifiedDate;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public FileExplorerFragment()
    {
        loaderId = NodeChildrenLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_download;
        initLoader = false;
        checkSession = false;
    }

    public static FileExplorerFragment newInstance()
    {
        return newInstance(null, null, MODE_LISTING);
    }

    public static FileExplorerFragment newInstance(File folder)
    {
        return newInstance(folder, null, MODE_LISTING);
    }

    public static FileExplorerFragment newInstance(String folderPath)
    {
        return newInstance(null, folderPath, MODE_LISTING);
    }

    public static FileExplorerFragment newInstance(File folder, int displayMode)
    {
        return newInstance(folder, null, displayMode);
    }

    public static FileExplorerFragment newInstance(File parentFolder, String pathFolder, int displayMode)
    {
        FileExplorerFragment bf = new FileExplorerFragment();
        ListingContext lc = new ListingContext();
        lc.setSortProperty(DocumentFolderService.SORT_PROPERTY_NAME);
        lc.setIsSortAscending(true);
        Bundle b = new Bundle(createBundleArgs(lc, LOAD_AUTO));
        b.putAll(createBundleArgs(parentFolder, pathFolder));
        b.putInt(PARAM_MODE, displayMode);
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle b = getArguments();
        if (b == null)
        {
            Account acc = ((BaseActivity) getActivity()).getCurrentAccount();
            if (acc != null)
            {
                File folder = StorageManager.getDownloadFolder(getActivity(), acc);
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
        else
        {
            parent = (File) b.getSerializable(ARGUMENT_FOLDER);
            if (parent == null && b.containsKey(ARGUMENT_FOLDERPATH) && b.getString(ARGUMENT_FOLDERPATH) != null)
            {
                parent = new File(b.getString(ARGUMENT_FOLDERPATH));
            }

            if (parent != null)
            {
                // TextView tv = ((TextView)vroot.findViewById(R.id.path));
                // tv.setText(parent.getPath().replaceAll("/", " > "));
                getLoaderManager().initLoader(FileExplorerLoader.ID, b, this);
                getLoaderManager().getLoader(FileExplorerLoader.ID).forceLoad();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        vroot = inflater.inflate(R.layout.app_filexplorer_list, container, false);
        if (vroot == null) return null;

        init(vroot, emptyListMessageId);

        setRetainInstance(true);
        if (initLoader)
        {
            continueLoading(loaderId, callback);
        }

        return vroot;
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
        
        if (getDialog() != null)
        {
            // getDialog().setTitle(titleId);
        }
        else
        {
            getActivity().getActionBar().show();
            if (!DisplayUtils.hasCentralPane(getActivity())){
                FileExplorerHelper.displayNavigationMode(getActivity(), getMode(), false);
                getActivity().getActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        getActivity().invalidateOptionsMenu();
        
        
        if (receiver == null)
        {
            IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_CREATE_FOLDER_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_DELETE_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
            receiver = new FileExplorerReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
        }

        refreshListView();
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

        getActivity().invalidateOptionsMenu();
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        if (receiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    private void displayNavigation(File file, boolean backstack)
    {
        BaseFragment frag = FileExplorerFragment.newInstance(file);
        FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                FileExplorerFragment.TAG, backstack);
    }
    
    // //////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        File file = (File) l.getItemAtPosition(position);

        if (getMode() == MODE_PICK_FILE)
        {
            if (nActions != null)
            {
                nActions.selectFile(file);
                adapter.notifyDataSetChanged();
            }
            else
            {
                if (file.isDirectory())
                {
                    displayNavigation(file, true);
                }
                else
                {
                    Intent pickResult = new Intent();
                    pickResult.setData(Uri.fromFile(file));
                    getActivity().setResult(Activity.RESULT_OK, pickResult);
                    getActivity().finish();
                }
            }
            return;
        }

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).getPath().equals(file.getPath());
        }
        l.setItemChecked(position, true);

        if (nActions != null)
        {
            nActions.selectFile(file);
            if (selectedItems.size() == 0)
            {
                hideDetails = true;
            }
        }
        else
        {
            selectedItems.clear();
        }

        if (hideDetails)
        {
            return;
        }
        else if (nActions == null)
        {
            if (file.isDirectory())
            {
                displayNavigation(file, true);
                //((MainActivity) getActivity()).addLocalFileNavigationFragment(file);
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
        adapter.notifyDataSetChanged();
    }
    
    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        if (nActions != null) { return false; }

        File item = (File) l.getItemAtPosition(position);

        selectedItems.clear();
        selectedItems.add(item);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new FileActions(FileExplorerFragment.this, selectedItems);
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

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu)
    {
        if (getMode() == MODE_LISTING)
        {
            SubMenu createMenu = menu.addSubMenu(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE, R.string.upload);
            createMenu.setIcon(android.R.drawable.ic_menu_add);
            createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            createMenu.add(Menu.NONE, MenuActionItem.MENU_CREATE_DOCUMENT, Menu.FIRST
                    + MenuActionItem.MENU_CREATE_DOCUMENT, R.string.create_document);

            createMenu.add(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO, R.string.take_photo);

            if (AndroidVersion.isICSOrAbove())
            {
                createMenu.add(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO, Menu.FIRST
                        + MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO, R.string.make_video);
            }

            createMenu.add(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO, R.string.record_audio);

            /*MenuItem mi = menu.add(Menu.NONE, MenuActionItem.MENU_CREATE_FOLDER, Menu.FIRST
                    + MenuActionItem.MENU_CREATE_FOLDER, R.string.folder_create);
            mi.setIcon(R.drawable.ic_add_folder);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);*/
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////
    public void setCreateFile(File newFile)
    {
        this.createFile = newFile;
        this.lastModifiedDate = newFile.lastModified();
    }

    public void selectAll()
    {
        if (nActions != null && adapter != null)
        {
            nActions.selectFiles(((FileExplorerAdapter) adapter).getFiles());
            adapter.notifyDataSetChanged();
        }
    }

    public File getParent()
    {
        return parent;
    }

    /**
     * Remove a site object inside the listing without requesting an HTTP call.
     * 
     * @param site : site to remove
     */
    public void remove(File file)
    {
        if (adapter != null)
        {
            ((FileExplorerAdapter) adapter).remove(file.getPath());
            if (adapter.isEmpty())
            {
                displayEmptyView();
            }
        }
    }
    
    public void createFolder()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(CreateDirectoryDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        CreateDirectoryDialogFragment.newInstance(getParent()).show(ft, CreateDirectoryDialogFragment.TAG);

    }

    // //////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // //////////////////////////////////////////////////////////////////////
    public class FileExplorerReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (adapter == null) { return; }

            if (intent.getExtras() != null)
            {
                File parentFolder = getParent();
                Bundle b = intent.getExtras().getParcelable(IntentIntegrator.EXTRA_DATA);
                if (b == null) { return; }
                if (b.getSerializable(IntentIntegrator.EXTRA_FOLDER) instanceof Folder) { return; }
                String pFolder = ((File) b.getSerializable(IntentIntegrator.EXTRA_FOLDER)).getPath();

                if (pFolder.equals(parentFolder.getPath()))
                {
                    if (intent.getAction().equals(IntentIntegrator.ACTION_DELETE_COMPLETED))
                    {
                        remove((File) b.getSerializable(IntentIntegrator.EXTRA_FILE));
                        return;
                    }
                    else if (intent.getAction().equals(IntentIntegrator.ACTION_CREATE_FOLDER_COMPLETED))
                    {
                        File file = (File) b.getSerializable(IntentIntegrator.EXTRA_CREATED_FOLDER);
                        ((FileExplorerAdapter) adapter).replaceFile(file);
                    }
                    else if (intent.getAction().equals(IntentIntegrator.ACTION_UPDATE_COMPLETED))
                    {
                        remove((File) b.getSerializable(IntentIntegrator.EXTRA_FILE));
                        File updatedFile = (File) b.getSerializable(IntentIntegrator.EXTRA_UPDATED_FILE);
                        ((FileExplorerAdapter) adapter).replaceFile(updatedFile);
                    }
                    refreshList();
                    lv.setSelection(selectedPosition);
                }
            }
        }

        private void refreshList()
        {
            if (((FileExplorerAdapter) adapter).getCount() >= 1)
            {
                lv.setVisibility(View.VISIBLE);
                ev.setVisibility(View.GONE);
                lv.setEmptyView(null);
                lv.setAdapter(adapter);
            }
        }
    }
}
