/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.utils.NodeComparator;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.BaseCursorGridAdapterHelper;
import org.alfresco.mobile.android.application.fragments.BaseGridFragment;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskPickerFragment;
import org.alfresco.mobile.android.application.manager.AccessibilityHelper;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.mimetype.MimeType;
import org.alfresco.mobile.android.application.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.application.utils.ProgressViewHolder;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

/**
 * Provides access to node (documents or folders) and displays them as a view
 * based on GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class NodeAdapter extends BaseListAdapter<Node, ProgressViewHolder>
{
    protected List<Node> originalNodes;

    protected List<Node> selectedItems;

    protected LinkedHashMap<String, Node> nodeNameIndexer = new LinkedHashMap<String, Node>();

    private Boolean activateThumbnail = Boolean.FALSE;

    private RenditionManager renditionManager;

    protected int mode;

    private boolean isEditable;

    private Fragment fragment;

    private BaseGridFragment gridFragment;

    private Map<String, Document> selectedMapItems;

    protected Activity context;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NodeAdapter(Activity context, int textViewResourceId, List<Node> listItems, List<Node> selectedItems,
            int mode)
    {
        super(context, textViewResourceId, listItems);
        originalNodes = listItems;
        this.selectedItems = selectedItems;
        this.renditionManager = ApplicationManager.getInstance(context).getRenditionManager(context);
        this.mode = mode;
        this.vhClassName = ProgressViewHolder.class.getCanonicalName();
        this.context = context;
    }

    public NodeAdapter(BaseGridFragment fr, int textViewResourceId, List<Node> listItems, List<Node> selectedItems,
            int mode)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        originalNodes = listItems;
        this.selectedItems = selectedItems;
        this.renditionManager = ApplicationManager.getInstance(fr.getActivity()).getRenditionManager(fr.getActivity());
        this.mode = mode;
        this.vhClassName = ProgressViewHolder.class.getCanonicalName();
        this.context = fr.getActivity();
        this.gridFragment = fr;
    }

    public NodeAdapter(Activity context, int textViewResourceId, List<Node> listItems)
    {
        super(context, textViewResourceId, listItems);
        this.renditionManager = ApplicationManager.getInstance(context).getRenditionManager(context);
        this.vhClassName = ProgressViewHolder.class.getCanonicalName();
        this.context = context;
    }

    public NodeAdapter(Fragment fr, int textViewResourceId, List<Node> listItems, boolean isEditable)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.fragment = fr;
        this.renditionManager = ApplicationManager.getInstance(fr.getActivity()).getRenditionManager(fr.getActivity());
        this.vhClassName = ProgressViewHolder.class.getCanonicalName();
        this.isEditable = isEditable;
        this.context = fr.getActivity();
    }

    public NodeAdapter(Activity context, int textViewResourceId, List<Node> listItems,
            Map<String, Document> selectedItems)
    {
        super(context, textViewResourceId, listItems);
        originalNodes = listItems;
        this.selectedMapItems = selectedItems;
        this.renditionManager = ApplicationManager.getInstance(context).getRenditionManager(context);
        this.mode = ListingModeFragment.MODE_PICK;
        this.vhClassName = ProgressViewHolder.class.getCanonicalName();
        this.context = context;
    }

    // /////////////////////////////////////////////////////////////
    // VIEWS
    // ////////////////////////////////////////////////////////////
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Specific part for dynaminc resize
        int[] layouts = BaseCursorGridAdapterHelper.getGridLayoutId(getContext(), gridFragment);

        // First init ==> always

        View v = convertView;
        if (convertView == null || convertView.findViewById(layouts[1]) == null)
        {
            v = createView(getContext(), convertView, layouts[0]);
        }
        else
        {
            return super.getView(position, convertView, parent);
        }

        ProgressViewHolder vh = (ProgressViewHolder) v.getTag();
        Node item = getItem(position);
        updateControls(vh, item);
        return v;
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
        Log.d("NodeAdapter", size + "");
    }

    public synchronized void replaceNode(Node node)
    {
        if (nodeNameIndexer.containsKey(node.getName()))
        {
            originalNodes.remove(getPosition(nodeNameIndexer.get(node.getName())));
        }
        originalNodes.add(node);
        originalNodes.removeAll(Collections.singleton(null));
        Collections.sort(originalNodes, new NodeComparator(true, DocumentFolderService.SORT_PROPERTY_NAME));

        List<Node> tmpNodes = new ArrayList<Node>(originalNodes);
        clear();
        addAll(tmpNodes);
        notifyDataSetChanged();
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
            notifyDataSetChanged();
        }
    }

    public List<Node> getNodes()
    {
        return originalNodes;
    }

    public synchronized boolean hasNode(String indexValue)
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
    protected void updateTopText(ProgressViewHolder vh, Node item)
    {
        vh.topText.setText(item.getName());
        if (item.isDocument() && mode == ListingModeFragment.MODE_IMPORT)
        {
            vh.topText.setEnabled(false);
        }
        else
        {
            vh.topText.setEnabled(true);
        }
    }

    private LinearLayout getSelectionLayout(ProgressViewHolder vh)
    {
        return (LinearLayout) vh.icon.getParent();
    }

    @Override
    protected void updateBottomText(ProgressViewHolder vh, Node item)
    {
        vh.bottomText.setText(createContentBottomText(getContext(), item));
        AccessibilityHelper.addContentDescription(vh.bottomText, createContentDescriptionBottomText(context, item));

        if (mode == ListingModeFragment.MODE_PICK)
        {
            if (selectedMapItems.containsKey(item.getIdentifier()))
            {
                UIUtils.setBackground(getSelectionLayout(vh),
                        getContext().getResources().getDrawable(R.drawable.list_longpressed_holo));
            }
            else
            {
                UIUtils.setBackground(getSelectionLayout(vh), null);
            }
        }
        else
        {
            if (selectedItems != null && selectedItems.contains(item))
            {
                UIUtils.setBackground(getSelectionLayout(vh),
                        getContext().getResources().getDrawable(R.drawable.list_longpressed_holo));
            }
            else
            {
                UIUtils.setBackground(getSelectionLayout(vh), null);
            }
        }

        if (item.isDocument() && mode == ListingModeFragment.MODE_IMPORT)
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

    private String createContentDescriptionBottomText(Context context, Node node)
    {
        StringBuilder s = new StringBuilder();

        if (node.getCreatedAt() != null)
        {
            s.append(context.getString(R.string.metadata_modified));
            s.append(formatDate(context, node.getCreatedAt().getTime()));
            if (node.isDocument())
            {
                Document doc = (Document) node;
                s.append(" - ");
                s.append(context.getString(R.string.metadata_size));
                s.append(Formatter.formatFileSize(context, doc.getContentStreamLength()));
            }
        }
        return s.toString();
    }

    @Override
    protected void updateIcon(ProgressViewHolder vh, final Node item)
    {
        if (isEditable)
        {
            vh.choose.setVisibility(View.VISIBLE);
            vh.choose.setScaleType(ScaleType.CENTER_INSIDE);
            vh.choose.setImageResource(R.drawable.ic_cancel);
            vh.choose.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    remove(item);
                    notifyDataSetChanged();
                    if (fragment instanceof CreateTaskPickerFragment && item instanceof Document)
                    {
                        ((CreateTaskPickerFragment) fragment).removeDocument((Document) item);
                    }
                }
            });
        }

        if (item.isDocument())
        {
            MimeType mime = MimeTypeManager.getMimetype(context, item.getName());
            if (!activateThumbnail)
            {
                vh.icon.setImageResource(mime != null ? mime.getLargeIconId(context) : MimeTypeManager.getIcon(context,
                        item.getName(), true));
            }
            else
            {
                renditionManager.display(
                        vh.icon,
                        item,
                        mime != null ? mime.getLargeIconId(context) : MimeTypeManager.getIcon(context, item.getName(),
                                true));
            }
            vh.choose.setVisibility(View.GONE);
            AccessibilityHelper.addContentDescription(vh.icon, mime != null ? mime.getDescription() : ((Document) item)
                    .getContentStreamMimeType());
        }
        else if (item.isFolder())
        {
            if (isEditable)
            {
                vh.choose.setVisibility(View.VISIBLE);
            }
            else
            {
                vh.choose.setVisibility(View.GONE);
            }
            vh.icon.setImageResource(R.drawable.mime_256_folder);
            AccessibilityHelper.addContentDescription(vh.icon, R.string.mime_folder);
        }
    }

    // /////////////////////////////////////////////////////////////
    // UTILITIES
    // ////////////////////////////////////////////////////////////
    public void setContext(Activity activity)
    {
        this.context = activity;
    }

    public Boolean hasActivateThumbnail()
    {
        return activateThumbnail;
    }

    public void setActivateThumbnail(Boolean activateThumbnail)
    {
        this.activateThumbnail = activateThumbnail;
    }
}
