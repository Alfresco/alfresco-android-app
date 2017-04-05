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
package org.alfresco.mobile.android.application.fragments.node.browser;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Link;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.impl.RepositoryVersionHelper;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.capture.DeviceCapture;
import org.alfresco.mobile.android.application.capture.DeviceCaptureHelper;
import org.alfresco.mobile.android.application.configuration.ConfigurableActionHelper;
import org.alfresco.mobile.android.application.configuration.model.view.RepositoryConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.GridAdapterHelper;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment;
import org.alfresco.mobile.android.application.fragments.node.create.AddContentDialogFragment;
import org.alfresco.mobile.android.application.fragments.node.create.AddFolderDialogFragment;
import org.alfresco.mobile.android.application.fragments.node.create.CreateFolderDialogFragment;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsActionMode;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.node.rendition.CarouselPreviewFragment;
import org.alfresco.mobile.android.application.fragments.search.SearchFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.ui.form.picker.DocumentPickerFragment.onPickDocumentFragment;
import org.alfresco.mobile.android.application.widgets.ActionShortcutActivity;
import org.alfresco.mobile.android.application.widgets.BaseShortcutActivity;
import org.alfresco.mobile.android.application.widgets.FolderShortcutActivity;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.browse.NodeChildrenEvent;
import org.alfresco.mobile.android.async.node.browse.NodeChildrenRequest;
import org.alfresco.mobile.android.async.node.create.CreateDocumentEvent;
import org.alfresco.mobile.android.async.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.async.node.create.CreateFolderEvent;
import org.alfresco.mobile.android.async.node.delete.DeleteNodeEvent;
import org.alfresco.mobile.android.async.node.download.DownloadEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.async.node.sync.SyncNodeEvent;
import org.alfresco.mobile.android.async.node.sync.SyncNodeRequest;
import org.alfresco.mobile.android.async.node.update.UpdateContentEvent;
import org.alfresco.mobile.android.async.node.update.UpdateNodeEvent;
import org.alfresco.mobile.android.async.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.async.utils.NodePlaceHolder;
import org.alfresco.mobile.android.platform.exception.AlfrescoAppException;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.extensions.ScanSnapManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentScanEvent;
import org.alfresco.mobile.android.ui.SelectableFragment;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserFragment;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import com.cocosw.bottomsheet.BottomSheet;
import com.squareup.otto.Subscribe;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * Display a dialogFragment to retrieve information about the content of a
 * specific folder.
 * 
 * @author Jean Marie Pascal
 */
public class DocumentFolderBrowserFragment extends NodeBrowserFragment implements SelectableFragment
{
    public static final String TAG = DocumentFolderBrowserFragment.class.getName();

    private boolean shortcutAlreadyVisible = false;

    private Folder importFolder;

    private File createFile;

    private long lastModifiedDate;

    private Button validationButton;

    private static final String ARGUMENT_IS_SHORTCUT = "isShortcut";

    private AbstractActions<Node> nActions;

    private File tmpFile;

    private onPickDocumentFragment fragmentPick;

    private Map<String, Node> pickedNodes = new HashMap<String, Node>(0);

    private List<Node> nodesToFavorite;

    private int displayMode = GridAdapterHelper.DISPLAY_GRID;

    private MenuItem displayMenuItem;

    private String fieldId;

    private boolean doFavorite;

    private Permissions permission;

    private boolean hasAudioRecorder = false;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public DocumentFolderBrowserFragment()
    {
        displayAsList = false;
        /** By default, the fragment is in Listing mode. */
        mode = MODE_LISTING;
        setHasOptionsMenu(true);
    }

    public static DocumentFolderBrowserFragment newInstanceByTemplate(Bundle b)
    {
        DocumentFolderBrowserFragment cbf = new DocumentFolderBrowserFragment();
        cbf.setArguments(b);
        b.putBoolean(ARGUMENT_BASED_ON_TEMPLATE, true);

        if (b != null)
        {
            if (b.containsKey(ARGUMENT_FOLDER_TYPE_ID))
            {
                int index = b.getInt(ARGUMENT_FOLDER_TYPE_ID);
                if (index == NodeChildrenRequest.FOLDER_SHARED)
                {
                    cbf.screenName = AnalyticsManager.SCREEN_REPOSITORY_SHARED;
                }
                else if (index == NodeChildrenRequest.FOLDER_USER_HOMES)
                {
                    cbf.screenName = AnalyticsManager.SCREEN_REPOSITORY_USERHOME;
                }
            }
            else if (!b.containsKey(ARGUMENT_FOLDER) && !b.containsKey(ARGUMENT_SITE) && !b.containsKey(ARGUMENT_PATH)
                    && !b.containsKey(ARGUMENT_FOLDER_NODEREF) && !b.containsKey(ARGUMENT_SITE_SHORTNAME))
            {
                cbf.screenName = AnalyticsManager.SCREEN_REPOSITORY;
            }
            else
            {
                cbf.screenName = AnalyticsManager.SCREEN_NODE_LISTING;
            }
        }

        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        // HomeScreen Shortcut ?
        if (getArguments() != null && getArguments().containsKey(ARGUMENT_IS_SHORTCUT))
        {
            checkSession = false;
        }

        // In case of Import mode, we disable thumbnails.
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            mode = MODE_IMPORT;
            setActivateThumbnail(false);
        }
        else if (getActivity() instanceof PrivateDialogActivity)
        {
            mode = MODE_PICK;
            fragmentPick = ((PrivateDialogActivity) getActivity()).getOnPickDocumentFragment();
            fieldId = ((PrivateDialogActivity) getActivity()).getFieldId();
        }
        else if (getActivity() instanceof BaseShortcutActivity)
        {
            mode = MODE_IMPORT;
            setActivateThumbnail(false);
        }

