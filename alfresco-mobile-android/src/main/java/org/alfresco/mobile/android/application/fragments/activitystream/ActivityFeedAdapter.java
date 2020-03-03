/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.activitystream;

import java.util.List;

import org.alfresco.mobile.android.api.model.ActivityEntry;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.user.UserProfileFragment;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.ui.activitystream.ActivityStreamAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesCaptionViewHolder;

import android.annotation.TargetApi;
import android.os.Build;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

/**
 * Provides access to activity entries and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class ActivityFeedAdapter extends ActivityStreamAdapter implements OnMenuItemClickListener
{
    protected Fragment fr;

    public ActivityFeedAdapter(Fragment fr, int textViewResourceId,
            List<ActivityEntry> listItems, List<ActivityEntry> selectedItems)
    {
        super(fr, textViewResourceId, listItems, selectedItems);
        this.renditionManager = RenditionManagerImpl.getInstance(fr.getActivity());
        this.fr = fr;
    }

    protected void updateBottomText(TwoLinesCaptionViewHolder vh, ActivityEntry item)
    {
        super.updateBottomText(vh, item);
        vh.icon.setBackgroundResource(R.drawable.alfrescohololight_item_background_holo_light);
        vh.icon.setTag(R.id.entry_action, item);
        vh.icon.setOnClickListener(new OnClickListener()
        {

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public void onClick(View v)
            {
                ActivityEntry item = (ActivityEntry) v.getTag(R.id.entry_action);
                UserProfileFragment.with(fr.getActivity()).personId(item.getCreatedBy()).displayAsDialog();
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu, ActivityEntry entry)
    {
        if (entry.getCreatedBy() != null)
        {
            menu.add(Menu.NONE, R.id.menu_activity_profile, Menu.FIRST,
                    String.format(getContext().getString(R.string.activity_profile), entry.getCreatedBy()));
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick;
        switch (item.getItemId())
        {
            case R.id.menu_activity_profile:
                UserProfileFragment.with(fr.getActivity()).personId(selectedOptionItems.get(0).getCreatedBy())
                        .displayAsDialog();
                onMenuItemClick = true;
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }
}
