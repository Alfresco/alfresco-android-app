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
package org.alfresco.mobile.android.application.fragments.builder;

import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.template.ListingTemplate;
import org.alfresco.mobile.android.ui.template.ViewTemplate;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

/**
 * Goal is to create a Fragment based on configuration provided.
 * 
 * @author jpascal
 */
public abstract class ListingFragmentBuilder extends AlfrescoFragmentBuilder
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected ListingFragmentBuilder()
    {

    }

    public ListingFragmentBuilder(FragmentActivity activity)
    {
        this(activity, null, null);
    }

    public ListingFragmentBuilder(FragmentActivity activity, Map<String, Object> configuration)
    {
        this(activity, configuration, null);
    }

    public ListingFragmentBuilder(FragmentActivity activity, Map<String, Object> configuration, Bundle b)
    {
        super(activity, configuration, b);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoFragmentBuilder setListingContext(ListingContext listing)
    {
        if (extraConfiguration == null)
        {
            extraConfiguration = new Bundle();
        }
        // extraConfiguration.putSerializable(LOAD_STATE, loadState);
        extraConfiguration.putSerializable(ListingTemplate.ARGUMENT_LISTING, listing);
        return this;
    }

    public AlfrescoFragmentBuilder mode(int mode)
    {
        if (extraConfiguration == null)
        {
            extraConfiguration = new Bundle();
        }
        // extraConfiguration.putSerializable(LOAD_STATE, loadState);
        extraConfiguration.putSerializable(ListingModeFragment.ARGUMENT_MODE, mode);
        return this;
    }

    public AlfrescoFragmentBuilder title(String title)
    {
        extraConfiguration.putString(ViewTemplate.ARGUMENT_LABEL, title);
        return this;
    }

    public AlfrescoFragmentBuilder description(String description)
    {
        extraConfiguration.putString(ViewTemplate.ARGUMENT_DESCRIPTION, description);
        return this;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DISPLAY
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void display()
    {
        // Display Fragment
        FragmentDisplayer.load(this).into(FragmentDisplayer.PANEL_LEFT);
    }
}
