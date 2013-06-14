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
package org.alfresco.mobile.android.application.fragments.browser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.NodeChildrenLoader;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.impl.RepositoryVersionHelper;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.exception.AlfrescoAppException;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.search.KeywordSearch;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.operations.batch.utils.NodePlaceHolder;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.documentfolder.NavigationFragment;
import org.alfresco.mobile.android.ui.documentfolder.actions.CreateFolderDialogFragment;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

/**
 * Display a dialogFragment to retrieve information about the content of a
 * specific folder.
 * 
 * @author Jean Marie Pascal
 */
@TargetApi(11)
public class ChildrenBrowserFragment extends NavigationFragment implements RefreshFragment, ListingModeFragment
{
    public static final String TAG = "ChildrenBrowserFragment";

    private boolean shortcutAlreadyVisible = false;

    private Folder importFolder;

    private File createFile;

    private long lastModifiedDate;

    /** By default, the fragment is in Listing mode. */
    private int mode = MODE_LISTING;

    private Button importButton;

    private TransfertReceiver receiver;

    private static final String PARAM_IS_SHORTCUT = "isShortcut";

    private NodeActions nActions;

    private File tmpFile;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public ChildrenBrowserFragment()
    {
    }

    public static ChildrenBrowserFragment newInstance(Folder folder)
    {
        return newInstance(folder, null, null);
    }

    public static ChildrenBrowserFragment newInstance(String folderPath)
    {
        return newInstance(null, folderPath, null);
    }

    public static ChildrenBrowserFragment newInstance(Site site)
    {
        return newInstance(null, null, site);
    }

    public static ChildrenBrowserFragment newInstance(Site site, Folder folder)
    {
        return newInstance(folder, null, site);
    }

