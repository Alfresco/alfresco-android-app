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
package org.alfresco.mobile.android.application.fragments.browser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.utils.NodeComparator;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

/**
 * Provides access to node (documents or folders) and displays them as a view
 * based on GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class NodeAdapter extends BaseListAdapter<Node, GenericViewHolder> implements OnMenuItemClickListener
{
    protected List<Node> originalNodes;

    protected List<Node> selectedItems;

    protected HashMap<String, Node> nodeNameIndexer = new HashMap<String, Node>();

    private Boolean activateThumbnail = Boolean.FALSE;

    private RenditionManager renditionManager;

    private int mode;

    private List<Node> selectedOptionItems = new ArrayList<Node>();

    public NodeAdapter(Activity context, int textViewResourceId, List<Node> listItems, List<Node> selectedItems,
            int mode)
    {
        super(context, textViewResourceId, listItems);
        originalNodes = listItems;
        this.selectedItems = selectedItems;
        this.renditionManager = ApplicationManager.getInstance(context).getRenditionManager(context);
        this.mode = mode;
    }

    public NodeAdapter(Activity context, int textViewResourceId, List<Node> listItems)
    {
        super(context, textViewResourceId, listItems);
        this.renditionManager = ApplicationManager.getInstance(context).getRenditionManager(context);
    }

    // /////////////////////////////////////////////////////////////
    // CRUD LIST
    // ////////////////////////////////////////////////////////////
    @Override
    public void clear()
    {
        nodeNameIndexer.clear();
        super.clear();
    }

    @Override
    public void add(Node node)
    {
        if (node != null)
        {
            nodeNameIndexer.put(node.getName(), node);
        }
        super.add(node);
    }

    @Override
    public void addAll(Collection<? extends Node> collection)
    {
        Node objects[] = (Node[]) collection.toArray(new Node[0]);

        int size = objects.length;
        for (int i = 0; i < size; i++)
        {
            add(objects[i]);
        }
    }

    public synchronized void replaceNode(Node node)
    {
        if (nodeNameIndexer.containsKey(node.getName()))
        {
            int position = getPosition(nodeNameIndexer.get(node.getName()));
            originalNodes.remove(position);
            originalNodes.add(node);
        }
        else
        {
            originalNodes.add(node);
        }

        originalNodes.removeAll(Collections.singleton(null));
        Collections.sort(originalNodes, new NodeComparator(true, DocumentFolderService.SORT_PROPERTY_NAME));

        List<Node> tmpNodes = new ArrayList<Node>(originalNodes);
        clear();
        addAll(tmpNodes);
    }

    public synchronized void remove(String nodeName)
    {
        if (nodeNameIndexer.containsKey(nodeName))
        {
            int position = getPosition(nodeNameIndexer.get(nodeName));
            originalNodes.remove(position);
            List<Node> tmpNodes = new ArrayList<Node>(originalNodes);
            clear();
            addAll(tmpNodes);
        }
    }

    public List<Node> getNodes()
    {
        return originalNodes;
    }

    public boolean hasNode(String indexValue)
    {
        return nodeNameIndexer.containsKey(indexValue);
    }

    public Node getNode(String indexValue)
    {
        int position = getPosition(nodeNameIndexer.get(indexValue));
        return originalNodes.get(position);
    }

    // /////////////////////////////////////////////////////////////
    // ITEM LINE
    // ////////////////////////////////////////////////////////////

    @Override
    protected void updateTopText(GenericViewHolder vh, Node item)
    {
        vh.topText.setText(item.getName());
        if (item.isDocument() && mode == ChildrenBrowserFragment.MODE_IMPORT)
        {
            vh.topText.setEnabled(false);
        }
        else
        {
            vh.topText.setEnabled(true);
        }
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Node item)
    {
        vh.bottomText.setText(createContentBottomText(getContext(), item));
        if (selectedItems != null && selectedItems.contains(item))
        {
            UIUtils.setBackground(((LinearLayout) vh.choose.getParent()),
                    getContext().getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((LinearLayout) vh.choose.getParent()), null);
        }
        if (item.isDocument() && mode == ChildrenBrowserFragment.MODE_IMPORT)
        {
            // Disable document : grey font color instead of black
            vh.bottomText.setEnabled(false);
        }
        else
        {
            vh.bottomText.setEnabled(true);
        }
    }

    private String createContentBottomText(Context context, Node node)
    {
        String s = "";

        if (node.getCreatedAt() != null)
        {
            s = formatDate(context, node.getCreatedAt().getTime());
            if (node.isDocument())
            {
                Document doc = (Document) node;
                s += " - " + Formatter.formatFileSize(context, doc.getContentStreamLength());
            }
        }
        return s;
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Node item)
    {
        if (item.isDocument())
        {
            if (!activateThumbnail)
            {
                vh.icon.setImageResource(MimeTypeManager.getIcon(item.getName()));
            }
            else
            {
                renditionManager.display(vh.icon, item, MimeTypeManager.getIcon(item.getName()));
            }
        }
        else if (item.isFolder())
        {
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mime_folder));

            if (mode == ChildrenBrowserFragment.MODE_IMPORT) { return; }

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

                    popup.setOnMenuItemClickListener(NodeAdapter.this);

                    popup.show();
                }
            });
        }
    }

    public Boolean hasActivateThumbnail()
    {
        return activateThumbnail;
    }

    public void setActivateThumbnail(Boolean activateThumbnail)
    {
        this.activateThumbnail = activateThumbnail;
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
                break;
            case MenuActionItem.MENU_DELETE_FOLDER:
                onMenuItemClick = true;
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }
}
