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
package org.alfresco.mobile.android.application.fragments.browser;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.OperationContentProvider;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.integration.utils.NodePlaceHolder;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;

public class ProgressNodeAdapter extends NodeAdapter implements LoaderManager.LoaderCallbacks<Cursor>,
        OnMenuItemClickListener
{
    private static final String TAG = ProgressNodeAdapter.class.getName();

    protected Node parentNode;

    private List<Node> selectedOptionItems = new ArrayList<Node>();

    public ProgressNodeAdapter(Activity context, int textViewResourceId, Node parentNode, List<Node> listItems,
            List<Node> selectedItems, int mode)
    {
        super(context, textViewResourceId, listItems, selectedItems, mode);
        this.parentNode = parentNode;
        context.getLoaderManager().restartLoader(context.hashCode(), null, this); 
    }

    public ProgressNodeAdapter(Activity context, int textViewResourceId, List<Node> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

    // /////////////////////////////////////////////////////////////
    // ITEM LINE
    // ////////////////////////////////////////////////////////////
    @Override
    protected void updateTopText(GenericViewHolder vh, Node item)
    {
        ProgressBar progressView = (ProgressBar) ((View) vh.topText.getParent()).findViewById(R.id.status_progress);

        if (item instanceof NodePlaceHolder)
        {
            vh.topText.setText(item.getName());
            vh.topText.setEnabled(false);
            long totalSize = ((NodePlaceHolder) item).getLength();
            progressView.setVisibility(View.VISIBLE);
            progressView.setIndeterminate(false);
            if (totalSize == -1)
            {
                progressView.setMax(100);
                progressView.setProgress(0);
            }
            else
            {
                long progress = ((NodePlaceHolder) item).getProgress();
                float value = (((float) progress / ((float) totalSize)) * 100);
                int percentage = Math.round(value);

                if (percentage == 100)
                {
                    progressView.setIndeterminate(true);
                }
                else
                {
                    progressView.setIndeterminate(false);
                    progressView.setMax(100);
                    progressView.setProgress(percentage);
                }
            }
        }
        else
        {
            progressView.setVisibility(View.GONE);
            super.updateTopText(vh, item);
        }
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Node item)
    {
        if (item instanceof NodePlaceHolder)
        {
            vh.bottomText.setEnabled(false);
            vh.bottomText.setVisibility(View.GONE);
        }
        else
        {
            vh.bottomText.setVisibility(View.VISIBLE);
            super.updateBottomText(vh, item);
        }
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Node item)
    {
        if (item instanceof NodePlaceHolder)
        {
            UIUtils.setBackground(((View) vh.icon), null);
            vh.icon.setImageResource(MimeTypeManager.getIcon(item.getName()));
        }
        else
        {
            super.updateIcon(vh, item);
        }

        if (item.isFolder())
        {
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mime_folder));

            if (mode == ListingModeFragment.MODE_IMPORT) { return; }

            UIUtils.setBackground(((View) vh.icon),
                    getContext().getResources().getDrawable(R.drawable.quickcontact_badge_overlay_light));

            vh.icon.setVisibility(View.VISIBLE);
            vh.icon.setTag(R.id.node_action, item);
            vh.icon.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Node item = (Node) v.getTag(R.id.node_action);
                    selectedOptionItems.add(item);
                    PopupMenu popup = new PopupMenu(getContext(), v);
                    getMenu(popup.getMenu(), item);

                    if (AndroidVersion.isICSOrAbove())
                    {
                        popup.setOnDismissListener(new OnDismissListener()
                        {
                            @Override
                            public void onDismiss(PopupMenu menu)
                            {
                                selectedOptionItems.clear();
                            }
                        });
                    }

                    popup.setOnMenuItemClickListener(ProgressNodeAdapter.this);

                    popup.show();
                }
            });
        }
        else
        {
            UIUtils.setBackground(((View) vh.icon), null);
        }
    }

    // /////////////////////////////////////////////////////////////
    // INLINE PROGRESS
    // ////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri baseUri = OperationContentProvider.CONTENT_URI;

        return new CursorLoader(getContext(), baseUri, OperationSchema.COLUMN_ALL, OperationSchema.COLUMN_PARENT_ID
                + "=\"" + parentNode.getIdentifier() + "\" AND " + OperationSchema.COLUMN_REQUEST_TYPE + " IN("
                + CreateDocumentRequest.TYPE_ID + ")", null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        Log.d(TAG, "Count : " + cursor.getCount());
        while (cursor.moveToNext())
        {
            int status = cursor.getInt(OperationSchema.COLUMN_STATUS_ID);
            String name = cursor.getString(OperationSchema.COLUMN_NOTIFICATION_TITLE_ID);

            switch (status)
            {
                case Operation.STATUS_PENDING:
                    // Add Node if not present
                    if (!hasNode(name))
                    {
                        replaceNode(new NodePlaceHolder(name));
                    }
                    break;
                case Operation.STATUS_RUNNING:
                    // Update node if not present
                    long progress = cursor.getLong(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID);
                    long totalSize = cursor.getLong(OperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                    replaceNode(new NodePlaceHolder(name, totalSize, progress));
                    break;
                default:
                    if (hasNode(name) && getNode(name) instanceof NodePlaceHolder)
                    {
                        remove(name);
                    }
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        // TODO Auto-generated method stub
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public void getMenu(Menu menu, Node node)
    {
        MenuItem mi;

        Permissions permission = SessionUtils.getSession(getContext()).getServiceRegistry().getDocumentFolderService()
                .getPermissions(node);

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_DETAILS, Menu.FIRST + MenuActionItem.MENU_DETAILS,
                R.string.action_view_properties);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (permission.canEdit())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT,
                    R.string.action_edit_properties);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (permission.canDelete())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE_FOLDER, Menu.FIRST + MenuActionItem.MENU_DELETE_FOLDER,
                    R.string.delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_DETAILS:
                onMenuItemClick = true;
                ((MainActivity) getContext()).addPropertiesFragment(selectedOptionItems.get(0));
                selectedItems.add(selectedOptionItems.get(0));
                notifyDataSetChanged();
                break;
            case MenuActionItem.MENU_EDIT:
                onMenuItemClick = true;
                NodeActions.edit((Activity) getContext(), (Folder) parentNode, selectedOptionItems.get(0));
                break;
            case MenuActionItem.MENU_DELETE_FOLDER:
                onMenuItemClick = true;
                Fragment fr = ((Activity) getContext()).getFragmentManager().findFragmentByTag(
                        ChildrenBrowserFragment.TAG);
                NodeActions.delete((Activity) getContext(), fr, selectedOptionItems.get(0));
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }
}
