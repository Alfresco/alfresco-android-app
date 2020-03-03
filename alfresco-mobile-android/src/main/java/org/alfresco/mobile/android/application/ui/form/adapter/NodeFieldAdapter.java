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
package org.alfresco.mobile.android.application.ui.form.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.application.ui.form.fields.BaseField;
import org.alfresco.mobile.android.application.ui.form.fields.NodeField;
import org.alfresco.mobile.android.platform.mimetype.MimeType;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesProgressViewHolder;

import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView.ScaleType;

/**
 * Provides access to node (documents or folders) and displays them as a view
 * based on GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class NodeFieldAdapter extends BaseListAdapter<Node, TwoLinesProgressViewHolder>
{
    protected WeakReference<Fragment> fragmentRef;

    protected String outputValue;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NodeFieldAdapter(Fragment fr, int textViewResourceId, List<Node> listItems, String propertyId)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.vhClassName = TwoLinesProgressViewHolder.class.getCanonicalName();
        this.fragmentRef = new WeakReference<Fragment>(fr);
        this.outputValue = propertyId;
    }

    // /////////////////////////////////////////////////////////////
    // ITEM LINE
    // ////////////////////////////////////////////////////////////
    @Override
    protected void updateTopText(TwoLinesProgressViewHolder vh, Node item)
    {
        vh.topText.setVisibility(View.GONE);
    }

    @Override
    protected void updateBottomText(TwoLinesProgressViewHolder vh, Node item)
    {
        switch (outputValue)
        {
            case NodeField.OUTPUT_ID:
                vh.bottomText.setText(item.getIdentifier());
                break;
            case NodeField.OUTPUT_OBJECT:
                vh.bottomText.setText(item.getName());
                break;
            default:
                if (item.getProperty(outputValue) != null)
                {
                    vh.bottomText.setText(BaseField.getStringValue(getContext(), item.getProperty(outputValue)
                            .getValue()));
                }
                else
                {
                    vh.bottomText.setText("");
                }
                break;
        }
    }

    @Override
    protected void updateIcon(TwoLinesProgressViewHolder vh, final Node item)
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
                if (getFragment() instanceof EditPropertiesPickerFragment)
                {
                    ((EditPropertiesPickerFragment) fragmentRef.get()).removeValue(item);
                }
            }
        });

        if (item.isDocument())
        {
            MimeType mime = MimeTypeManager.getInstance(getContext()).getMimetype(item.getName());
            vh.icon.setImageResource(mime != null ? mime.getLargeIconId(getContext()) : MimeTypeManager.getInstance(
                    getContext()).getIcon(item.getName(), true));
            AccessibilityUtils.addContentDescription(vh.icon,
                    mime != null ? mime.getDescription() : ((Document) item).getContentStreamMimeType());
        }
        else if (item.isFolder())
        {
            vh.icon.setImageResource(R.drawable.mime_256_folder);
            AccessibilityUtils.addContentDescription(vh.icon, R.string.mime_folder);
        }
    }

    // /////////////////////////////////////////////////////////////
    // UTILITIES
    // ////////////////////////////////////////////////////////////
    protected Fragment getFragment()
    {
        return fragmentRef.get();
    }

}
