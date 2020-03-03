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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;

public class ShortCutFolderMenuAdapter extends BaseListAdapter<Integer, TwoLinesViewHolder>
{
    public ShortCutFolderMenuAdapter(FragmentActivity context)
    {
        super(context, R.layout.app_path_shortcut, SHORTCUTS);
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, Integer item)
    {
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, Integer item)
    {
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, Integer item)
    {
        int iconId = -1;
        ((View) vh.icon.getParent()).setClickable(false);
        ((View) vh.icon.getParent()).setFocusable(false);
        vh.topText.setVisibility(View.VISIBLE);
        vh.topText.setText(getContext().getString(item));
        vh.bottomText.setVisibility(View.GONE);

        switch (item)
        {
            case R.string.shortcut_alfresco_downloads:
                iconId = R.drawable.ic_download_light;
                break;
            case R.string.shortcut_local_sdcard:
                iconId = R.drawable.ic_sdcard;
                break;
            case R.string.shortcut_library_office:
                iconId = R.drawable.ic_doc_light;
                break;
            case R.string.shortcut_library_images:
                iconId = R.drawable.ic_pictures;
                break;
            case R.string.shortcut_library_videos:
                iconId = R.drawable.ic_videos;
                break;
            case R.string.shortcut_library_audios:
                iconId = R.drawable.ic_music;
                break;
            case R.string.shortcut_thirdparty_applications:
                iconId = R.drawable.ic_share;
                break;
            case R.string.shortcut_local_downloads:
                iconId = R.drawable.ic_download_light;
                break;
            default:
                vh.topText.setVisibility(View.GONE);
                vh.bottomText.setVisibility(View.VISIBLE);
                vh.bottomText.setText(getContext().getString(item));
                ((View) vh.icon.getParent()).setClickable(true);
                ((View) vh.icon.getParent()).setFocusable(true);
                break;
        }
        if (iconId == -1)
        {
            vh.icon.setImageDrawable(null);
        }
        else
        {
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(iconId));
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }

    private static final List<Integer> SHORTCUTS = new ArrayList<Integer>(10)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.string.shortcut_alfresco);
            add(R.string.shortcut_alfresco_downloads);

            add(R.string.shortcut_local);
            add(R.string.shortcut_local_sdcard);
            add(R.string.shortcut_local_downloads);

            add(R.string.shortcut_library);
            add(R.string.shortcut_library_office);
            add(R.string.shortcut_library_audios);
            add(R.string.shortcut_library_videos);
            add(R.string.shortcut_library_images);

            add(R.string.shortcut_thirdparty_app);
            add(R.string.shortcut_thirdparty_applications);

        }
    };
}
