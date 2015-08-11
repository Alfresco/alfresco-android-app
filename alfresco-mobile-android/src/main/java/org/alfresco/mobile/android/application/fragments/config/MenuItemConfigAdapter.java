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
import org.alfresco.mobile.android.application.configuration.model.ConfigModelHelper;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.SingleLineCheckBoxViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.os.Build;
import android.view.View;

public class MenuItemConfigAdapter extends
        BaseListAdapter<MenuConfigFragment.MenuItemConfig, SingleLineCheckBoxViewHolder>
{
    private final int INVALID_ID = -1;

    private HashMap<MenuConfigFragment.MenuItemConfig, Integer> mIdMap = new HashMap<>();

    private WeakReference<MenuConfigFragment> fragmentRef;

    private int selectedCounter = 0;

    public MenuItemConfigAdapter(MenuConfigFragment fragment, int textViewResourceId,
            List<MenuConfigFragment.MenuItemConfig> objects)
    {
        super(fragment.getActivity(), textViewResourceId, objects);
        this.vhClassName = SingleLineCheckBoxViewHolder.class.getCanonicalName();
        fragmentRef = new WeakReference<>(fragment);

        for (int i = 0; i < objects.size(); ++i)
        {
            if (objects.get(i) == null)
            {
                continue;
            }
            mIdMap.put(objects.get(i), i);
            if (objects.get(i).isEnable())
            {
                selectedCounter++;
            }
        }
    }

    @Override
    protected void updateTopText(final SingleLineCheckBoxViewHolder vh, final MenuConfigFragment.MenuItemConfig item)
    {
        vh.topText.setText(item.config.getLabel());
        vh.choose.setChecked(item.isEnable());
        vh.choose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                item.setEnable(vh.choose.isChecked());
                UIUtils.setBackground((View) vh.choose.getParent(), (vh.choose.isChecked()) ? null : getContext()
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
    protected void updateBottomText(SingleLineCheckBoxViewHolder vh, MenuConfigFragment.MenuItemConfig item)
    {
    }

    @Override
    protected void updateIcon(SingleLineCheckBoxViewHolder vh, MenuConfigFragment.MenuItemConfig item)
    {
        vh.icon.setImageResource(ConfigModelHelper.getLightIconId(item.config));
        UIUtils.setBackground((View) vh.choose.getParent(), (vh.choose.isChecked()) ? null : getContext()
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
        return android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }
}
