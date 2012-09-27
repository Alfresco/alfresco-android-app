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
package org.alfresco.mobile.android.ui.filebrowser;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.content.Context;
import android.view.View;

/**
 * Provides access to files and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class LocalFileExplorerAdapter extends BaseListAdapter<File, GenericViewHolder>
{

    private List<File> selectedItems;

    public LocalFileExplorerAdapter(Activity context, int textViewResourceId, List<File> listItems)
    {
        this(context, textViewResourceId, listItems, null);
    }

    public LocalFileExplorerAdapter(Activity context, int textViewResourceId, List<File> listItems,
            List<File> selectedItems)
    {
        super(context, textViewResourceId, listItems);
        this.selectedItems = selectedItems;
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, File item)
    {
        vh.topText.setText(item.getName());
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, File item)
    {
        vh.bottomText.setText(createContentBottomText(getContext(), item));
        if (selectedItems != null && selectedItems.contains(item))
        {
            vh.choose.setVisibility(View.VISIBLE);
        }
        else
        {
            vh.choose.setVisibility(View.GONE);
        }

    }

    private String createContentBottomText(Context context, File file)
    {
        String s = "";
        s = formatDate(context, new Date(file.lastModified()));
        if (file.isFile())
        {
            s += " - " + Formatter.formatFileSize(context, file.length());
        }
        return s;
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, File item)
    {
        if (item.isFile())
        {
            // TODO Thumbnails
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(MimeTypeManager.getIcon(item.getName())));
        }
        else if (item.isDirectory())
        {
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mime_folder));
        }
    }
}
