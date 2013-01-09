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
package org.alfresco.mobile.android.application.fragments.properties;

import java.io.File;
import java.util.Date;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.DocumentImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.exception.AlfrescoAppException;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.browser.DownloadDialogFragment;
import org.alfresco.mobile.android.application.fragments.comments.CommentsFragment;
import org.alfresco.mobile.android.application.fragments.tags.TagsListNodeFragment;
import org.alfresco.mobile.android.application.fragments.versions.VersionFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeUpdateListener;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.RenditionManager;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.apache.chemistry.opencmis.commons.enums.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class DetailsFragment extends MetadataFragment implements OnTabChangeListener
{

    public static final String TAG = "DetailsFragment";

    private TabHost mTabHost;

    public static final String ARGUMENT_NODE = "node";

    protected RenditionManager renditionManager;

    protected Date downloadDateTime;

    protected Integer tabSelected = null;

    protected Integer tabSelection = null;

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
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);

        container.setVisibility(View.VISIBLE);
        alfSession = SessionUtils.getSession(getActivity());
        View v = inflater.inflate(R.layout.app_details, container, false);

        node = (Node) getArguments().get(ARGUMENT_NODE);
        if (node == null) { return null; }

        renditionManager = new RenditionManager(getActivity(), alfSession);

        // Header
        TextView tv = (TextView) v.findViewById(R.id.title);
        tv.setText(node.getName());
        tv = (TextView) v.findViewById(R.id.details);
        tv.setText(Formatter.createContentBottomText(getActivity(), node, true));

        // Preview
        ImageView iv = (ImageView) v.findViewById(R.id.icon);
        int iconId = R.drawable.mime_folder;
        if (node.isDocument())
        {
            iconId = MimeTypeManager.getIcon(node.getName());
            if (((Document) node).isLatestVersion())
            {
                renditionManager.display(iv, node, iconId);
            }
            else
            {
                iv.setImageResource(iconId);
            }

            iv.setOnClickListener(new OnClickListener()
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

        // TAB
        if (savedInstanceState != null)
        {
            tabSelection = savedInstanceState.getInt("TAB");
            savedInstanceState.remove("TAB");
        }

        mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
        setupTabs();

        if (mTabHost == null)
        {
            ViewGroup parent = (ViewGroup) v.findViewById(R.id.metadata);
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_GENERAL, false);
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_GEOGRAPHIC);
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_EXIF);
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_AUDIO);
        }
        Log.d(TAG, "END TAB");

        // BUTTONS
        ImageView b = (ImageView) v.findViewById(R.id.action_openin);
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

        b = (ImageView) v.findViewById(R.id.action_geolocation);
        if (node.isDocument() && node.hasAspect(ContentModel.ASPECT_GEOGRAPHIC))
        {
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ActionManager.actionShowMap(DetailsFragment.this, node.getName(),
                            node.getProperty(ContentModel.PROP_LATITUDE).getValue().toString(),
                            node.getProperty(ContentModel.PROP_LONGITUDE).getValue().toString());
                }
            });
        }
        else
        {
            b.setVisibility(View.GONE);
        }

        b = (ImageView) v.findViewById(R.id.like);
        if (alfSession != null && alfSession.getRepositoryInfo() != null
                && alfSession.getRepositoryInfo().getCapabilities() != null
                && alfSession.getRepositoryInfo().getCapabilities().doesSupportLikingNodes())
        {
            IsLikedLoaderCallBack lcb = new IsLikedLoaderCallBack(alfSession, getActivity(), node);
            lcb.setImageButton(b);
            lcb.setProgressView(v.findViewById(R.id.like_progress));
            lcb.execute(false);

            b.setOnClickListener(new OnClickListener()
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
            b.setVisibility(View.GONE);
            if (v.findViewById(R.id.like_progress) != null)
            {
                v.findViewById(R.id.like_progress).setVisibility(View.GONE);
            }
        }

        b = (ImageView) v.findViewById(R.id.action_share);
        if (node.isDocument())
        {
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    share();
                }
            });
        }
        else
        {
            b.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onStart()
    {
        ((MainActivity) getActivity()).setCurrentNode(node);
        getActivity().invalidateOptionsMenu();
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            getActivity().setTitle(R.string.details);
            getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        }
        else
        {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
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
        if (node.isFolder()) return;

        final MainActivity activity = ((MainActivity) getActivity());
        boolean cloud = (alfSession instanceof CloudSession);

        // TODO Add a mechanism at SDK to store parent in each node, so that we
        // can share links to documents
        // at all levels, rather than just from browser itself.
        boolean canShareLink = (activity.getFragment(ChildrenBrowserFragment.TAG) != null);

        if (!cloud || !canShareLink)
        {
            // Only sharing as attachment is allowed when we're not on a cloud
            // account
            Bundle b = new Bundle();
            b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
            b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_EMAIL);
            DialogFragment frag = new DownloadDialogFragment();
            frag.setArguments(b);
            frag.show(getFragmentManager(), DownloadDialogFragment.TAG);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.app_name);
            builder.setMessage(R.string.link_or_attach);

            builder.setPositiveButton(R.string.full_attachment, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    // TODO Change it !
                    Bundle b = new Bundle();
                    b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
                    b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_EMAIL);
                    DialogFragment frag = new DownloadDialogFragment();
                    frag.setArguments(b);
                    frag.show(getFragmentManager(), DownloadDialogFragment.TAG);
                }
            });
            builder.setNegativeButton(R.string.link_to_repo, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    Folder parentFolder = ((ChildrenBrowserFragment) activity.getFragment(ChildrenBrowserFragment.TAG))
                            .getParent();
                    if (parentFolder != null)
                    {
                        String path = parentFolder.getPropertyValue("cmis:path");
                        if (path.length() > 0)
                        {
                            if (path.startsWith("/Sites/"))
                            {
                                String sub1 = path.substring(7); // Get
                                                                 // past
                                                                 // the
                                                                 // '/Sites/'
                                int idx = sub1.indexOf('/'); // Find end
                                                             // of
                                                             // site
                                                             // name
                                if (idx == -1)
                                {
                                    idx = sub1.length();
                                }
                                String siteName = sub1.substring(0, idx);
                                String nodeID = NodeRefUtils.getCleanIdentifier(node.getIdentifier());
                                String fullPath = String.format(getString(R.string.cloud_share_url), siteName, nodeID);
                                ActionManager.actionShareLink(DetailsFragment.this, fullPath);
                            }
                            else
                            {
                                Log.i(getString(R.string.app_name), "Site path not as expected: no /sites/");
                            }
                        }
                        else
                        {
                            Log.i(getString(R.string.app_name), "Site path not as expected: no parent path");
                        }
                    }
                    else
                    {
                        Log.i(getString(R.string.app_name), "Site path not as expected: No parent folder");
                    }

                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void openin()
    {
        Bundle b = new Bundle();
        b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
        b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_OPEN);
        DialogFragment frag = new DownloadDialogFragment();
        frag.setArguments(b);
        frag.show(getFragmentManager(), DownloadDialogFragment.TAG);
    }

    public void download()
    {
        NodeActions.download(getActivity(), node);
    }

    public void update()
    {
        ActionManager.actionPickFile(this, PublicIntent.REQUESTCODE_FILEPICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case PublicIntent.REQUESTCODE_SAVE_BACK:
                final File dlFile = NodeActions.getDownloadFile(getActivity(), node);

                long datetime = dlFile.lastModified();
                Date d = new Date(datetime);

                boolean modified = (d != null && downloadDateTime != null) ? d.after(downloadDateTime) : false;

                if (modified
                        && alfSession.getServiceRegistry().getDocumentFolderService().getPermissions(node).canEdit())
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.save_back);
                    builder.setMessage(node.getName() + " " + getResources().getString(R.string.save_back_description));
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            UpdateContentCallback up = new UpdateContentCallback(alfSession, getActivity(),
                                    (Document) node, dlFile);
                            up.setOnUpdateListener(listener);
                            getLoaderManager().initLoader(UpdateContentLoader.ID, null, up);
                            getLoaderManager().getLoader(UpdateContentLoader.ID).forceLoad();
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
                    String tmpPath = ActionManager.getPath(getActivity(), data.getData());
                    if (tmpPath != null)
                    {
                        File f = new File(ActionManager.getPath(getActivity(), data.getData()));
                        UpdateContentCallback up = new UpdateContentCallback(alfSession, getActivity(),
                                (Document) node, f);
                        up.setOnUpdateListener(listener);
                        getLoaderManager().initLoader(UpdateContentLoader.ID, null, up);
                        getLoaderManager().getLoader(UpdateContentLoader.ID).forceLoad();
                    }
                    else
                    {
                        // Error case : Unable to find the file path associated
                        // to user pick.
                        // Sample : Picasa image case
                        ActionManager.actionDisplayError(DetailsFragment.this, new AlfrescoAppException(
                                getString(R.string.error_unknown_filepath), true));
                    }
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
                    node.getName() + " " + getResources().getString(R.string.save_back_sucess));
            refreshThumbnail(node);
        }

        boolean hasWaiting = false;

        @Override
        public void beforeUpdate(Node node)
        {
            if (!hasWaiting && getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
            {
                new WaitingDialogFragment().show(getFragmentManager(), WaitingDialogFragment.TAG);
            }
            hasWaiting = true;
        }

        @Override
        public void onExeceptionDuringUpdate(Exception e)
        {
            ActionManager.actionDisplayError(DetailsFragment.this, e);
        }
    };

    private OnNodeUpdateListener listener = new OnNodeUpdateListener()
    {
        public boolean hasWaiting = false;

        @Override
        public void beforeUpdate(Node node)
        {
            if (!hasWaiting && getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
            {
                new WaitingDialogFragment().show(getFragmentManager(), WaitingDialogFragment.TAG);
            }
            hasWaiting = true;
        }

        @Override
        public void afterUpdate(Node node)
        {
            ((MainActivity) getActivity()).setCurrentNode(node);
            ActionManager.actionRefresh(DetailsFragment.this, IntentIntegrator.CATEGORY_REFRESH_ALL,
                    PublicIntent.NODE_TYPE);
            MessengerManager.showToast(getActivity(),
                    node.getName() + " " + getResources().getString(R.string.update_sucess));
            refreshThumbnail(node);
        }

        @Override
        public void onExeceptionDuringUpdate(Exception e)
        {
            ActionManager.actionDisplayError(DetailsFragment.this, e);
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
        NodeActions.edit(getActivity(), node);
    }

    public void like(View v)
    {
        IsLikedLoaderCallBack lcb = new IsLikedLoaderCallBack(alfSession, getActivity(), node);
        lcb.setImageButton((ImageView) v.findViewById(R.id.like));
        lcb.setProgressView(((ViewGroup) v.getParent()).findViewById(R.id.like_progress));
        lcb.execute(true);
    }

    public void comment()
    {
        addComments(node, DisplayUtils.getMainPaneId(getActivity()), true);
    }

    public void versions()
    {
        addVersions((Document) node, DisplayUtils.getMainPaneId(getActivity()), true);
    }

    public void tags()
    {
        addTags(node, DisplayUtils.getMainPaneId(getActivity()), true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public static void getMenu(AlfrescoSession session, Activity activity, Menu menu, Node node)
    {
        MenuItem mi;

        if (node == null) { return; }

        if (node.isDocument())
        {
            if (((Document) node).getContentStreamLength() > 0)
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_DOWNLOAD, Menu.FIRST + MenuActionItem.MENU_DOWNLOAD,
                        R.string.share);
                mi.setIcon(R.drawable.ic_download_dark);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (((Document) node).isLatestVersion()
                    && ((DocumentImpl) node).hasAllowableAction(Action.CAN_SET_CONTENT_STREAM.value()))
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_UPDATE, Menu.FIRST + MenuActionItem.MENU_UPDATE,
                        R.string.upload);
                mi.setIcon(R.drawable.ic_upload);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        if (session.getServiceRegistry().getDocumentFolderService().getPermissions(node).canEdit())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT, R.string.edit);
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (session.getServiceRegistry().getDocumentFolderService().getPermissions(node).canDelete())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE, Menu.FIRST + MenuActionItem.MENU_DELETE,
                    R.string.delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (!DisplayUtils.hasCentralPane(activity))
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_COMMENT, Menu.FIRST + MenuActionItem.MENU_COMMENT,
                    R.string.comments);
            mi.setIcon(R.drawable.ic_comment);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            if (node.isDocument())
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_VERSION_HISTORY, Menu.FIRST
                        + MenuActionItem.MENU_VERSION_HISTORY, R.string.version_history);
                mi.setIcon(R.drawable.ic_menu_recent_history);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            mi = menu.add(Menu.NONE, MenuActionItem.MENU_TAGS, Menu.FIRST + MenuActionItem.MENU_TAGS, R.string.tags);
            mi.setIcon(R.drawable.mime_tags);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    public void getMenu(Menu menu)
    {
        getMenu(alfSession, getActivity(), menu, node);
    }

    public Node getCurrentNode()
    {
        return node;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (tabSelected != null)
        {
            outState.putInt("TAB", tabSelected);
        }
    }

    private static final String TAB_METADATA = "Metadata";

    private static final String TAB_COMMENTS = "Comments";

    private static final String TAB_HISTORY = "History";

    private static final String TAB_TAGS = "Tags";

    private void setupTabs()
    {
        if (mTabHost == null) { return; }

        mTabHost.setup(); // you must call this before adding your tabs!
        mTabHost.setOnTabChangedListener(this);

        mTabHost.addTab(newTab(TAB_METADATA, R.string.metadata, android.R.id.tabcontent));
        if (node.isDocument())
        {
            mTabHost.addTab(newTab(TAB_HISTORY, R.string.action_version, android.R.id.tabcontent));
        }
        mTabHost.addTab(newTab(TAB_COMMENTS, R.string.comments, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_TAGS, R.string.tags, android.R.id.tabcontent));

        if (tabSelection != null)
        {
            if (tabSelection == 0) return;
            int index = (node.isDocument()) ? tabSelection : tabSelection - 1;
            mTabHost.setCurrentTab(index);
        }
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
        {
            tabSelected = 0;
            addMetadata(node);
        }
        else if (TAB_COMMENTS.equals(tabId))
        {
            tabSelected = 2;
            addComments(node);
        }
        else if (TAB_HISTORY.equals(tabId) && node.isDocument())
        {
            tabSelected = 1;
            addVersions((Document) node);
        }
        else if (TAB_TAGS.equals(tabId))
        {
            tabSelected = 3;
            addTags(node);
        }
    }

    public void addComments(Node n, int layoutId, boolean backstack)
    {
        BaseFragment frag = CommentsFragment.newInstance(n);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, layoutId, CommentsFragment.TAG, backstack);
    }

    public void addComments(Node n)
    {
        addComments(n, android.R.id.tabcontent, false);
    }

    public void addMetadata(Node n)
    {
        int layoutid = android.R.id.tabcontent;
        BaseFragment frag = MetadataFragment.newInstance(n);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, layoutid, MetadataFragment.TAG, false);
    }

    public void addVersions(Document d, int layoutId, boolean backstack)
    {
        BaseFragment frag = VersionFragment.newInstance(d);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, layoutId, VersionFragment.TAG, backstack);
    }

    public void addVersions(Document d)
    {
        addVersions(d, android.R.id.tabcontent, false);
    }

    public void addTags(Node d, int layoutId, boolean backstack)
    {
        BaseFragment frag = TagsListNodeFragment.newInstance(d);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, layoutId, TagsListNodeFragment.TAG, backstack);
    }

    public void addTags(Node d)
    {
        addTags(d, android.R.id.tabcontent, false);
    }

    public void setDownloadDateTime(Date downloadDateTime)
    {
        this.downloadDateTime = downloadDateTime;
    }
}
