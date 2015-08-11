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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.DevConfigModelHelper;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

public class ConfigMenuItemAdapter extends BaseListAdapter<ViewConfig, TwoLinesViewHolder>
        implements PopupMenu.OnMenuItemClickListener
{
    private final int INVALID_ID = -1;

    private HashMap<ViewConfig, Integer> mIdMap = new HashMap<>();

    private WeakReference<ConfigMenuEditorFragment> fragmentRef;

    private ViewConfig selectedItem;

    private int selectedCounter = 0;

    public ConfigMenuItemAdapter(ConfigMenuEditorFragment fragment, int textViewResourceId,
 List<ViewConfig> objects)
    {
        super(fragment.getActivity(), textViewResourceId, objects);
        this.vhClassName = TwoLinesViewHolder.class.getCanonicalName();
        fragmentRef = new WeakReference<>(fragment);
    }

    @Override
    protected void updateTopText(final TwoLinesViewHolder vh, final ViewConfig item)
    {
        vh.topText.setText(item.getLabel());
        vh.choose.setVisibility(View.VISIBLE);
        vh.choose.setImageResource(R.drawable.ic_more_options);
        vh.choose.setBackgroundResource(R.drawable.alfrescohololight_list_selector_holo_light);
        int d_16 = DisplayUtils.getPixels(getContext(), R.dimen.d_16);
        vh.choose.setPadding(d_16, d_16, d_16, d_16);
        vh.choose.setTag(R.id.node_action, item);
        vh.choose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectedItem = (ViewConfig) v.getTag(R.id.node_action);
                PopupMenu popup = new PopupMenu(getContext(), v);
                popup.getMenu().add(Menu.NONE, R.id.menu_action_edit, Menu.FIRST, R.string.edit);
                popup.getMenu().add(Menu.NONE, R.id.menu_action_delete, Menu.FIRST + 1, R.string.delete);
                popup.setOnMenuItemClickListener(ConfigMenuItemAdapter.this);
                popup.show();
            }
        });
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, ViewConfig item)
    {
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, ViewConfig item)
    {
        vh.icon.setImageResource(DevConfigModelHelper.getLightIconId(item));
    }

    @Override
    public long getItemId(int position)
    {
        if (position < 0 || position >= mIdMap.size()) { return INVALID_ID; }
        ViewConfig item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds()
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.config_menu_id:
                return true;
            case R.id.menu_action_delete:
                delete();
                return true;
            default:
                return false;
        }
    }

    private void delete()
    {
        if (selectedItem != null)
        {
            fragmentRef.get().delete(selectedItem);
        }
    }

}