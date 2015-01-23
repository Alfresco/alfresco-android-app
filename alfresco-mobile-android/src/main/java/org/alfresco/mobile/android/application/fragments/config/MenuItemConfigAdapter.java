package org.alfresco.mobile.android.application.fragments.config;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.utils.IconCheckBoxViewHolder;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.view.View;

public class MenuItemConfigAdapter extends BaseListAdapter<MenuConfigFragment.MenuItemConfig, IconCheckBoxViewHolder>
{
    private final int INVALID_ID = -1;

    private HashMap<MenuConfigFragment.MenuItemConfig, Integer> mIdMap = new HashMap<MenuConfigFragment.MenuItemConfig, Integer>();

    private WeakReference<MenuConfigFragment> fragmentRef;

    private int selectedCounter = 0;

    public MenuItemConfigAdapter(MenuConfigFragment fragment, int textViewResourceId,
            List<MenuConfigFragment.MenuItemConfig> objects)
    {
        super(fragment.getActivity(), textViewResourceId, objects);
        this.vhClassName = IconCheckBoxViewHolder.class.getCanonicalName();
        fragmentRef = new WeakReference<MenuConfigFragment>(fragment);

        for (int i = 0; i < objects.size(); ++i)
        {
            mIdMap.put(objects.get(i), i);
            if (objects.get(i).isEnable())
            {
                selectedCounter++;
            }
        }
    }

    @Override
    protected void updateTopText(final IconCheckBoxViewHolder vh, final MenuConfigFragment.MenuItemConfig item)
    {
        vh.topText.setText(item.config.getLabel());
        vh.checkBox.setChecked(item.isEnable());
        vh.checkBox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                item.setEnable(vh.checkBox.isChecked());
                UIUtils.setBackground((View) vh.checkBox.getParent(), (vh.checkBox.isChecked()) ? null : getContext()
                        .getResources().getDrawable(R.drawable.list_longpressed_holo));

                if (item.isEnable())
                {
                    selectedCounter++;
                }
                else
                {
                    selectedCounter--;
                }
                fragmentRef.get().updateCounter(selectedCounter);
            }
        });
    }

    @Override
    protected void updateBottomText(IconCheckBoxViewHolder vh, MenuConfigFragment.MenuItemConfig item)
    {
        vh.bottomText.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(IconCheckBoxViewHolder vh, MenuConfigFragment.MenuItemConfig item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(item.iconId));
        UIUtils.setBackground((View) vh.checkBox.getParent(), (vh.checkBox.isChecked()) ? null : getContext()
                .getResources().getDrawable(R.drawable.list_longpressed_holo));
    }

    @Override
    public long getItemId(int position)
    {
        if (position < 0 || position >= mIdMap.size()) { return INVALID_ID; }
        MenuConfigFragment.MenuItemConfig item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }
}
