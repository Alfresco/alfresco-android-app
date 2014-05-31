/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.ui.node.version;

import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.ViewHolder;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Provides access to history version and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class VersionsNodeAdapter extends BaseListAdapter<Document, GenericVersionViewHolder>
{
    private Resources res;

    public VersionsNodeAdapter(Activity context, int textViewResourceId, List<Document> listItems)
    {
        super(context, textViewResourceId, listItems);
        res = getContext().getResources();
        vhClassName = GenericVersionViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateTopText(GenericVersionViewHolder vh, Document item)
    {
        vh.topText.setText(res.getString(R.string.metadata_prop_version) + " " + item.getVersionLabel());
    }

    @Override
    protected void updateBottomText(GenericVersionViewHolder vh, Document item)
    {
        String s = formatDate(getContext(), item.getModifiedAt().getTime());
        vh.line1Text.setText(res.getString(R.string.metadata_prop_version_modified_by) + " " + s);

        // modified by
        s = item.getModifiedBy();
        vh.line2Text.setText(res.getString(R.string.metadata_prop_version_modified_by) + " " + s);

        // comment
        /*
         * s = (item.getProperty("cmis:checkinComment") != null) ? (String)
         * item.getProperty("cmis:checkinComment") .getValue() : "";
         * vh.line3Text.setText(res.getString(R.string.version_comment) + " " +
         * s);
         */
        vh.line3Text.setVisibility(View.GONE);

        // current version
        vh.bottomText.setText(res.getString(R.string.metadata_prop_version_current) + " " + item.isLatestVersion());
    }

    @Override
    protected void updateIcon(GenericVersionViewHolder vh, Document item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(
                MimeTypeManager.getInstance(getContext()).getIcon(item.getName())));
    }
}

final class GenericVersionViewHolder extends ViewHolder
{
    public TextView topText;

    public TextView line1Text;

    public TextView line2Text;

    public TextView line3Text;

    public TextView bottomText;

    public ImageView icon;

    public GenericVersionViewHolder(View v)
    {
        super(v);
        icon = (ImageView) v.findViewById(R.id.icon);
        topText = (TextView) v.findViewById(R.id.toptext);
        line1Text = (TextView) v.findViewById(R.id.line1);
        line2Text = (TextView) v.findViewById(R.id.line2);
        line3Text = (TextView) v.findViewById(R.id.line3);
        bottomText = (TextView) v.findViewById(R.id.bottomtext);
    }
}