        super.onActivityCreated(savedInstanceState);

        if (getSession() != null && RepositoryVersionHelper.isAlfrescoProduct(getSession()))
        {
            setActivateThumbnail(true);
        }

        // Test Audio Recording
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        hasAudioRecorder = intent.resolveActivity(getActivity().getPackageManager()) != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v;
        // In case of Import mode, we wrap the listing with buttons.
        if (getActivity() instanceof PublicDispatcherActivity || getActivity() instanceof PrivateDialogActivity
                || getActivity() instanceof BaseShortcutActivity)
        {
            setRootView(inflater.inflate(R.layout.app_browser_import, container, false));
            init(getRootView(), emptyListMessageId);

            validationButton = (Button) viewById(R.id.action_validation);
            GridView gridView = (GridView) viewById(R.id.gridview);
            if (getActivity() instanceof PrivateDialogActivity)
            {
                validationButton.setText(R.string.done);
                gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
            }
            else if (getActivity() instanceof BaseShortcutActivity)
            {
                validationButton.setText(R.string.select_folder);
                gridView.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
            }
            else
            {
                gridView.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
            }
            gridView.setClickable(true);

            v = getRootView();
        }
        else
        {
            v = super.onCreateView(inflater, container, savedInstanceState);

            GridView gridView = (GridView) v.findViewById(R.id.gridview);
            gridView.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
            gridView.setClickable(true);

            gridView.setBackgroundColor(getResources().getColor(R.color.grey_lighter));
        }

