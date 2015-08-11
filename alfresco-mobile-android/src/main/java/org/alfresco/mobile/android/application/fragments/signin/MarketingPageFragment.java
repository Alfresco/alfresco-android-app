/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco Activiti Mobile for Android.
 *
 * Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.alfresco.mobile.android.application.fragments.signin;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jpascal on 19/03/2015.
 */
public class MarketingPageFragment extends AlfrescoFragment
{
    public static final String ARGUMENT_POSITION = "position";

    private int position;

    public MarketingPageFragment()
    {
        requiredSession = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);
        if (getRootView() != null) { return getRootView(); }

        setRootView(inflater.inflate(R.layout.fr_welcome_page, container, false));

        if (getArguments() != null)
        {
            position = getArguments().getInt(ARGUMENT_POSITION, 0);
        }

        int imageId = -1;
        int textId = -1;
        /*
         * switch (position) { case 1: imageId = R.drawable.marketing_1; textId
         * = R.string.text_marketing_1; break; case 2: imageId =
         * R.drawable.marketing_2; textId = R.string.text_marketing_2; break;
         * default: imageId = R.drawable.marketing_3; textId =
         * R.string.text_marketing_3; break; }
         */

        // ((WelcomeActivity)
        // getActivity()).getPicasso().load(imageId).into((ImageView)
        // viewById(R.type.image));
        // ((TextView) viewById(R.type.text)).setText(textId);

        return getRootView();
    }
}
