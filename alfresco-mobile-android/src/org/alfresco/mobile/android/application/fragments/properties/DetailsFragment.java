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
package org.alfresco.mobile.android.application.fragments.properties;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.DownloadTask;
import org.alfresco.mobile.android.api.asynchronous.NodeUpdateLoader;
import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.DocumentImpl;
import org.alfresco.mobile.android.api.services.impl.AbstractDocumentFolderServiceImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.authentication.AuthenticationProvider;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.comments.CommentsFragment;
import org.alfresco.mobile.android.application.fragments.tags.TagsListNodeFragment;
import org.alfresco.mobile.android.application.fragments.versions.VersionFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.documentfolder.actions.DownloadTaskCallback;
import org.alfresco.mobile.android.ui.documentfolder.actions.UpdateLoaderCallback;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeUpdateListener;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;
import org.alfresco.mobile.android.ui.manager.RenditionManager;
import org.alfresco.mobile.android.ui.manager.StorageManager;
import org.alfresco.mobile.android.ui.properties.PropertiesFragment;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.apache.chemistry.opencmis.commons.enums.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class DetailsFragment extends PropertiesFragment implements OnTabChangeListener
{

    public static final String TAG = "DetailsFragment";
    
    private TabHost mTabHost;

    public static final String ARGUMENT_NODE = "node";
    
    protected RenditionManager renditionManager;


    public DetailsFragment()
    {
    }
    
    protected Node node;

    
    public static Bundle createBundleArgs(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    public static DetailsFragment newInstance(Node n)
    {
        DetailsFragment bf = new DetailsFragment();
        bf.setArguments(createBundleArgs(n));
        return bf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getsession(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);
        alfSession = SessionUtils.getsession(getActivity());
        View v = inflater.inflate(R.layout.sdk_details, container, false);
        
        node = (Node) getArguments().get(ARGUMENT_NODE);
        renditionManager = new RenditionManager(getActivity(), alfSession);

        // Header
        TextView tv = (TextView) v.findViewById(R.id.title);
        tv.setText(node.getName());
        tv = (TextView) v.findViewById(R.id.details);
        tv.setText(Formatter.createContentBottomText(getActivity(), node, true));

        // Preview
        ImageView iv = (ImageView) v.findViewById(R.id.icon);
        int iconId = R.drawable.mime_folder;
        if (node.isDocument()){
            iconId = MimeTypeManager.getIcon(node.getName());
            renditionManager.display(iv, node, iconId);
        } else {
            iv.setImageResource(iconId);
        }

        // Description
        tv = (TextView) v.findViewById(R.id.description);
        if (node.getDescription() != null && node.getDescription().length() > 0)
        {
            tv.setVisibility(View.VISIBLE);
            tv.setText(node.getDescription());
        }
        else
        {
            tv.setVisibility(View.GONE);
        }
        
        //TAB
        
        mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
        setupTabs();
        
        if (mTabHost == null){
            ViewGroup parent = (ViewGroup) v.findViewById(R.id.metadata); 
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_GENERAL, null, null, false);
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_GEOGRAPHIC, R.drawable.ic_location,
                    new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            ActionManager.actionShowMap(DetailsFragment.this, node.getName(),
                                    node.getProperty(ContentModel.PROP_LATITUDE).getValue().toString(),
                                    node.getProperty(ContentModel.PROP_LONGITUDE).getValue().toString());
                        }
                    });
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_EXIF, null, null);
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_AUDIO, null, null);
        }
           

        //BUTTONS
        Button b = (Button) v.findViewById(R.id.action_openin);
        if (node.isDocument() && ((DocumentImpl) node).hasAllowableAction(Action.CAN_GET_CONTENT_STREAM.value())
                && ((Document) node).getContentStreamLength() > 0)
        {
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    openin();
                }
            });
        }
        else
        {
            b.setVisibility(View.GONE);
        }

        /*
         * b = (Button) v.findViewById(R.id.display_comments); if
         * (node.getProperty("fm:commentCount") != null &&
         * node.getProperty("fm:commentCount").getValue() != null) {
         * b.setText(b.getText() + " (" +
         * node.getProperty("fm:commentCount").getValue().toString() + ") "); }
         * if (alfSession.getServiceRegistry().getCommentService() != null) {
         * b.setOnClickListener(new OnClickListener() {
         * @Override public void onClick(View v) { ((MainActivity)
         * getActivity()).addComments(node); } }); } else {
         * b.setVisibility(View.GONE); } b = (Button)
         * v.findViewById(R.id.display_all_versions); if (((DocumentImpl)
         * node).hasAllowableAction(Action.CAN_GET_ALL_VERSIONS.value())) {
         * b.setOnClickListener(new OnClickListener() {
         * @Override public void onClick(View v) { ((MainActivity)
         * getActivity()).addVersions((Document) node); } }); } else {
         * b.setVisibility(View.GONE); }
         */

        ImageButton ba = (ImageButton) v.findViewById(R.id.like);
        if (alfSession.getRepositoryInfo().getCapabilities().doesSupportLikingNodes())
        {
            ba.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    like(v);
                }
            });
        }
        else
        {
            ba.setVisibility(View.GONE);
        }
        
        IsLikedLoaderCallBack lcb = new IsLikedLoaderCallBack(alfSession, getActivity(), node);
        lcb.setImageButton(ba);
        lcb.execute(false);
        
        ba = (ImageButton) v.findViewById(R.id.share);
        ba.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                share();  
            }
        });
        
        //((MainActivity) getActivity()).addComments(node);
        if (DisplayUtils.hasRightPane(getActivity()))
            ((MainActivity) getActivity()).addExtraDetails(node);

        return v;
    }

    @Override
    public void onStart()
    {
        DisplayUtils.setTitleFragmentPlace(getActivity(), "Properties");
        ((MainActivity) getActivity()).setCurrentNode(node);
        getActivity().invalidateOptionsMenu();
        if (mTabHost != null)
            mTabHost.setCurrentTabByTag(TAB_METADATA);
        super.onStart();
    }

    @Override
    public void onDestroy()
    {
        getActivity().invalidateOptionsMenu();
        ((MainActivity) getActivity()).setCurrentNode(null);
        super.onDestroy();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void share()
    {
        //TODO Share Link OR Content
        ActionManager.actionShareLink(this, ((AbstractDocumentFolderServiceImpl) alfSession.getServiceRegistry()
                .getDocumentFolderService()).getDownloadUrl((Document) node));
    }

    public void openin()
    {
        DownloadTask dlt = new DownloadTask(alfSession, (Document) node, getDownloadFile());
        dlt.setDl(new DownloadTaskCallback(this, (Document) node));
        dlt.execute();
    }

    private File getDownloadFile()
    {
        //File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File folder = StorageManager.getDownloadFolder(getActivity(), alfSession.getBaseUrl(), alfSession.getPersonIdentifier());
        return new File(folder, node.getName());
    }

    public void download()
    {
        Uri uri = Uri.parse(((AbstractDocumentFolderServiceImpl) alfSession.getServiceRegistry().getDocumentFolderService())
                .getDownloadUrl((Document) node));

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Activity.DOWNLOAD_SERVICE);

        Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(node.getName())
                .setDescription(node.getDescription())
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(
                        Uri.fromFile(StorageManager.getDownloadFile(getActivity(), alfSession.getBaseUrl(),
                                alfSession.getPersonIdentifier(), node.getName())));

        AuthenticationProvider auth = ((AbstractAlfrescoSessionImpl) SessionUtils.getsession(getActivity()))
                .getAuthenticationProvider();
        Map<String, List<String>> httpHeaders = auth.getHTTPHeaders();
        if (httpHeaders != null)
        {
            for (Map.Entry<String, List<String>> header : httpHeaders.entrySet())
            {
                if (header.getValue() != null)
                {
                    for (String value : header.getValue())
                        request.addRequestHeader(header.getKey(), value);
                }
            }
        }

        manager.enqueue(request);
    }

    public void update()
    {
        ActionManager.actionPickFile(this, PublicIntent.REQUESTCODE_FILEPICKER);
    }

    public void rename()
    {
        ActionManager.actionPickFile(this, PublicIntent.REQUESTCODE_FILEPICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case PublicIntent.REQUESTCODE_SAVE_BACK:
                if (getDownloadFile().length() != ((Document) node).getContentStreamLength())
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.action_save_back);
                    builder.setMessage(node.getName() + " "
                            + getResources().getString(R.string.action_save_back_description));
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            UpdateLoaderCallback up = new UpdateLoaderCallback(alfSession, getActivity(),
                                    (Document) node, getDownloadFile());
                            up.setOnUpdateListener(saveBackListener);
                            getLoaderManager().initLoader(NodeUpdateLoader.ID, null, up);
                            getLoaderManager().getLoader(NodeUpdateLoader.ID).forceLoad();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                break;
            case PublicIntent.REQUESTCODE_FILEPICKER:
                if (data != null && data.getData() != null)
                {
                    File f = new File(ActionManager.getPath(getActivity(), data.getData()));
                    UpdateLoaderCallback up = new UpdateLoaderCallback(alfSession, getActivity(), (Document) node, f);
                    up.setOnUpdateListener(listener);
                    getLoaderManager().initLoader(NodeUpdateLoader.ID, null, up);
                    getLoaderManager().getLoader(NodeUpdateLoader.ID).forceLoad();
                }
                break;
            default:
                break;
        }
    }

    private OnNodeUpdateListener saveBackListener = new OnNodeUpdateListener()
    {
        @Override
        public void afterUpdate(Node node)
        {
            ((MainActivity) getActivity()).setCurrentNode(node);
            ActionManager.actionRefresh(DetailsFragment.this, IntentIntegrator.CATEGORY_REFRESH_ALL,
                    PublicIntent.NODE_TYPE);
            MessengerManager.showToast(getActivity(),
                    node.getName() + " " + getResources().getString(R.string.action_save_back_sucess));
            refreshThumbnail(node);
        }

        @Override
        public void beforeUpdate(Node node)
        {

        }
    };

    private OnNodeUpdateListener listener = new OnNodeUpdateListener()
    {

        @Override
        public void afterUpdate(Node node)
        {
            ((MainActivity) getActivity()).setCurrentNode(node);
            ActionManager.actionRefresh(DetailsFragment.this, IntentIntegrator.CATEGORY_REFRESH_ALL,
                    PublicIntent.NODE_TYPE);
            MessengerManager.showToast(getActivity(),
                    node.getName() + " " + getResources().getString(R.string.action_update_sucess));
            refreshThumbnail(node);
        }

        @Override
        public void beforeUpdate(Node node)
        {
        }
    };

    private void refreshThumbnail(Node node)
    {
        if (node.isDocument())
        {
            if ((ImageView) getActivity().findViewById(R.id.icon) != null)
            {
                renditionManager.display((ImageView) getActivity().findViewById(R.id.icon), node,
                        MimeTypeManager.getIcon(node.getName()));
            }
        }
    }

    public void delete()
    {
        NodeActions.delete(getActivity(), this, node);
    }

    public void edit()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(UpdateDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        UpdateDialogFragment newFragment = UpdateDialogFragment.newInstance(node);
        newFragment.show(ft, UpdateDialogFragment.TAG);
    }
    
    public void like(View v)
    {
        IsLikedLoaderCallBack lcb = new IsLikedLoaderCallBack(alfSession, getActivity(), node);
        lcb.setImageButton((ImageButton) v.findViewById(R.id.like));
        lcb.execute(true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public static void getMenu(AlfrescoSession session, Menu menu, Node node)
    {
        MenuItem mi;

        /*mi = menu.add(Menu.NONE, MenuActionItem.MENU_SHARE, Menu.FIRST + MenuActionItem.MENU_SHARE,
                R.string.action_share);
        mi.setIcon(R.drawable.ic_share);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);*/

        if (node.isDocument())
        {
            if (((DocumentImpl) node).hasAllowableAction(Action.CAN_GET_CONTENT_STREAM.value())
                    && ((Document) node).getContentStreamLength() > 0)
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_DOWNLOAD, Menu.FIRST + MenuActionItem.MENU_DOWNLOAD,
                        R.string.action_share);
                mi.setIcon(R.drawable.ic_download);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (((DocumentImpl) node).hasAllowableAction(Action.CAN_SET_CONTENT_STREAM.value()))
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_UPDATE, Menu.FIRST + MenuActionItem.MENU_UPDATE,
                        R.string.action_upload);
                mi.setIcon(R.drawable.ic_upload);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
        
        if (session.getServiceRegistry().getDocumentFolderService().getPermissions(node).canEdit())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT,
                    R.string.action_edit);
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (session.getServiceRegistry().getDocumentFolderService().getPermissions(node).canDelete())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE, Menu.FIRST + MenuActionItem.MENU_DELETE,
                    R.string.action_delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    public void getMenu(Menu menu)
    {
        getMenu(alfSession, menu, node);
    }

    public Node getCurrentNode()
    {
        return node;
    }
    
    private static final String TAB_METADATA = "Metadata";
    
    private static final String TAB_COMMENTS = "Comments";

    private static final String TAB_HISTORY = "History";

    private static final String TAB_TAGS = "Tags";

    private void setupTabs()
    {
        if (mTabHost == null) return;
        
        mTabHost.setup(); // you must call this before adding your tabs!

        if (node.isDocument())
            mTabHost.addTab(newTab(TAB_HISTORY, R.string.action_versions, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_METADATA, R.string.metadata, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_COMMENTS, R.string.action_comments, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_TAGS, R.string.action_tags, android.R.id.tabcontent));
        mTabHost.setOnTabChangedListener(this);
    }

    private TabSpec newTab(String tag, int labelId, int tabContentId)
    {
        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setContent(tabContentId);
        tabSpec.setIndicator(this.getText(labelId));
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId)
    {
        if (TAB_METADATA.equals(tabId))
            addMetadata(node);
        else if (TAB_COMMENTS.equals(tabId))
            addComments(node);
        else if (TAB_HISTORY.equals(tabId) && node.isDocument())
            addVersions((Document) node);
        else if (TAB_TAGS.equals(tabId))
            addTags(node);
    }

    public void addComments(Node n)
    {
        BaseFragment frag = CommentsFragment.newInstance(n);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, android.R.id.tabcontent, CommentsFragment.TAG, false);
    }
    
    public void addMetadata(Node n)
    {
        int layoutid = R.id.metadata;
        if (getResources().getBoolean(R.bool.tablet_middle))
            layoutid = android.R.id.tabcontent;
        BaseFragment frag = MetadataFragment.newInstance(n);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, layoutid, MetadataFragment.TAG, false);
    }

    public void addVersions(Document d)
    {
        BaseFragment frag = VersionFragment.newInstance(d);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, android.R.id.tabcontent, VersionFragment.TAG, false);
    }
    
    public void addTags(Node d)
    {
        BaseFragment frag = TagsListNodeFragment.newInstance(d);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, android.R.id.tabcontent, TagsListNodeFragment.TAG, false);
    }
    
}