    private static ChildrenBrowserFragment newInstance(Folder parentFolder, String pathFolder, Site site)
    {
        ChildrenBrowserFragment bf = new ChildrenBrowserFragment();
        ListingContext lc = new ListingContext();
        lc.setSortProperty(DocumentFolderService.SORT_PROPERTY_NAME);
        lc.setIsSortAscending(true);
        Bundle b = createBundleArgs(parentFolder, pathFolder, site);
        b.putBoolean(PARAM_IS_SHORTCUT, pathFolder != null);
        b.putAll(createBundleArgs(lc, LOAD_AUTO));
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        alfSession = SessionUtils.getSession(getActivity());
        if (alfSession == null)
        {

        }
        else if (RepositoryVersionHelper.isAlfrescoProduct(alfSession))
        {
            setActivateThumbnail(true);
        }

        // In case of Import mode, we disable thumbnails.
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            mode = MODE_IMPORT;
            setActivateThumbnail(false);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = null;
        // In case of Import mode, we wrap the listing with buttons.
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            v = inflater.inflate(R.layout.app_browser_import, container, false);
            init(v, emptyListMessageId);

            importButton = (Button) v.findViewById(R.id.action_import);
        }
        else
        {
            v = super.onCreateView(inflater, container, savedInstanceState);

            ListView listView = (ListView) v.findViewById(R.id.listView);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setClickable(true);

            listView.setDivider(null);
            listView.setDividerHeight(0);

            listView.setBackgroundColor(getResources().getColor(R.color.grey_lighter));
        }
        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        int titleId = R.string.app_name;
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            mode = MODE_IMPORT;
            titleId = R.string.import_document_title;
            checkImportButton();
        }

        // If the fragment is resumed after user content creation action, we
        // have to check if the file has been modified or not. Depending on
        // result we prompt the upload dialog or we do nothing (no modification
        // / blank file)
        if (createFile != null)
        {
            if (createFile.length() > 0 && lastModifiedDate < createFile.lastModified())
            {
                tmpFile = createFile;
            }
            else
            {
                if (!createFile.delete())
                {
                    Log.w(TAG, createFile.getName() + "is not deleted.");
                }
                createFile = null;
            }
        }

        if (tmpFile != null)
        {
            importFolder = ((MainActivity) getActivity()).getImportParent();
            createFile(tmpFile);
        }

        if (getActivity().getActionBar() != null)
        {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
            getActivity().setTitle(titleId);
            if (shortcutAlreadyVisible)
            {
                displayPathShortcut();
            }
        }

        if (receiver == null)
        {
            IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_UPLOAD_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_DELETE_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_UPLOAD_STARTED);
            intentFilter.addAction(IntentIntegrator.ACTION_CREATE_FOLDER_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_FAVORITE_COMPLETED);
            intentFilter.addAction(IntentIntegrator.ACTION_DOWNLOAD_COMPLETED);
            receiver = new TransfertReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
        }

        refreshListView();
    }

    @Override
    public void onPause()
    {
        getActivity().invalidateOptionsMenu();

        int titleId = R.string.app_name;
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            titleId = R.string.import_document_title;
        }
        getActivity().setTitle(titleId);
        if (getActivity().getActionBar() != null)
        {
            getActivity().getActionBar().setDisplayShowTitleEnabled(true);
            getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }

        super.onPause();
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
    public void onDestroy()
    {
        if (receiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    // //////////////////////////////////////////////////////////////////////
    // PATH
    // //////////////////////////////////////////////////////////////////////
    private void displayPathShortcut()
    {
        // /QUICK PATH
        if (parentFolder != null && getActivity().getActionBar() != null)
        {
            //
            getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            String pathValue = parentFolder.getProperty(PropertyIds.PATH).getValue();

            boolean fromSite = false;
            if (getActivity() instanceof MainActivity)
            {
                fromSite = currentSiteParameter != null;
            }

            List<String> listFolder = getPath(pathValue, fromSite);

            SpinnerAdapter adapter = new PathAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                    listFolder);

            OnNavigationListener mOnNavigationListener = new OnNavigationListener()
            {

                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId)
                {
                    if (itemPosition == 0) { return true; }

                    if (isShortcut())
                    {
                        boolean fromSite = false;
                        if (getActivity() instanceof MainActivity)
                        {
                            fromSite = currentSiteParameter != null;
                        }

                        // Determine the path
                        String pathValue = parentFolder.getProperty(PropertyIds.PATH).getValue();
                        List<String> listFolder = getPath(pathValue, fromSite);

                        List<String> subPath = listFolder.subList(itemPosition, listFolder.size());
                        Collections.reverse(subPath);
                        String path = subPath.remove(0);
                        for (String string : subPath)
                        {
                            path += string + "/";
                        }

                        ((BaseActivity) getActivity()).addBrowserFragment(path);
                    }
                    else
                    {
                        for (int i = 0; i < itemPosition; i++)
                        {
                            getFragmentManager().popBackStack();
                        }
                    }

                    return true;
                }

            };

            getActivity().getActionBar().setListNavigationCallbacks(adapter, mOnNavigationListener);

            shortcutAlreadyVisible = true;
        }
    }

    private List<String> getPath(String pathValue, boolean fromSite)
    {
        String[] path = pathValue.split("/");
        if (path.length == 0)
        {
            path = new String[] { "/" };
        }

        String tmpPath = "";

        List<String> listFolder = new ArrayList<String>(path.length);
        for (int i = path.length - 1; i > -1; i--)
        {
            tmpPath = path[i];

            if (tmpPath.isEmpty())
            {
                tmpPath = "/";
            }
            listFolder.add(tmpPath);
        }

        if (fromSite && listFolder.size() > 3)
        {
            for (int i = 0; i < 3; i++)
            {
                listFolder.remove(listFolder.size() - 1);
            }
            listFolder.add(listFolder.size() - 1, currentSiteParameter.getTitle());
            listFolder.remove(listFolder.size() - 1);
        }

        return listFolder;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Node item = (Node) l.getItemAtPosition(position);

        if (item instanceof NodePlaceHolder) { return; }

        // In case of import mode, we disable selection of document.
        // It's only possible to select a folder for navigation purpose.
        if (mode == MODE_IMPORT && getActivity() instanceof PublicDispatcherActivity)
        {
            l.setChoiceMode(ListView.CHOICE_MODE_NONE);
            if (item.isFolder())
            {
                ((PublicDispatcherActivity) getActivity()).addNavigationFragment((Folder) item);
            }
            return;
        }

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).getIdentifier().equals(item.getIdentifier());
        }
        l.setItemChecked(position, true);

        if (nActions != null)
        {
            nActions.selectNode(item);
            if (selectedItems.size() == 0)
            {
                hideDetails = true;
            }
        }
        else
        {
            selectedItems.clear();
            if (!hideDetails && item.isDocument() && DisplayUtils.hasCentralPane(getActivity()))
            {
                selectedItems.add(item);
            }
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
                FragmentDisplayer.removeFragment(getActivity(), android.R.id.tabcontent);
            }
        }
        else if (nActions == null)
        {
            if (item.isFolder())
            {
                addNavigationFragment(currentSiteParameter, (Folder) item);
            }
            else
            {
                // Show properties
                ((MainActivity) getActivity()).addPropertiesFragment(item);
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
        }
    }

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        // We disable long click during import mode.
        if (mode == MODE_IMPORT) { return false; }

        Node n = (Node) l.getItemAtPosition(position);
        boolean b = true;
        if (n instanceof NodePlaceHolder)
        {
            getActivity().startActivity(new Intent(IntentIntegrator.ACTION_DISPLAY_OPERATIONS));
            b = false;
        }
        else
        {
            l.setItemChecked(position, true);
            b = startSelection(n);
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
                FragmentDisplayer.removeFragment(getActivity(), android.R.id.tabcontent);
            }
        }
        return b;
    };

    private boolean startSelection(Node item)
    {
        if (nActions != null) { return false; }

        selectedItems.clear();
        selectedItems.add(item);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new NodeActions(ChildrenBrowserFragment.this, selectedItems);
        nActions.setOnFinishModeListerner(new onFinishModeListerner()
        {
            @Override
            public void onFinish()
            {
                nActions = null;
                unselect();
                refreshListView();
            }
        });
        getActivity().startActionMode(nActions);
        return true;
    }

    // //////////////////////////////////////////////////////////////////////
    // LOADERS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Node>>> loader, LoaderResult<PagingResult<Node>> results)
    {

        if (getActivity() instanceof MainActivity && ((MainActivity) getActivity()).getCurrentNode() != null)
        {
            selectedItems.clear();
            selectedItems.add(((MainActivity) getActivity()).getCurrentNode());
        }

        if (loader instanceof NodeChildrenLoader)
        {
            parentFolder = ((NodeChildrenLoader) loader).getParentFolder();
            importFolder = parentFolder;
        }

        if (adapter == null)
        {
            adapter = new AlphabeticNodeAdapter(getActivity(), R.layout.app_list_progress_row, parentFolder,
                    new ArrayList<Node>(0), selectedItems, mode);
        }

        if (results.hasException())
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
        ((NodeAdapter) adapter).setActivateThumbnail(hasActivateThumbnail());
        getActivity().invalidateOptionsMenu();
        displayPathShortcut();
        checkImportButton();
    }

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }

    // //////////////////////////////////////////////////////////////////////
    // ACTIONS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case PublicIntent.REQUESTCODE_FILEPICKER:
                if (data != null && IntentIntegrator.ACTION_PICK_FILE.equals(data.getAction()))
                {
                    ActionManager.actionPickFile(getFragmentManager().findFragmentByTag(TAG),
                            IntentIntegrator.REQUESTCODE_FILEPICKER);
                }
                else if (data != null && data.getData() != null)
                {
                    String tmpPath = ActionManager.getPath(getActivity(), data.getData());
                    if (tmpPath != null)
                    {
                        tmpFile = new File(tmpPath);
                    }
                    else
                    {
                        // Error case : Unable to find the file path associated
                        // to user pick.
                        // Sample : Picasa image case
                        ActionManager.actionDisplayError(ChildrenBrowserFragment.this, new AlfrescoAppException(
                                getString(R.string.error_unknown_filepath), true));
                    }
                }
                else if (data != null && data.getExtras().containsKey(Intent.EXTRA_STREAM))
                {
                    List<File> files = new ArrayList<File>();
                    List<Uri> uris = data.getExtras().getParcelableArrayList(Intent.EXTRA_STREAM);
                    for (Uri uri : uris)
                    {
                        files.add(new File(ActionManager.getPath(getActivity(), uri)));
                    }
                    createFiles(files);
                }
                break;
            default:
                break;
        }
    }

    public void createFile(File f)
    {
        // Create and show the dialog.
        AddContentDialogFragment newFragment = AddContentDialogFragment.newInstance(importFolder, f,
                (createFile != null));
        newFragment.show(getActivity().getFragmentManager(), AddContentDialogFragment.TAG);
        tmpFile = null;
        createFile = null;
    }

    public void createFiles(List<File> files)
    {
        if (files.size() == 1)
        {
            createFile(files.get(0));
            return;
        }
        else
        {
            OperationsRequestGroup group = new OperationsRequestGroup(getActivity(),
                    SessionUtils.getAccount(getActivity()));
            for (File file : files)
            {
                group.enqueue(new CreateDocumentRequest(importFolder.getIdentifier(), file.getName(),
                        new ContentFileProgressImpl(file)));
            }
            BatchOperationManager.getInstance(getActivity()).enqueue(group);

            if (getActivity() instanceof PublicDispatcherActivity)
            {
                getActivity().finish();
            }
        }
        tmpFile = null;
        createFile = null;
    }

    public void createFolder()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(CreateFolderDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        AddFolderDialogFragment.newInstance(parentFolder).show(ft, CreateFolderDialogFragment.TAG);
    }

    public void refresh()
    {
        if (parentFolder == null)
        {
            parentFolder = SessionUtils.getSession(getActivity()).getRootFolder();
        }
        super.refresh();
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu)
    {
        if (parentFolder == null) { return; }

        if (getActivity() instanceof MainActivity)
        {
            getMenu(alfSession, menu, parentFolder);
        }
        else if (getActivity() instanceof PublicDispatcherActivity)
        {
            Permissions permission = alfSession.getServiceRegistry().getDocumentFolderService()
                    .getPermissions(parentFolder);

            if (permission.canAddChildren())
            {
                MenuItem mi = menu.add(Menu.NONE, MenuActionItem.MENU_CREATE_FOLDER, Menu.FIRST
                        + MenuActionItem.MENU_CREATE_FOLDER, R.string.folder_create);
                mi.setIcon(R.drawable.ic_add_folder);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
    }

    public static void getMenu(AlfrescoSession session, Menu menu, Folder parentFolder, boolean actionMode)
    {
        MenuItem mi;

        if (parentFolder == null) { return; }
        Permissions permission = null;
        try
        {
            permission = session.getServiceRegistry().getDocumentFolderService().getPermissions(parentFolder);
        }
        catch (Exception e)
        {
            return;
        }

        if (!actionMode)
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_SEARCH_FOLDER, Menu.FIRST + MenuActionItem.MENU_SEARCH_FOLDER,
                    R.string.search);
            mi.setIcon(R.drawable.ic_search);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (!actionMode && permission.canAddChildren())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_CREATE_FOLDER, Menu.FIRST + MenuActionItem.MENU_CREATE_FOLDER,
                    R.string.folder_create);
            mi.setIcon(R.drawable.ic_add_folder);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            SubMenu createMenu = menu.addSubMenu(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE, R.string.upload);
            createMenu.setIcon(android.R.drawable.ic_menu_add);
            createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            createMenu.add(Menu.NONE, MenuActionItem.MENU_UPLOAD, Menu.FIRST + MenuActionItem.MENU_UPLOAD,
                    R.string.upload_title);

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

        }

        if (actionMode)
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DETAILS, Menu.FIRST + MenuActionItem.MENU_DETAILS,
                    R.string.action_view_properties);
            mi.setIcon(R.drawable.ic_details);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (actionMode && permission.canEdit())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT, R.string.edit);
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (actionMode && permission.canDelete())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE, Menu.FIRST + MenuActionItem.MENU_DELETE,
                    R.string.delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (!actionMode)
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, Menu.FIRST + MenuActionItem.MENU_REFRESH,
                    R.string.refresh);
            mi.setIcon(R.drawable.ic_refresh);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    public static void getMenu(AlfrescoSession session, Menu menu, Folder parentFolder)
    {
        getMenu(session, menu, parentFolder, false);
    }

    // //////////////////////////////////////////////////////////////////////
    // LIST MANAGEMENT UTILS
    // //////////////////////////////////////////////////////////////////////
    public void unselect()
    {
        selectedItems.clear();
    }

    /**
     * Remove a site object inside the listing without requesting an HTTP call.
     * 
     * @param site : site to remove
     */
    public void remove(Node node)
    {
        if (adapter != null)
        {
            ((AlphabeticNodeAdapter) adapter).remove(node.getName());
            if (adapter.isEmpty())
            {
                displayEmptyView();
            }
        }
    }

    public void selectAll()
    {
        if (nActions != null && adapter != null)
        {
            nActions.selectNodes(((AlphabeticNodeAdapter) adapter).getNodes());
            adapter.notifyDataSetChanged();
        }
    }

    public void select(Node updatedNode)
    {
        selectedItems.add(updatedNode);
    }

    // //////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // //////////////////////////////////////////////////////////////////////
    public class TransfertReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());

            if (adapter == null) return;

            if (intent.getExtras() != null)
            {
                Folder parentFolder = getParent();
                Bundle b = intent.getExtras().getParcelable(IntentIntegrator.EXTRA_DATA);
                if (b == null) { return; }
                if (b.getSerializable(IntentIntegrator.EXTRA_FOLDER) instanceof File) { return; }
                String pFolder = ((Folder) b.getParcelable(IntentIntegrator.EXTRA_FOLDER)).getIdentifier();

                if (pFolder.equals(parentFolder.getIdentifier()))
                {
                    if (intent.getAction().equals(IntentIntegrator.ACTION_DELETE_COMPLETED))
                    {
                        remove((Node) b.getParcelable(IntentIntegrator.EXTRA_DOCUMENT));
                        return;
                    }
                    else if (intent.getAction().equals(IntentIntegrator.ACTION_UPLOAD_STARTED))
                    {
                        String documentName = b.getString(IntentIntegrator.EXTRA_DOCUMENT_NAME);
                        Node node = new NodePlaceHolder(documentName, CreateDocumentRequest.TYPE_ID,
                                Operation.STATUS_RUNNING);
                        ((AlphabeticNodeAdapter) adapter).replaceNode(node);
                    }
                    else if (intent.getAction().equals(IntentIntegrator.ACTION_UPLOAD_COMPLETED))
                    {
                        Node node = (Node) b.getParcelable(IntentIntegrator.EXTRA_DOCUMENT);
                        ((AlphabeticNodeAdapter) adapter).replaceNode(node);
                    }
                    else if (intent.getAction().equals(IntentIntegrator.ACTION_UPDATE_COMPLETED))
                    {
                        if (b.containsKey(IntentIntegrator.EXTRA_DOCUMENT))
                        {
                            remove((Node) b.getParcelable(IntentIntegrator.EXTRA_DOCUMENT));
                        }
                        else
                        {
                            remove((Node) b.getParcelable(IntentIntegrator.EXTRA_NODE));
                        }

                        Node updatedNode = (Node) b.getParcelable(IntentIntegrator.EXTRA_UPDATED_NODE);
                        ((AlphabeticNodeAdapter) adapter).replaceNode(updatedNode);
                    }
                    else if (intent.getAction().equals(IntentIntegrator.ACTION_CREATE_FOLDER_COMPLETED))
                    {
                        Node node = (Node) b.getParcelable(IntentIntegrator.EXTRA_CREATED_FOLDER);
                        ((AlphabeticNodeAdapter) adapter).replaceNode(node);
                    }
                    else if (intent.getAction().equals(IntentIntegrator.ACTION_FAVORITE_COMPLETED))
                    {
                        ((AlphabeticNodeAdapter) adapter).refreshFavorites();
                    }
                    else if (intent.getAction().equals(IntentIntegrator.ACTION_DOWNLOAD_COMPLETED))
                    {
                        Node node = (Node) b.getParcelable(IntentIntegrator.EXTRA_DOCUMENT);
                        ((AlphabeticNodeAdapter) adapter).replaceNode(node);
                    }
                    refreshList();
                    lv.setSelection(selectedPosition);
                }
            }
        }

        private void refreshList()
        {
            if (((AlphabeticNodeAdapter) adapter).getCount() >= 2)
            {
                lv.setVisibility(View.VISIBLE);
                ev.setVisibility(View.GONE);
                lv.setEmptyView(null);
                lv.setAdapter(adapter);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////
    public void search(int fragmentPlaceId)
    {
        FragmentDisplayer.replaceFragment(getActivity(),
                KeywordSearch.newInstance(folderParameter, currentSiteParameter), fragmentPlaceId, KeywordSearch.TAG,
                true);
    }

    public void setCreateFile(File newFile)
    {
        this.createFile = newFile;
        this.lastModifiedDate = newFile.lastModified();
    }

    public Folder getImportFolder()
    {
        return importFolder;
    }

    @Override
    public int getMode()
    {
        return mode;
    }

    /**
     * Helper method to enable/disable the import button depending on mode and
     * permission.
     */
    private void checkImportButton()
    {
        if (mode == MODE_IMPORT)
        {
            boolean enable = false;
            if (parentFolder != null)
            {
                Permissions permission = alfSession.getServiceRegistry().getDocumentFolderService()
                        .getPermissions(parentFolder);
                enable = permission.canAddChildren();
            }
            importButton.setEnabled(enable);
        }
    }

    public boolean isShortcut()
    {
        return (Boolean) getArguments().get(PARAM_IS_SHORTCUT);
    }

    private void addNavigationFragment(Site currentSite, Folder item)
    {
        ((BaseActivity) getActivity()).addNavigationFragment(currentSite, item);
    }
}
