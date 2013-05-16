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
package org.alfresco.mobile.android.application.fragments.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.DocumentImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.exception.AlfrescoAppException;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.browser.DownloadDialogFragment;
import org.alfresco.mobile.android.application.fragments.comments.CommentsFragment;
import org.alfresco.mobile.android.application.fragments.encryption.EncryptionDialogFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.tags.TagsListNodeFragment;
import org.alfresco.mobile.android.application.fragments.versions.VersionFragment;
import org.alfresco.mobile.android.application.integration.OperationManager;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.OperationRequestGroup;
import org.alfresco.mobile.android.application.integration.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.application.integration.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.security.CipherUtils;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
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

/**
 * Responsible to display details of a specific Node.
 * 
 * @author Jean Marie Pascal
 */
public class DetailsFragment extends MetadataFragment implements OnTabChangeListener,
        LoaderCallbacks<LoaderResult<Node>>
{

    private static final String ACTION_REFRESH = "org.alfresco.mobile.android.intent.ACTION_REFRESH";

    public static final String TAG = "DetailsFragment";

    private TabHost mTabHost;

    private static final String ARGUMENT_NODE_ID = "nodeIdentifier";

    private static final String TAB_SELECTED = "tabSelected";

    protected RenditionManager renditionManager;

    protected Date downloadDateTime;

    protected Integer tabSelected = null;

    protected Integer tabSelection = null;

    protected File tempFile = null;

    protected PreviewFragment replacementPreviewFragment = null;

    public DetailsFragment()
    {
    }

    private View vRoot;

    public static DetailsFragment newInstance(Node node, Folder parentNode)
    {
        DetailsFragment bf = new DetailsFragment();
        bf.setArguments(createBundleArgs(node, parentNode));
        return bf;
    }

    public static DetailsFragment newInstance(String nodeIdentifier)
    {
        DetailsFragment bf = new DetailsFragment();
        Bundle b = new Bundle();
        b.putString(ARGUMENT_NODE_ID, nodeIdentifier);
        bf.setArguments(b);
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
        vRoot = inflater.inflate(R.layout.app_details, container, false);

        node = (Node) getArguments().get(ARGUMENT_NODE);
        String nodeIdentifier = (String) getArguments().get(ARGUMENT_NODE_ID);
        parentNode = (Folder) getArguments().get(ARGUMENT_NODE_PARENT);
        if (node == null && nodeIdentifier == null) { return null; }

        // TAB
        if (savedInstanceState != null)
        {
            tabSelection = savedInstanceState.getInt(TAB_SELECTED);
            savedInstanceState.remove(TAB_SELECTED);
        }

        if (node != null)
        {
            display(node, inflater);
        }
        else if (nodeIdentifier != null)
        {
            getActivity().getLoaderManager().restartLoader(NodeLoader.ID, getArguments(), this);
        }
        return vRoot;
    }

    public void refresh()
    {
        vRoot.findViewById(R.id.properties_details).setVisibility(View.VISIBLE);
        vRoot.findViewById(R.id.progressbar).setVisibility(View.GONE);
        display(node);
        getActivity().invalidateOptionsMenu();
    }

    private void display(Node refreshedNode)
    {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        display(refreshedNode, inflater);
    }

    private void display(Node refreshedNode, LayoutInflater inflater)
    {
        node = refreshedNode;

        renditionManager = ApplicationManager.getInstance(getActivity()).getRenditionManager(getActivity());

        // Header
        TextView tv = (TextView) vRoot.findViewById(R.id.title);
        tv.setText(node.getName());
        tv = (TextView) vRoot.findViewById(R.id.details);
        tv.setText(Formatter.createContentBottomText(getActivity(), node, true));

        // Preview + Thumbnail
        displayIcon(node, R.drawable.mime_folder, (ImageView) vRoot.findViewById(R.id.icon), false);
        displayIcon(node, R.drawable.mime_256_folder, (ImageView) vRoot.findViewById(R.id.preview), true);

        // Description
        Integer generalPropertyTitle = null;
        tv = (TextView) vRoot.findViewById(R.id.description);
        List<String> filter = new ArrayList<String>();
        if (node.getDescription() != null && node.getDescription().length() > 0
                && vRoot.findViewById(R.id.description_group) != null)
        {
            vRoot.findViewById(R.id.description_group).setVisibility(View.VISIBLE);
            tv.setText(node.getDescription());
            generalPropertyTitle = -1;
            ((TextView) vRoot.findViewById(R.id.prop_name_value)).setText(node.getName());
            filter.add(ContentModel.PROP_NAME);
        }
        else if (vRoot.findViewById(R.id.description_group) != null)
        {
            vRoot.findViewById(R.id.description_group).setVisibility(View.GONE);
            generalPropertyTitle = R.string.metadata;
        }

        mTabHost = (TabHost) vRoot.findViewById(android.R.id.tabhost);
        setupTabs();

        if (mTabHost == null)
        {
            ViewGroup parent = (ViewGroup) vRoot.findViewById(R.id.metadata);
            ViewGroup generalGroup = createAspectPanel(inflater, parent, node, ContentModel.ASPECT_GENERAL, false,
                    generalPropertyTitle, filter);
            addPathProperty(generalGroup, inflater);
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_GEOGRAPHIC);
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_EXIF);
            createAspectPanel(inflater, parent, node, ContentModel.ASPECT_AUDIO);
        }

        // BUTTONS
        ImageView b = (ImageView) vRoot.findViewById(R.id.action_openin);
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

        b = (ImageView) vRoot.findViewById(R.id.action_geolocation);
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

        b = (ImageView) vRoot.findViewById(R.id.like);
        if (alfSession != null && alfSession.getRepositoryInfo() != null
                && alfSession.getRepositoryInfo().getCapabilities() != null
                && alfSession.getRepositoryInfo().getCapabilities().doesSupportLikingNodes())
        {
            IsLikedLoaderCallBack lcb = new IsLikedLoaderCallBack(alfSession, getActivity(), node);
            lcb.setImageButton(b);
            lcb.setProgressView(vRoot.findViewById(R.id.like_progress));
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
            if (vRoot.findViewById(R.id.like_progress) != null)
            {
                vRoot.findViewById(R.id.like_progress).setVisibility(View.GONE);
            }
        }

        // BUTTONS
        b = (ImageView) vRoot.findViewById(R.id.action_favorite);
        IsFavoriteLoaderCallBack lcb = new IsFavoriteLoaderCallBack(alfSession, getActivity(), node);
        lcb.setImageButton(b);
        lcb.setProgressView(vRoot.findViewById(R.id.favorite_progress));
        lcb.execute(false);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                favorite(v);
            }
        });

        b = (ImageView) vRoot.findViewById(R.id.action_share);
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
    }

    /**
     * Display a drawable associated to the node type on a specific imageview.
     * 
     * @param node
     * @param defaultIconId
     * @param iv
     * @param isLarge
     */
    private void displayIcon(Node node, int defaultIconId, ImageView iv, boolean isLarge)
    {
        if (iv == null) { return; }

        int iconId = defaultIconId;
        if (node.isDocument())
        {
            iconId = MimeTypeManager.getIcon(node.getName(), isLarge);
            if (((Document) node).isLatestVersion())
            {
                if (isLarge)
                {
                    renditionManager.preview(iv, node, iconId, DisplayUtils.getWidth(getActivity()));
                }
                else
                {
                    renditionManager.display(iv, node, iconId);
                }
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
            iv.setImageResource(defaultIconId);
        }
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
    public void onStop()
    {
        getActivity().invalidateOptionsMenu();
        ((MainActivity) getActivity()).setCurrentNode(null);
        super.onStop();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_UPDATE_COMPLETE);
        intentFilter.addAction(IntentIntegrator.ACTION_DELETE_COMPLETE);
        intentFilter.addAction(IntentIntegrator.ACTION_LIKE_COMPLETE);
        intentFilter.addAction(IntentIntegrator.ACTION_FAVORITE_COMPLETE);
        intentFilter.addAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
        intentFilter.addAction(ACTION_REFRESH);
        receiver = new UpdateReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause()
    {
        if (receiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
        super.onPause();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void share()
    {
        if (node.isFolder()) { return; }

        if (alfSession instanceof RepositorySession)
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
                    if (parentNode != null)
                    {
                        String path = parentNode.getPropertyValue(PropertyIds.PATH);
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
                                String fullPath = String.format(getString(R.string.cloud_share_url),
                                        ((CloudSession) alfSession).getNetwork().getIdentifier(), siteName, nodeID);
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

        if (CipherUtils.isEncryptionActive(getActivity()))
        {
            tempFile = IOUtils.makeTempFile(NodeActions.getDownloadFile(getActivity(), node));
            if (replacementPreviewFragment != null)
            {
                replacementPreviewFragment.setTempFile(tempFile);
            }
            b.putString(DownloadDialogFragment.ARGUMENT_TEMPFILE, tempFile.getPath());
        }

        b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
        b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_OPEN);
        DialogFragment frag = new DownloadDialogFragment();
        frag.setArguments(b);
        frag.show(getFragmentManager(), DownloadDialogFragment.TAG);
    }

    public void download()
    {
        if (node instanceof Document)
        {
            NodeActions.download(getActivity(), (Document) node);
        }
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
                if (replacementPreviewFragment != null)
                {
                    tempFile = replacementPreviewFragment.getTempFile();
                }
                final File dlFile = (tempFile != null ? tempFile : NodeActions.getDownloadFile(getActivity(), node));

                long datetime = dlFile.lastModified();
                Date d = new Date(datetime);

                boolean modified = (d != null && downloadDateTime != null) ? d.after(downloadDateTime) : false;

                if (modified
                        && alfSession.getServiceRegistry().getDocumentFolderService().getPermissions(node).canEdit())
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.save_back);
                    builder.setMessage(String.format(getResources().getString(R.string.save_back_description),
                            node.getName()));
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            update(dlFile);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            if (StorageManager.shouldEncryptDecrypt(getActivity(), dlFile.getPath()))
                            {
                                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                                        .beginTransaction();
                                EncryptionDialogFragment fragment = EncryptionDialogFragment.encrypt(dlFile.getPath());
                                fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
                                fragmentTransaction.commit();
                            }

                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else
                {
                    if (StorageManager.shouldEncryptDecrypt(getActivity(), dlFile.getPath()))
                    {
                        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                        EncryptionDialogFragment fragment = EncryptionDialogFragment.encrypt(dlFile.getPath());
                        fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
                        fragmentTransaction.commit();
                    }
                }
                break;
            case PublicIntent.REQUESTCODE_FILEPICKER:
                if (data != null && data.getData() != null)
                {
                    String tmpPath = ActionManager.getPath(getActivity(), data.getData());
                    if (tmpPath != null)
                    {
                        File f = new File(tmpPath);
                        update(f);
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

    public void update(File f)
    {
        OperationRequestGroup group = new OperationRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new UpdateContentRequest(parentNode, (Document) node, new ContentFileProgressImpl(f)));
        OperationManager.getInstance(getActivity()).enqueue(group);
    }

    public void delete()
    {
        NodeActions.delete(getActivity(), this, node);
    }

    public void edit()
    {
        NodeActions.edit(getActivity(), parentNode, node);
    }

    public void like(View v)
    {
        vRoot.findViewById(R.id.like_progress).setVisibility(View.VISIBLE);
        OperationRequestGroup group = new OperationRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new LikeNodeRequest(parentNode, node)
                .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
        OperationManager.getInstance(getActivity()).enqueue(group);
    }

    public void favorite(View v)
    {
        IsFavoriteLoaderCallBack lcb = new IsFavoriteLoaderCallBack(alfSession, getActivity(), node);
        lcb.setImageButton((ImageView) v.findViewById(R.id.action_favorite));
        lcb.setProgressView(((ViewGroup) v.getParent()).findViewById(R.id.favorite_progress));
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

    public static void getMenu(AlfrescoSession session, Activity activity, Menu menu, Node node, boolean actionMode)
    {
        MenuItem mi;

        if (node == null) { return; }

        if (node.isDocument())
        {
            if (((Document) node).getContentStreamLength() > 0)
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_DOWNLOAD, Menu.FIRST + MenuActionItem.MENU_DOWNLOAD,
                        R.string.download);
                mi.setIcon(R.drawable.ic_download_dark);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (((Document) node).isLatestVersion()
                    && ((DocumentImpl) node).hasAllowableAction(Action.CAN_SET_CONTENT_STREAM.value()))
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_UPDATE, Menu.FIRST + MenuActionItem.MENU_UPDATE,
                        R.string.update);
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
        getMenu(alfSession, getActivity(), menu, node, false);
    }

    public Node getCurrentNode()
    {
        return node;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<Node>> onCreateLoader(final int id, Bundle args)
    {
        vRoot.findViewById(R.id.properties_details).setVisibility(View.GONE);
        vRoot.findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

        return new NodeLoader(getActivity(), alfSession, args.getString(ARGUMENT_NODE_ID));
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Node>> loader, LoaderResult<Node> results)
    {
        if (results.hasException())
        {
            vRoot.findViewById(R.id.progressbar).setVisibility(View.GONE);
            vRoot.findViewById(R.id.empty).setVisibility(View.VISIBLE);
            ((TextView) vRoot.findViewById(R.id.empty_text)).setText(R.string.empty_child);
        }
        else if (loader instanceof NodeLoader && getActivity() != null)
        {
            node = results.getData();
            parentNode = ((NodeLoader) loader).getParentFolder();
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ACTION_REFRESH));
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Node>> arg0)
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TAB MENU
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (tabSelected != null)
        {
            outState.putInt(TAB_SELECTED, tabSelected);
        }
    }

    private static final String TAB_PREVIEW = "Preview";

    private static final String TAB_METADATA = "Metadata";

    private static final String TAB_COMMENTS = "Comments";

    private static final String TAB_HISTORY = "History";

    private static final String TAB_TAGS = "Tags";

    private void setupTabs()
    {
        if (mTabHost == null) { return; }

        mTabHost.setup(); // you must call this before adding your tabs!
        mTabHost.setOnTabChangedListener(this);

        if (node.isDocument() && ((Document) node).isLatestVersion())
        {
            mTabHost.addTab(newTab(TAB_PREVIEW, R.string.preview, android.R.id.tabcontent));
        }
        mTabHost.addTab(newTab(TAB_METADATA, R.string.metadata, android.R.id.tabcontent));
        if (node.isDocument())
        {
            mTabHost.addTab(newTab(TAB_HISTORY, R.string.action_version, android.R.id.tabcontent));
        }
        mTabHost.addTab(newTab(TAB_COMMENTS, R.string.comments, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_TAGS, R.string.tags, android.R.id.tabcontent));

        if (tabSelection != null)
        {
            if (tabSelection == 0) { return; }
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
            tabSelected = 1;
            addMetadata(node);
        }
        else if (TAB_COMMENTS.equals(tabId))
        {
            tabSelected = 3;
            addComments(node);
        }
        else if (TAB_HISTORY.equals(tabId) && node.isDocument())
        {
            tabSelected = 2;
            addVersions((Document) node);
        }
        else if (TAB_TAGS.equals(tabId))
        {
            tabSelected = 4;
            addTags(node);
        }
        else if (TAB_PREVIEW.equals(tabId))
        {
            tabSelected = 0;
            addPreview(node);
        }
    }

    public void addPreview(Node n)
    {
        addPreview(n, android.R.id.tabcontent, false);
    }

    public void addPreview(Node n, int layoutId, boolean backstack)
    {
        replacementPreviewFragment = PreviewFragment.newInstance(n);
        replacementPreviewFragment.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), replacementPreviewFragment, layoutId, PreviewFragment.TAG,
                backstack);
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
        BaseFragment frag = MetadataFragment.newInstance(n, parentNode);
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

    private UpdateReceiver receiver;

    public class UpdateReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (getActivity() == null) { return; }

            if (intent.getAction().equals(ACTION_REFRESH))
            {
                refresh();
                return;
            }

            if (intent.getExtras() != null)
            {
                DetailsFragment detailsFragment = (DetailsFragment) getFragmentManager().findFragmentByTag(
                        DetailsFragment.TAG);
                if (detailsFragment != null && getActivity() instanceof MainActivity)
                {
                    Node n = (Node) detailsFragment.getArguments().get(DetailsFragment.ARGUMENT_NODE);
                    Bundle b = intent.getExtras().getParcelable(IntentIntegrator.EXTRA_DATA);
                    if (b == null) { return; }

                    Node node = null;
                    if (b.containsKey(IntentIntegrator.EXTRA_DOCUMENT))
                    {
                        node = (Node) b.getParcelable(IntentIntegrator.EXTRA_DOCUMENT);
                    }
                    else if (b.containsKey(IntentIntegrator.EXTRA_NODE))
                    {
                        node = (Node) b.getParcelable(IntentIntegrator.EXTRA_NODE);
                    }
                    if (n != null
                            && node != null
                            && NodeRefUtils.getCleanIdentifier(n.getIdentifier()).equals(
                                    NodeRefUtils.getCleanIdentifier(node.getIdentifier())))
                    {
                        if (intent.getAction().equals(IntentIntegrator.ACTION_DELETE_COMPLETE))
                        {
                            ((MainActivity) getActivity()).setCurrentNode(null);
                            if (DisplayUtils.hasCentralPane(getActivity()))
                            {
                                FragmentDisplayer.removeFragment(getActivity(), DetailsFragment.TAG);
                            }
                            else
                            {
                                getFragmentManager().popBackStack(DetailsFragment.TAG,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                            return;
                        }

                        ((MainActivity) getActivity()).setCurrentNode(node);

                        if (intent.getAction().equals(IntentIntegrator.ACTION_LIKE_COMPLETE))
                        {
                            View progressView = vRoot.findViewById(R.id.like_progress);
                            ImageView imageView = (ImageView) vRoot.findViewById(R.id.like);
                            if (progressView != null)
                            {
                                progressView.setVisibility(View.GONE);
                            }
                            Boolean isLiked = (b.getString(IntentIntegrator.EXTRA_LIKE) != null) ? Boolean
                                    .parseBoolean(b.getString(IntentIntegrator.EXTRA_LIKE)) : null;
                            if (isLiked != null)
                            {
                                int drawable = isLiked ? R.drawable.ic_like : R.drawable.ic_unlike;
                                imageView.setImageDrawable(context.getResources().getDrawable(drawable));
                            }
                            return;
                        }

                        if (intent.getAction().equals(IntentIntegrator.ACTION_FAVORITE_COMPLETE))
                        {
                            View progressView = vRoot.findViewById(R.id.favorite_progress);
                            ImageView imageView = (ImageView) vRoot.findViewById(R.id.action_favorite);
                            if (progressView != null)
                            {
                                progressView.setVisibility(View.GONE);
                            }
                            Boolean isFavorite = (b.getString(IntentIntegrator.EXTRA_FAVORITE) != null) ? Boolean
                                    .parseBoolean(b.getString(IntentIntegrator.EXTRA_FAVORITE)) : null;
                            if (isFavorite != null)
                            {
                                int drawable = isFavorite ? R.drawable.ic_favorite_dark : R.drawable.ic_unfavorite_dark;
                                imageView.setImageDrawable(context.getResources().getDrawable(drawable));
                            }
                            return;
                        }

                        if (intent.getAction().equals(IntentIntegrator.ACTION_UPDATE_COMPLETE))
                        {
                            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);

                            Node updatedNode = (Node) b.getParcelable(IntentIntegrator.EXTRA_UPDATED_DOCUMENT);

                            ApplicationManager.getInstance(getActivity()).getRenditionManager(getActivity())
                                    .removeFromCache(node.getIdentifier());
                            Boolean backstack = false;
                            if (!DisplayUtils.hasCentralPane(getActivity()))
                            {
                                backstack = true;
                                getFragmentManager().popBackStack(DetailsFragment.TAG,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                            else
                            {
                                ((ChildrenBrowserFragment) getFragmentManager().findFragmentByTag(
                                        ChildrenBrowserFragment.TAG)).select(updatedNode);
                            }
                            Folder pFolder = (Folder) b.getParcelable(IntentIntegrator.EXTRA_FOLDER);

                            ((MainActivity) getActivity()).addPropertiesFragment(updatedNode, pFolder, backstack);

                            MessengerManager.showToast(getActivity(),
                                    String.format(getResources().getString(R.string.update_sucess), node.getName()));

                            return;
                        }

                        if (intent.getAction().equals(IntentIntegrator.ACTION_UPDATE_COMPLETED))
                        {
                            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);

                            Node updatedNode = (Node) b.getParcelable(IntentIntegrator.EXTRA_NODE);

                            Boolean backstack = false;
                            if (!DisplayUtils.hasCentralPane(getActivity()))
                            {
                                backstack = true;
                                getFragmentManager().popBackStack(DetailsFragment.TAG,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                            else
                            {
                                ((ChildrenBrowserFragment) getFragmentManager().findFragmentByTag(
                                        ChildrenBrowserFragment.TAG)).select(updatedNode);
                            }
                            Folder pFolder = (Folder) b.getParcelable(IntentIntegrator.EXTRA_FOLDER);

                            ((MainActivity) getActivity()).addPropertiesFragment(updatedNode, pFolder, backstack);

                            MessengerManager.showToast(getActivity(),
                                    String.format(getResources().getString(R.string.update_sucess), node.getName()));

                            return;
                        }
                    }
                }
            }
        }
    }
}
