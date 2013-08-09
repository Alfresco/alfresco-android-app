/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.person;

import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskFragment;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

/**
 * @since 1.3.0
 * @author jpascal
 */
public class PersonAdapter extends BaseListAdapter<Person, GenericViewHolder>
{
    private Fragment fragment;

    private Map<String, Person> selectedItems;

    private RenditionManager renditionManager;

    private boolean isEditable = false;

    public PersonAdapter(Fragment fr, int textViewResourceId, List<Person> listItems, boolean isEditable)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.fragment = fr;
        this.renditionManager = ApplicationManager.getInstance(getContext()).getRenditionManager(fr.getActivity());
        this.isEditable = isEditable;
    }

    public PersonAdapter(Fragment fr, int textViewResourceId, List<Person> listItems, Map<String, Person> selectedItems)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.fragment = fr;
        this.selectedItems = selectedItems;
        this.renditionManager = ApplicationManager.getInstance(getContext()).getRenditionManager(fr.getActivity());
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        if (isEditable && getCount() == 1)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(textViewResourceId, null);
            GenericViewHolder vh = create(vhClassName, v);
            v.setTag(vh);
            Person item = getItem(position);
            updateBottomText(vh, item);
            updateTopText(vh, item);
            renditionManager.display(vh.icon, item.getIdentifier(), R.drawable.ic_avatar);
            return v;
        }
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return super.getView(position, convertView, parent);
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Person item)
    {
        vh.bottomText.setText(item.getJobTitle());
        if (selectedItems != null && selectedItems.containsKey(item.getIdentifier()))
        {
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()), getContext().getResources()
                    .getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()), null);
        }
    }

    @Override
    protected void updateIcon(final GenericViewHolder vh, final Person item)
    {
        renditionManager.display(vh.icon, item.getIdentifier(), R.drawable.ic_avatar);

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
                    if (fragment instanceof CreateTaskFragment)
                    {
                        ((CreateTaskFragment) fragment).removeAssignee(item, v);
                    }
                }
            });
        }

    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Person item)
    {
        vh.topText.setText(item.getFullName());
    }

}