        return v;
    }

    protected void prepareEmptyInitialView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        prepareEmptyView(ev, emptyImageView, firstEmptyMessage, secondEmptyMessage);
    }

    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {

        int iconId = R.drawable.ic_empty_folder_ro;
        int titleId = R.string.nodebrowser_empty_ro_title;
        int descriptionId = -1;
        try
        {
            if (parentFolder != null)
            {
                if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), parentFolder,
                        ConfigurableActionHelper.ACTION_CREATE_FOLDER))
                {
                    iconId = R.drawable.ic_empty_folder_rw;
                    titleId = R.string.nodebrowser_empty_rw_title;
                    descriptionId = R.string.nodebrowser_empty_rw_description;
                }
            }
        }
        catch (Exception e)
        {
            // DO Nothing
        }

        emptyImageView.setImageResource(iconId);
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        firstEmptyMessage.setText(titleId);
        if (descriptionId != -1)
        {
            secondEmptyMessage.setVisibility(View.VISIBLE);
            secondEmptyMessage.setText(descriptionId);
        }
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
            checkValidationButton();
        }
        else if (getActivity() instanceof PrivateDialogActivity)
        {
            mode = MODE_PICK;
            titleId = R.string.picker_document_title;
            checkValidationButton();
        }
        else if (getActivity() instanceof ActionShortcutActivity)
        {
            mode = MODE_IMPORT;
            titleId = R.string.shortcut_action_create;
            checkValidationButton();
        }
        else if (getActivity() instanceof FolderShortcutActivity)
        {
            mode = MODE_IMPORT;
            titleId = R.string.shortcut_create;
            checkValidationButton();
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

        if (getActionBar() != null)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowCustomEnabled(false);
            getActivity().setTitle(titleId);
            AccessibilityUtils.sendAccessibilityEvent(getActivity());
            if (shortcutAlreadyVisible)
            {
                displayPathShortcut();
            }
        }

        refreshListView();

        // For tablet : Display Item if node has been selected previously and
        // after a resume
        if (selectedItems != null && selectedItems.size() == 1 && DisplayUtils.hasCentralPane(getActivity())
                && getFragmentManager().findFragmentById(DisplayUtils.getCentralFragmentId(getActivity())) == null)
        {
            NodeDetailsFragment.with(getActivity()).parentFolder(parentFolder).node(selectedItems.get(0)).display();
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

    public void refreshListView()
    {
        super.refreshListView();
        if (adapter != null && adapter instanceof ProgressNodeAdapter)
        {
            ((ProgressNodeAdapter) adapter).refreshOperations();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // PATH
    // //////////////////////////////////////////////////////////////////////
    private void displayPathShortcut()
    {
        displayPathShortcut(false);
    }

    private void displayPathShortcut(boolean force)
    {
        // /QUICK PATH
        if (parentFolder != null && getActionBar() != null)
        {
            // If tablet & right panel displayed show nothing
            if (DisplayUtils.hasCentralPane(getActivity()) && !force)
            {
                Fragment sideFragment = getFragmentManager()
                        .findFragmentById(DisplayUtils.getCentralFragmentId(getActivity()));
                if (sideFragment != null) { return; }
            }

            ((ViewGroup) getActionBar().getCustomView()).setVisibility(View.GONE);
            getActionBar().setDisplayUseLogoEnabled(false);
            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            String pathValue = parentFolder.getName();
            if (parentFolder.getProperty(PropertyIds.PATH) != null)
            {
                pathValue = parentFolder.getProperty(PropertyIds.PATH).getValue();
            }

            boolean fromSite = false;
            if (getActivity() instanceof MainActivity)
            {
                fromSite = site != null;
            }

            List<String> listFolder = getPath(pathValue, fromSite);
            SpinnerAdapter adapter = new FolderPathAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                    listFolder);

            ActionBar.OnNavigationListener mOnNavigationListener = new android.support.v7.app.ActionBar.OnNavigationListener()
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
                            fromSite = site != null;
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

                        DocumentFolderBrowserFragment.with(getActivity()).path(path).shortcut(true).display();
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

            getActionBar().setListNavigationCallbacks(adapter, mOnNavigationListener);

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

        String tmpPath;

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
            listFolder.add(listFolder.size() - 1, site.getTitle());
            listFolder.remove(listFolder.size() - 1);
        }

        return listFolder;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        Node item = (Node) l.getItemAtPosition(position);

        if (item instanceof NodePlaceHolder)
        {
            l.setItemChecked(position, false);
            return;
        }

        // In case of import mode, we disable selection of document.
        // It's only possible to select a folder for navigation purpose.
        if (mode == MODE_IMPORT && getActivity() instanceof PublicDispatcherActivity
                || getActivity() instanceof BaseShortcutActivity)
        {
            l.setChoiceMode(GridView.CHOICE_MODE_NONE);
            if (item.isFolder())
            {
                DocumentFolderBrowserFragment.with(getActivity()).folder((Folder) item).shortcut(isShortcut())
                        .display();
            }
            return;
        }

        // In case of pick mode, we allow multiSelection
        if (mode == MODE_PICK && getActivity() instanceof PrivateDialogActivity && item.isDocument())
        {
            l.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
            if (pickedNodes.containsKey(item.getIdentifier()))
            {
                pickedNodes.remove(item.getIdentifier());
            }
            else
            {
                pickedNodes.put(item.getIdentifier(), item);
            }
            l.setItemChecked(position, true);
            checkValidationButton();
            return;
        }

        // In other case, listing mode
        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).getIdentifier().equals(item.getIdentifier());
        }
        l.setItemChecked(position, true);

        if (nActions != null && nActions.hasMultiSelectionEnabled())
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
            FragmentDisplayer.clearCentralPane(getActivity());
            if (nActions != null && !nActions.hasMultiSelectionEnabled())
            {
                nActions.finish();
            }
            getActivity().supportInvalidateOptionsMenu();
            displayPathShortcut(true);
        }
        else if (nActions == null || (nActions != null && !nActions.hasMultiSelectionEnabled()))
        {
            if (item.isFolder())
            {
                FragmentDisplayer.clearCentralPane(getActivity());
                DocumentFolderBrowserFragment.with(getActivity()).site(site).folder((Folder) item)
                        .shortcut(isShortcut()).display();
            }
            else if (item.isDocument())
            {
                NodeDetailsFragment.with(getActivity()).parentFolder(parentFolder).node(item).display();
            }
            else if (item instanceof Link)
            {
                if (item instanceof Document)
                {
                    NodeDetailsFragment.with(getActivity()).parentFolder(parentFolder)
                            .nodeId(((Link) item).getDestination()).display();
                }
                else if (item instanceof Folder)
                {
                    FragmentDisplayer.clearCentralPane(getActivity());
                    DocumentFolderBrowserFragment.with(getActivity()).site(site)
                            .folderIdentifier(((Link) item).getDestination()).shortcut(isShortcut()).display();
                }
            }
        }
    }

    public boolean onListItemLongClick(GridView l, View v, int position, long id)
    {
        // We disable long click during import mode.
        if (mode == MODE_IMPORT || mode == MODE_PICK) { return false; }

        if (nActions != null && nActions instanceof NodeDetailsActionMode)
        {
            nActions.finish();
        }

        Node n = (Node) l.getItemAtPosition(position);
        boolean b;
        if (n instanceof NodePlaceHolder)
        {
            getActivity().startActivity(new Intent(PrivateIntent.ACTION_DISPLAY_OPERATIONS)
                    .putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, SessionUtils.getAccount(getActivity()).getId()));
            b = false;
        }
        else
        {
            l.setItemChecked(position, true);
            b = startSelection(n);
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
                FragmentDisplayer.with(getActivity()).remove(android.R.id.tabcontent);
            }
        }
        return b;
    }

    public boolean hasActionMode()
    {
        return nActions != null;
    }

    private boolean startSelection(Node item)
    {
        if (nActions != null) { return false; }

        selectedItems.clear();
        selectedItems.add(item);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new NodeActions(DocumentFolderBrowserFragment.this, selectedItems);
        nActions.setOnFinishModeListener(new AbstractActions.onFinishModeListener()
        {
            @Override
            public void onFinish()
            {
                if (fab != null)
                {
                    fab.hide(true);
                    if (permission.canAddChildren())
                    {
                        fab.setImageResource(R.drawable.ic_content_add);
                        fab.setOnClickListener(onPrepareFabClickListener());
                        fab.show(true);
                    }
                }
                nActions = null;
                unselect();
                refreshListView();
            }
        });
        if (fab != null)
        {
            fab.hide(true);
            fab.setImageResource(R.drawable.ic_done_all_white);
            fab.setOnClickListener(onMultiSelectionFabClickListener());
            fab.show(true);
        }
        getActivity().startActionMode(nActions);
        return true;
    }

    // //////////////////////////////////////////////////////////////////////
    // REQUEST & RESULTS
    // //////////////////////////////////////////////////////////////////////
    @Override
    @Subscribe
    public void onResult(NodeChildrenEvent event)
    {
        if (getActivity() instanceof MainActivity && ((MainActivity) getActivity()).getCurrentNode() != null)
        {
            selectedItems.clear();
            selectedItems.add(((MainActivity) getActivity()).getCurrentNode());
        }

        if (event.parentFolder != null)
        {
            parentFolder = event.parentFolder;
            importFolder = parentFolder;
        }

        if (event.site != null)
        {
            this.site = event.site;
        }

        if (adapter == null)
        {
            adapter = onAdapterCreation();
            ((BaseListAdapter) adapter).setFragmentSettings(getArguments());
        }

        if (event.hasException)
        {
            if (adapter.getCount() == 0)
            {
                ev.setVisibility(View.VISIBLE);
            }

            if (event.exception != null && event.exception.getCause() instanceof CmisObjectNotFoundException)
            {
                displayEmptyView();
                if (viewById(R.id.empty_text) != null)
                {
                    ((TextView) viewById(R.id.empty_text)).setText(R.string.node_browser_folder_not_found);
                }
            }
            else
            {
                onResultError(event.exception);
            }
        }
        else
        {
            displayData(event);
        }
        ((NodeAdapter) adapter).setActivateThumbnail(hasActivateThumbnail());
        getActivity().invalidateOptionsMenu();
        displayPathShortcut();
        checkValidationButton();

        // Hide Loading progress
        refreshHelper.setRefreshComplete();
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        if (mode == MODE_PICK && adapter == null)
        {
            pickedNodes = fragmentPick.getNodeSelected(fieldId);
            return new ProgressNodeAdapter(this, GridAdapterHelper.getDisplayItemLayout(getActivity(), gv, displayMode),
                    parentFolder, new ArrayList<Node>(0), pickedNodes, mode);
        }
        else if (adapter == null) { return new ProgressNodeAdapter(this,
                GridAdapterHelper.getDisplayItemLayout(getActivity(), gv, displayMode), parentFolder,
                new ArrayList<Node>(0), selectedItems, mode); }
        return null;
    }

    // //////////////////////////////////////////////////////////////////////
    // ACTIONS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case RequestCode.FILEPICKER:
                if (data != null && PrivateIntent.ACTION_PICK_FILE.equals(data.getAction()))
                {
                    ActionUtils.actionPickFile(getFragmentManager().findFragmentByTag(TAG), RequestCode.FILEPICKER);
                }
                else if (data != null && data.getData() != null)
                {
                    String tmpPath = ActionUtils.getPath(getActivity(), data.getData());
                    if (tmpPath != null)
                    {
                        tmpFile = new File(tmpPath);
                    }
                    else
                    {
                        // Error case : Unable to find the file path associated
                        // to user pick.
                        // Sample : Picasa image case
                        ActionUtils.actionDisplayError(this,
                                new AlfrescoAppException(getString(R.string.error_unknown_filepath), true));
                    }
                }
                else if (data != null && data.getExtras() != null && data.getExtras().containsKey(Intent.EXTRA_STREAM))
                {
                    List<File> files = new ArrayList<File>();
                    List<Uri> uris = data.getExtras().getParcelableArrayList(Intent.EXTRA_STREAM);
                    for (Uri uri : uris)
                    {
                        files.add(new File(ActionUtils.getPath(getActivity(), uri)));
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
        newFragment.show(getActivity().getSupportFragmentManager(), AddContentDialogFragment.TAG);
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
            List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(selectedItems.size());
            for (File file : files)
            {
                requestsBuilder.add(new CreateDocumentRequest.Builder(importFolder, file.getName(),
                        new ContentFileProgressImpl(file))
                                .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
            }
            Operator.with(getActivity(), getAccount()).load(requestsBuilder);

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
        FragmentDisplayer.with(getActivity()).remove(CreateFolderDialogFragment.TAG);

        // Create and show the dialog.
        AddFolderDialogFragment.newInstance(parentFolder)
                .show(getActivity().getSupportFragmentManager().beginTransaction(), CreateFolderDialogFragment.TAG);
    }

    public void refresh()
    {
        super.refresh();

        // Display Refresh Progress
        refreshHelper.setRefreshing();
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu)
    {
        if (parentFolder == null) { return; }

        if (getActivity() instanceof MainActivity)
        {
            getMenu(getSession(), menu, parentFolder);

            if (hasDocument())
            {
                displayMenuItem = menu.add(Menu.NONE, R.id.menu_gallery, Menu.FIRST, R.string.display_gallery);
                displayMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }
        else if (getActivity() instanceof PublicDispatcherActivity || getActivity() instanceof BaseShortcutActivity)
        {
            // permission =
            // getSession().getServiceRegistry().getDocumentFolderService().getPermissions(parentFolder);
            if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), parentFolder,
                    ConfigurableActionHelper.ACTION_CREATE_FOLDER))
            {
                MenuItem mi = menu.add(Menu.NONE, R.id.menu_create_folder, Menu.FIRST, R.string.folder_create);
                mi.setIcon(R.drawable.ic_repository_light);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        MenuFragmentHelper.getMenu(getActivity(), menu);
    }

    public void getMenu(AlfrescoSession session, Menu menu, Folder parentFolder)
    {
        MenuItem mi;

        if (parentFolder == null) { return; }
        try
        {
            permission = session.getServiceRegistry().getDocumentFolderService().getPermissions(parentFolder);
        }
        catch (Exception e)
        {
            return;
        }

        mi = menu.add(Menu.NONE, R.id.menu_search_from_folder, Menu.FIRST + 10, R.string.search);
        mi.setIcon(R.drawable.ic_search_light);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), parentFolder,
                ConfigurableActionHelper.ACTION_CREATE_DOC)
                || ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), parentFolder,
                        ConfigurableActionHelper.ACTION_CREATE_FOLDER)
                || ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), parentFolder,
                        ConfigurableActionHelper.ACTION_NODE_UPLOAD))
        {
            displayFab();
        }
    }

    @Override
    protected OnClickListener onPrepareFabClickListener()
    {
        if (permission == null || !permission.canAddChildren()) { return null; }

        return new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BottomSheet.Builder builder = new BottomSheet.Builder(getActivity(), R.style.M_StyleDialog)
                        .title(R.string.add_menu);
                if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), parentFolder,
                        ConfigurableActionHelper.ACTION_CREATE_FOLDER))
                {
                    builder.sheet(R.id.menu_create_folder, R.drawable.ic_repository_light, R.string.folder_create);
                }

                if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), parentFolder,
                        ConfigurableActionHelper.ACTION_NODE_UPLOAD))
                {
                    builder.sheet(R.id.menu_upload, R.drawable.ic_upload, R.string.upload_title);
                }

                if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), parentFolder,
                        ConfigurableActionHelper.ACTION_CREATE_DOC))
                {
                    builder.sheet(R.id.menu_create_document, R.drawable.ic_doc_light, R.string.create_document);
                    builder.sheet(R.id.menu_device_capture_camera_photo, R.drawable.ic_camera, R.string.take_photo);
                    builder.sheet(R.id.menu_device_capture_camera_video, R.drawable.ic_videos, R.string.make_video);
                    if (hasAudioRecorder)
                    {
                        builder.sheet(R.id.menu_device_capture_mic_audio, R.drawable.ic_microphone,
                                R.string.record_audio);
                    }
                    if (ScanSnapManager.getInstance(getActivity()) != null
                            && ScanSnapManager.getInstance(getActivity()).hasScanSnapApplication())
                    {
                        builder.sheet(R.id.menu_scan_document, R.drawable.ic_camera, R.string.scan);
                    }
                }

                builder.grid().listener(new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        onOptionMenuItemSelected(which);
                    }
                }).show();
            }
        };
    }

    protected OnClickListener onMultiSelectionFabClickListener()
    {
        return new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectAll();
            }
        };
    }

    protected OnClickListener onCancelMultiSelectionFabClickListener()
    {
        return new OnClickListener()
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

    private void displayFab()
    {
        if (fab != null && fab.getVisibility() == View.GONE)
        {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(onPrepareFabClickListener());
            fab.show(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        if (!MenuFragmentHelper.canDisplayFragmentMenu(getActivity())) { return; }
        menu.clear();
        getMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return !onOptionMenuItemSelected(item.getItemId()) ? false : super.onOptionsItemSelected(item);
    }

    private boolean onOptionMenuItemSelected(int itemId)
    {
        if (getActivity() == null) { return false; }
        switch (itemId)
        {
            case R.id.menu_search_from_folder:
                AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                        AnalyticsManager.ACTION_QUICK_ACTIONS, AnalyticsManager.ACTION_SEARCH, 1, false);
                search();
                return true;
            case R.id.menu_create_folder:
                createFolder();
                AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                        AnalyticsManager.ACTION_QUICK_ACTIONS,
                        AnalyticsManager.ACTION_CREATE.concat(" " + AnalyticsManager.TYPE_FOLDER), 1, false);
                return true;
            case R.id.menu_upload:
                Intent i = new Intent(PrivateIntent.ACTION_PICK_FILE, null, getActivity(),
                        PublicDispatcherActivity.class);
                i.putExtra(PrivateIntent.EXTRA_FOLDER, AlfrescoStorageManager.getInstance(getActivity())
                        .getDownloadFolder(SessionUtils.getAccount(getActivity())));
                i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, SessionUtils.getAccount(getActivity()).getId());
                startActivityForResult(i, RequestCode.FILEPICKER);
                return true;
            case R.id.menu_create_document:
                DocumentTypesDialogFragment dialogft = DocumentTypesDialogFragment
                        .newInstance(SessionUtils.getAccount(getActivity()), TAG);
                dialogft.show(getFragmentManager(), DocumentTypesDialogFragment.TAG);
                return true;
            case R.id.menu_gallery:
                CarouselPreviewFragment.with(getActivity()).display();
                return true;
            case R.id.menu_device_capture_camera_photo:
            case R.id.menu_device_capture_camera_video:
            case R.id.menu_device_capture_mic_audio:
                DeviceCapture capture = DeviceCaptureHelper.createDeviceCapture((BaseActivity) getActivity(), itemId);
                if (getActivity() instanceof MainActivity)
                {
                    ((MainActivity) getActivity()).setCapture(capture);
                }
                return true;
            case R.id.menu_scan_document:
                if (ScanSnapManager.getInstance(getActivity()) != null)
                {
                    ScanSnapManager.getInstance(getActivity()).startPresetChooser(getActivity());
                    AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                            AnalyticsManager.ACTION_QUICK_ACTIONS, AnalyticsManager.ACTION_SCAN, 1, false);
                }
                return true;
            default:
                break;
        }
        return false;
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
     */
    public void remove(Node node)
    {
        if (adapter != null)
        {
            ((ProgressNodeAdapter) adapter).remove(node.getName());
            if (adapter.isEmpty())
            {
                displayEmptyView();
            }
        }
    }

    @Override
    public void selectAll()
    {
        if (nActions != null && adapter != null)
        {
            nActions.selectNodes(((ProgressNodeAdapter) adapter).getNodes());
            adapter.notifyDataSetChanged();
        }

        if (fab != null)
        {
            fab.toggle(true);
            fab.setImageResource(R.drawable.ic_close_dark);
            fab.setOnClickListener(onCancelMultiSelectionFabClickListener());
            fab.toggle(true);
        }
    }

    public void select(Node updatedNode)
    {
        selectedItems.add(updatedNode);
    }

    public void highLight(Node updatedNode)
    {
        unselect();
        selectedItems.add(updatedNode);
        adapter.notifyDataSetChanged();
    }

    public List<Node> getNodes()
    {
        if (adapter != null)
        {
            return ((ProgressNodeAdapter) adapter).getNodes() != null ? ((ProgressNodeAdapter) adapter).getNodes()
                    : new ArrayList<Node>(0);
        }
        else
        {
            return new ArrayList<>(0);
        }
    }

    private boolean hasDocument()
    {
        if (adapter != null)
        {
            for (Node node : ((ProgressNodeAdapter) adapter).getNodes())
            {
                if (node.isDocument()) { return true; }
            }
        }
        return false;
    }

    public Node getSelectedNodes()
    {
        return (selectedItems != null && !selectedItems.isEmpty()) ? selectedItems.get(0) : null;
    }

    // //////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////
    public void search()
    {
        // Use case : DocumentLibrary Site
        if (folderParameter == null && site != null)
        {
            folderParameter = parentFolder;
        }
        SearchFragment.with(getActivity()).folder(folderParameter).site(site).display();
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

    public Folder getParentFolder()
    {
        return parentFolder;
    }

    public Site getSite()
    {
        return site;
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
    private void checkValidationButton()
    {
        boolean enable = false;
        if (mode == MODE_IMPORT)
        {
            if (getActivity() instanceof FolderShortcutActivity)
            {
                validationButton.setEnabled(true);
                return;
            }

            if (parentFolder != null)
            {
                enable = ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), parentFolder,
                        ConfigurableActionHelper.ACTION_NODE_UPLOAD);
            }
            validationButton.setEnabled(enable);
        }
        else if (mode == MODE_PICK && selectedItems != null)
        {
            validationButton.setText(
                    String.format(MessageFormat.format(getString(R.string.picker_attach_document), pickedNodes.size()),
                            pickedNodes.size()));
            validationButton.setEnabled(!pickedNodes.isEmpty());
            validationButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    fragmentPick.onNodeSelected(fieldId, pickedNodes);
                }
            });
        }
    }

    public boolean isShortcut()
    {
        if (getArguments().containsKey(ARGUMENT_IS_SHORTCUT))
        {
            return (Boolean) getArguments().get(ARGUMENT_IS_SHORTCUT);
        }
        else
        {
            return false;
        }
    }

    @Override
    public void displayTitle()
    {
        displayPathShortcut();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onDocumentUpdated(UpdateNodeEvent event)
    {
        if (event.hasException) { return; }
        if (event.parentFolder == null || parentFolder == null
                || !event.parentFolder.getIdentifier().equals(parentFolder.getIdentifier())) { return; }
        Node updatedNode = event.data;
        remove(event.initialNode);

        if (adapter != null)
        {
            ((ProgressNodeAdapter) adapter).replaceNode(updatedNode);
            displayDataView();
        }
        if (getActivity() instanceof BaseActivity)
        {
            ((BaseActivity) getActivity()).removeWaitingDialog();
        }
    }

    @Subscribe
    public void onContentUpdated(UpdateContentEvent event)
    {
        if (event.hasException) { return; }
        Node updatedNode = event.data;
        remove(event.node);
        if (adapter != null)
        {
            ((ProgressNodeAdapter) adapter).replaceNode(updatedNode);
            displayDataView();
        }
        if (getActivity() instanceof BaseActivity)
        {
            ((BaseActivity) getActivity()).removeWaitingDialog();
        }
    }

    @Subscribe
    public void onNodeDeleted(DeleteNodeEvent event)
    {
        if (event.data == null) { return; }
        remove(event.data);
        if (adapter.getCount() == 0)
        {
            setListShown(true);
        }
    }

    @Subscribe
    public void onDocumentDownloaded(DownloadEvent event)
    {
        if (event.hasException) { return; }
        if (parentFolder != null && parentFolder.getIdentifier().equals(event.parentFolder.getIdentifier()))
        {
            ((ProgressNodeAdapter) adapter).replaceNode(event.document);
        }
    }

    @Subscribe
    public void onDocumentCreated(CreateDocumentEvent event)
    {
        if (event.hasException || adapter == null) { return; }
        boolean hasEmptyAdapter = true;
        hasEmptyAdapter = (adapter.getCount() == 0);
        if (parentFolder != null && parentFolder.getIdentifier().equals(event.parentFolder.getIdentifier()))
        {
            ((ProgressNodeAdapter) adapter).replaceNode(event.data);
        }
        if (hasEmptyAdapter)
        {
            displayDataView();
        }
    }

    @Subscribe
    public void onFolderCreated(CreateFolderEvent event)
    {
        Node node = event.data;
        if (node == null) { return; }
        boolean hasEmptyAdapter = true;
        hasEmptyAdapter = (adapter.getCount() == 0);
        ((ProgressNodeAdapter) adapter).replaceNode(node);
        if (hasEmptyAdapter)
        {
            displayDataView();
        }
        if (getActivity() instanceof BaseActivity)
        {
            ((BaseActivity) getActivity()).removeWaitingDialog();
        }
    }

    @Subscribe
    public void onFavoriteNodeEvent(FavoriteNodeEvent event)
    {
        if (event.hasException) { return; }
        if (adapter != null)
        {
            ((ProgressNodeAdapter) adapter).refreshOperations();
            refreshListView();
        }
        favorite(nodesToFavorite, doFavorite, true);
    }

    @Subscribe
    public void onSyncCompleted(SyncContentScanEvent event)
    {
        if (adapter != null)
        {
            ((ProgressNodeAdapter) adapter).refreshOperations();
            refreshListView();
        }
    }

    @Subscribe
    public void onSyncNodeEvent(SyncNodeEvent event)
    {
        if (event.hasException) { return; }
        if (SyncContentManager.getInstance(getActivity()).canSync(SessionUtils.getAccount(getActivity())))
        {
            SyncContentManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()),
                    event.node.getIdentifier());
            ((ProgressNodeAdapter) adapter).refreshOperations();
        }
        refreshListView();
    }

    /**
     * This method is specific to favorite and is bind with NodeActions.
     * Multiple favorite action must be done sequentially and not in parallel.
     * It's not supported by the server for older version.
     */
    public void favorite(List<Node> selectedItems, boolean dofavorite, boolean update)
    {
        if (selectedItems == null || selectedItems.isEmpty())
        {
            ((AlfrescoActivity) getActivity()).removeWaitingDialog();
            return;
        }
        nodesToFavorite = new ArrayList<Node>(selectedItems);
        doFavorite = dofavorite;
        Node node = nodesToFavorite.get(0);
        OperationBuilder requestBuilder = new FavoriteNodeRequest.Builder(parentFolder, node, doFavorite, true)
                .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG);
        Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(requestBuilder);
        nodesToFavorite.remove(0);

        if (!update)
        {
            int titleId = R.string.unfavorite;
            int iconId = R.drawable.ic_unfavorite_dark;
            if (doFavorite)
            {
                titleId = R.string.favorite;
                iconId = R.drawable.ic_favorite_light;
            }
            OperationWaitingDialogFragment
                    .newInstance(FavoriteNodeRequest.TYPE_ID, iconId, getActivity().getString(titleId), null,
                            parentFolder, selectedItems.size(), false)
                    .show(getActivity().getSupportFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }

    public void sync(List<Node> selectedItems, boolean doSync, boolean update)
    {
        if (selectedItems == null || selectedItems.isEmpty())
        {
            ((AlfrescoActivity) getActivity()).removeWaitingDialog();
            return;
        }

        List<OperationBuilder> requestsBuilder = new ArrayList<>(selectedItems.size());
        for (Node node : selectedItems)
        {
            requestsBuilder.add(new SyncNodeRequest.Builder(parentFolder, node, doSync, true)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }
        String operationId = Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(requestsBuilder);

        if (!update)
        {
            int titleId = R.string.unsync;
            int iconId = R.drawable.ic_sync_light;
            if (doSync)
            {
                titleId = R.string.sync;
                iconId = R.drawable.ic_sync_light;
            }
            OperationWaitingDialogFragment
                    .newInstance(SyncNodeRequest.TYPE_ID, iconId, getActivity().getString(titleId), null, parentFolder,
                            selectedItems.size(), operationId)
                    .show(getActivity().getSupportFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity appActivity)
    {
        return new Builder(appActivity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        /** By Folder Object. */
        public Builder(FragmentActivity activity, Folder folder)
        {
            this(activity, folder, null, null, null);
        }

        /** By Folder PATH. */
        public Builder(FragmentActivity activity, String folderPath)
        {
            this(activity, null, folderPath, null, null);
        }

        /** By SITE Object. */
        public Builder(FragmentActivity activity, Site site)
        {
            this(activity, null, null, site, null);
        }

        /** By Folder Type ID. */
        public Builder(FragmentActivity activity, int folderId)
        {
            this(activity, null, null, null, folderId);
        }

        protected Builder(FragmentActivity activity, Folder parentFolder, String pathFolder, Site site,
                Integer folderId)
        {
            this(activity);
            BundleUtils.addIfNotEmpty(extraConfiguration, createBundleArgs(parentFolder, pathFolder, site));
            BundleUtils.addIfNotNull(extraConfiguration, ARGUMENT_FOLDER_TYPE_ID, folderId);
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            viewConfigModel = new RepositoryConfigModel(configuration);
            this.extraConfiguration = new Bundle();
            if (configuration != null && configuration.containsKey(NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID))
            {
                String folderTypeValue = JSONConverter.getString(configuration,
                        NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID);
                if (NodeBrowserTemplate.FOLDER_TYPE_SHARED.equalsIgnoreCase(folderTypeValue))
                {
                    extraConfiguration.putSerializable(ARGUMENT_FOLDER_TYPE_ID, NodeChildrenRequest.FOLDER_SHARED);
                    shortcut(true);
                }
                else if (NodeBrowserTemplate.FOLDER_TYPE_USERHOME.equalsIgnoreCase(folderTypeValue))
                {
                    extraConfiguration.putSerializable(ARGUMENT_FOLDER_TYPE_ID, NodeChildrenRequest.FOLDER_USER_HOMES);
                    shortcut(true);
                }
            }
            else if (configuration != null && ((configuration.containsKey(NodeBrowserTemplate.ARGUMENT_LABEL)
                    && configuration.size() > 1)
                    || (!configuration.containsKey(NodeBrowserTemplate.ARGUMENT_LABEL) && configuration.size() > 0)))
            {
                shortcut(true);
            }
            else
            {
                shortcut(false);
            }

            this.templateArguments = new String[] { ARGUMENT_FOLDER_NODEREF, ARGUMENT_SITE_SHORTNAME, ARGUMENT_PATH,
                    ARGUMENT_FOLDER_TYPE_ID, ARGUMENT_FOLDER, ARGUMENT_SITE, ARGUMENT_IS_SHORTCUT };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder folderIdentifier(String folderIdentifier)
        {
            extraConfiguration.putString(ARGUMENT_FOLDER_NODEREF, folderIdentifier);
            return this;
        }

        public Builder site(Site site)
        {
            extraConfiguration.putSerializable(ARGUMENT_SITE, site);
            return this;
        }

        public Builder siteShortName(String siteShortName)
        {
            extraConfiguration.putSerializable(ARGUMENT_SITE_SHORTNAME, siteShortName);
            return this;
        }

        public Builder folder(Folder folder)
        {
            extraConfiguration.putSerializable(ARGUMENT_FOLDER, folder);
            return this;
        }

        public Builder path(String pathFolder)
        {
            extraConfiguration.putSerializable(ARGUMENT_PATH, pathFolder);
            return this;
        }

        public Builder shortcut(boolean isShortCut)
        {
            extraConfiguration.putSerializable(ARGUMENT_IS_SHORTCUT, isShortCut);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}
