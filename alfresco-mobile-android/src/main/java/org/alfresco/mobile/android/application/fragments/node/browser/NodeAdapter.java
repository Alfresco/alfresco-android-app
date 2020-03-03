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

import java.lang.ref.WeakReference;
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
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.GridAdapterHelper;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskPickerFragment;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.platform.mimetype.MimeType;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesProgressViewHolder;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Provides access to node (documents or folders) and displays them as a view
 * based on GenericViewHolder.
 *
 * @author Jean Marie Pascal
 */
public class NodeAdapter extends BaseListAdapter<Node, TwoLinesProgressViewHolder>
{
    protected List<Node> originalNodes;

    protected List<Node> selectedItems;

    protected Map<String, Node> nodeNameIndexer = Collections.synchronizedMap(new LinkedHashMap<String, Node>());

    private Boolean activateThumbnail = Boolean.FALSE;

    private RenditionManagerImpl renditionManager;

    protected int mode;

    private boolean isEditable;

    protected WeakReference<Fragment> fragmentRef;

    private BaseGridFragment gridFragment;

    protected Map<String, Node> selectedMapItems;

    protected WeakReference<FragmentActivity> activityRef;

    protected boolean fromFavorites = false;

    protected List<String> favoritesNodeIndex;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NodeAdapter(FragmentActivity activity, int textViewResourceId, List<Node> listItems)
    {
        super(activity, textViewResourceId, listItems);
        this.renditionManager = RenditionManagerImpl.getInstance(activity);
        this.vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
        this.activityRef = new WeakReference<>(activity);
    }

