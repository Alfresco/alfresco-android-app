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
package org.alfresco.mobile.android.application.ui.form.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.application.ui.form.BaseField;
import org.alfresco.mobile.android.application.ui.form.NodeField;
import org.alfresco.mobile.android.platform.mimetype.MimeType;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView.ScaleType;

/**
 * Provides access to node (documents or folders) and displays them as a view
 * based on GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class NodeFieldAdapter extends BaseListAdapter<Node, GenericViewHolder>
{
    protected WeakReference<Fragment> fragmentRef;

    protected String outputValue;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NodeFieldAdapter(Fragment fr, int textViewResourceId, List<Node> listItems, String propertyId)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.fragmentRef = new WeakReference<Fragment>(fr);
        this.outputValue = propertyId;
    }

    // /////////////////////////////////////////////////////////////
    // ITEM LINE
    // ////////////////////////////////////////////////////////////
    @Override
    protected void updateTopText(GenericViewHolder vh, Node item)
    {
        vh.topText.setVisibility(View.GONE);
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Node item)
    {
        if (NodeField.OUTPUT_ID.equals(outputValue))
        {
            vh.bottomText.setText(item.getIdentifier());
        }
        else if (NodeField.OUTPUT_OBJECT.equals(outputValue))
        {
            vh.bottomText.setText(item.getName());
        }
        else
        {
            if (item.getProperty(outputValue) != null)
            {
                vh.bottomText.setText(BaseField.getStringValue(getContext(), item.getProperty(outputValue).getValue()));
            }
            else
            {
                vh.bottomText.setText("");
            }
        }
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, final Node item)
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
