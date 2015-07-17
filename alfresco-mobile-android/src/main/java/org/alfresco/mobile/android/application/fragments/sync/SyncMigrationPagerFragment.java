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

package org.alfresco.mobile.android.application.fragments.sync;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jpascal on 19/03/2015.
 */
public class SyncMigrationPagerFragment extends AlfrescoFragment
{
    public static final String ARGUMENT_POSITION = "position";

    private int position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);
        if (getRootView() != null) { return getRootView(); }

        setRootView(inflater.inflate(R.layout.fr_info_message, container, false));

        if (getArguments() != null)
        {
            position = getArguments().getInt(ARGUMENT_POSITION, 0);
        }

        int imageId;
        int titleId, textId, secondTextId = -1;
        switch (position)
        {
            case 1:
                imageId = R.drawable.ic_synced;
                titleId = R.string.sync_info_two_title;
                textId = R.string.sync_info_two_first_text;
                secondTextId = R.string.sync_info_two_second_text;
                break;
            case 2:
                imageId = R.drawable.ic_synced;
                titleId = R.string.sync_info_third_title;
                textId = R.string.sync_info_third_first_text;
                break;
            default:
                imageId = R.drawable.ic_synced;
                titleId = R.string.sync_info_one_title;
                textId = R.string.sync_info_one_first_text;
                secondTextId = R.string.sync_info_one_second_text;
                break;
        }

        ((TextView) viewById(R.id.info_title)).setText(titleId);
        ((TextView) viewById(R.id.info_first)).setText(textId);
        if (secondTextId != -1)
        {
            ((TextView) viewById(R.id.info_second)).setText(secondTextId);
        }
        else
        {
            hide(R.id.info_second);
        }

        return getRootView();
    }
}
