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

import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;

/**
 * Provides access to node (documents or folders) and displays them as a view
 * based on GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class NodeAdapter  extends BaseListAdapter<Node, GenericViewHolder>
{
    private List<Node> selectedItems;

    private Boolean activateThumbnail = Boolean.FALSE;

    private RenditionManager renditionManager;

    private int mode;

    public NodeAdapter(Activity context, AlfrescoSession session, int textViewResourceId, List<Node> listItems,
            List<Node> selectedItems, int mode)
    {
        super(context, textViewResourceId, listItems);
        this.selectedItems = selectedItems;
        this.renditionManager = SessionUtils.getRenditionManager(context);
        this.mode = mode;
    }

    public NodeAdapter(Context context, int textViewResourceId, List<Node> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

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
        if (selectedItems != null && selectedItems.size() == 1
                && selectedItems.get(0).getIdentifier().equals(item.getIdentifier()))
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
}
