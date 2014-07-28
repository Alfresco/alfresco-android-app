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
package org.alfresco.mobile.android.ui.person;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * @since 1.3.0
 * @author jpascal
 */
public class PersonAdapter extends BaseListAdapter<Person, GenericViewHolder>
{
    protected Map<String, Person> selectedItems;

    protected RenditionManager renditionManager;
    
    protected WeakReference<Activity> activityRef;

    public PersonAdapter(Fragment fr, int textViewResourceId, List<Person> listItems)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.activityRef = new WeakReference<Activity>(fr.getActivity());
    }

    public PersonAdapter(Fragment fr, int textViewResourceId, List<Person> listItems,
            Map<String, Person> selectedItems)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.selectedItems = selectedItems;
        this.activityRef = new WeakReference<Activity>(fr.getActivity());
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
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
        RenditionManager.with(activityRef.get()).loadAvatar(item.getIdentifier()).placeHolder(R.drawable.ic_avatar).into(vh.icon);
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Person item)
    {
        vh.topText.setText(item.getFullName());
    }

}