    public NodeAdapter(Fragment fr, int textViewResourceId, List<Node> listItems, List<Node> selectedItems, int mode)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.fragmentRef = new WeakReference<>(fr);
        originalNodes = Collections.synchronizedList(listItems);
        this.selectedItems = selectedItems;
        this.renditionManager = RenditionManagerImpl.getInstance(fr.getActivity());
        this.mode = mode;
        this.vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
        this.activityRef = new WeakReference<>(fr.getActivity());
        this.gridFragment = (BaseGridFragment) fr;
    }

    public NodeAdapter(FragmentActivity activity, int textViewResourceId, List<Node> listItems,
            List<Node> selectedItems, int mode)
    {
        super(activity, textViewResourceId, listItems);
        originalNodes = Collections.synchronizedList(listItems);
        this.selectedItems = selectedItems;
        this.renditionManager = RenditionManagerImpl.getInstance(activity);
        this.mode = mode;
        this.vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
        this.activityRef = new WeakReference<>(activity);
        this.fromFavorites = true;
    }

    public NodeAdapter(Fragment fr, int textViewResourceId, List<Node> listItems, boolean isEditable)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.fragmentRef = new WeakReference<>(fr);
        this.renditionManager = RenditionManagerImpl.getInstance(fr.getActivity());
        this.vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
        this.isEditable = isEditable;
        this.activityRef = new WeakReference<>(fr.getActivity());
    }

    public NodeAdapter(FragmentActivity activity, int textViewResourceId, List<Node> listItems,
            Map<String, Node> selectedItems)
    {
        super(activity, textViewResourceId, listItems);
        originalNodes = listItems;
        this.selectedMapItems = selectedItems;
        this.renditionManager = RenditionManagerImpl.getInstance(activity);
        this.mode = ListingModeFragment.MODE_PICK;
        this.vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
        this.activityRef = new WeakReference<>(activity);
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
        int[] layouts = GridAdapterHelper.getGridLayoutId(getContext(), gridFragment);

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

        TwoLinesProgressViewHolder vh = (TwoLinesProgressViewHolder) v.getTag();
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
        // Log.d("NodeAdapter", size + "");
    }

    public synchronized void replaceNode(Node node)
    {
        if (nodeNameIndexer.containsKey(node.getName()))
        {
            originalNodes.remove(getPosition(nodeNameIndexer.get(node.getName())));
        }
        originalNodes.add(node);
        originalNodes.removeAll(Collections.singleton(null));
        if (!originalNodes.isEmpty())
        {
            Collections.sort(originalNodes, new NodeComparator(true, DocumentFolderService.SORT_PROPERTY_NAME));
        }

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
    protected void updateTopText(TwoLinesProgressViewHolder vh, Node item)
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

    private ViewGroup getSelectionLayout(TwoLinesProgressViewHolder vh)
    {
        if (vh.icon.getParent() instanceof RelativeLayout)
        {
            return (RelativeLayout) vh.icon.getParent();
        }
        else
        {
            return (LinearLayout) vh.icon.getParent().getParent();
        }

    }

    @Override
    protected void updateBottomText(TwoLinesProgressViewHolder vh, Node item)
    {
        if (favoritesNodeIndex == null)
        {
            if (fromFavorites)
            {
                vh.favoriteIcon.setVisibility(View.VISIBLE);
                vh.favoriteIcon.setImageResource(R.drawable.ic_favorite_light);
            }
            else
            {
                vh.favoriteIcon.setVisibility(View.GONE);
            }
        }

        vh.bottomText.setText(createContentBottomText(getContext(), item));
        AccessibilityUtils.addContentDescription(vh.bottomText,
                createContentDescriptionBottomText(getActivity(), item));

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

        if (node.getModifiedAt() != null)
        {
            s = formatDate(context, node.getModifiedAt().getTime());
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

        if (node.getModifiedAt() != null)
        {
            s.append(context.getString(R.string.metadata_modified));
            s.append(formatDate(context, node.getModifiedAt().getTime()));
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
    protected void updateIcon(TwoLinesProgressViewHolder vh, final Node item)
    {
        if (isEditable)
        {
            vh.choose.setVisibility(View.VISIBLE);
            vh.choose.setScaleType(ScaleType.CENTER_INSIDE);
            int d_16 = DisplayUtils.getPixels(getContext(), R.dimen.d_16);
            vh.choose.setPadding(d_16, d_16, d_16, d_16);
            vh.choose.setImageResource(R.drawable.ic_cancel);
            vh.choose.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    remove(item);
                    notifyDataSetChanged();
                    if (getFragment() instanceof CreateTaskPickerFragment && item instanceof Document)
                    {
                        ((CreateTaskPickerFragment) getFragment()).removeDocument((Document) item);
                    }
                }
            });
        }

        if (item.isDocument())
        {
            MimeType mime = MimeTypeManager.getInstance(getActivity()).getMimetype(item.getName());
            if (!activateThumbnail)
            {
                vh.icon.setImageResource(mime != null ? mime.getLargeIconId(getActivity())
                        : MimeTypeManager.getInstance(getActivity()).getIcon(item.getName(), true));
            }
            else
            {
                RenditionManager.with(getActivity()).loadNode(item)
                        .placeHolder(mime != null ? mime.getLargeIconId(getActivity())
                                : MimeTypeManager.getInstance(getActivity()).getIcon(item.getName(), true))
                        .into(vh.icon);
            }
            vh.choose.setVisibility(View.GONE);
            AccessibilityUtils.addContentDescription(vh.icon,
                    mime != null ? mime.getDescription() : ((Document) item).getContentStreamMimeType());
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
            AccessibilityUtils.addContentDescription(vh.icon, R.string.mime_folder);
        }
    }

    // /////////////////////////////////////////////////////////////
    // UTILITIES
    // ////////////////////////////////////////////////////////////
    public void setContext(FragmentActivity activity)
    {
        this.activityRef = new WeakReference<>(activity);
    }

    public Boolean hasActivateThumbnail()
    {
        return activateThumbnail;
    }

    public void setActivateThumbnail(Boolean activateThumbnail)
    {
        this.activateThumbnail = activateThumbnail;
    }

    protected FragmentActivity getActivity()
    {
        return activityRef.get();
    }

    protected Fragment getFragment()
    {
        return fragmentRef.get();
    }

}
