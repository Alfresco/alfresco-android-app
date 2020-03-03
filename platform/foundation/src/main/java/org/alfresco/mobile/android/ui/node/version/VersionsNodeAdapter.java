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
package org.alfresco.mobile.android.ui.node.version;

import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesProgressViewHolder;

import android.content.res.Resources;
import androidx.fragment.app.FragmentActivity;

/**
 * Provides access to history version and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class VersionsNodeAdapter extends BaseListAdapter<Document, TwoLinesProgressViewHolder>
{
    private Resources res;

    public VersionsNodeAdapter(FragmentActivity context, int textViewResourceId, List<Document> listItems)
    {
        super(context, textViewResourceId, listItems);
        res = getContext().getResources();
        vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateTopText(TwoLinesProgressViewHolder vh, Document item)
    {
        vh.topText.setText(res.getString(R.string.metadata_prop_version) + " " + item.getVersionLabel());
    }

    @Override
    protected void updateBottomText(TwoLinesProgressViewHolder vh, Document item)
    {
        HolderUtils.makeMultiLine(vh.bottomText, 2);
        String s = formatDate(getContext(), item.getModifiedAt().getTime());
        vh.bottomText.setText(res.getString(R.string.metadata_prop_version_modified_by) + " " + item.getModifiedBy()
                + " - " + s);
        // current version
        if (item.isLatestVersion())
        {
            vh.topTextRight.setText(res.getString(R.string.metadata_prop_version_current));
        }
    }

    @Override
    protected void updateIcon(TwoLinesProgressViewHolder vh, Document item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(
                MimeTypeManager.getInstance(getContext()).getIcon(item.getName())));
    }
}
