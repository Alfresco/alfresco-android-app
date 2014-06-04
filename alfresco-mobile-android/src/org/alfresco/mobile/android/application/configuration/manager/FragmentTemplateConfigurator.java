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
package org.alfresco.mobile.android.application.configuration.manager;

import org.alfresco.mobile.android.api.model.config.Configuration;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.builder.FragmentBuilderFactory;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.app.Activity;
import android.os.Bundle;

public class FragmentTemplateConfigurator extends BaseConfigurator
{

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public FragmentTemplateConfigurator(Activity activity, Configuration configurationContext, String path)
    {
        super(activity, configurationContext);
        //this.rootConfiguration = retrieveConfigurationByPath(configurationContext.getJson(), path.split("/"));
    }

    public void displayFragment(Bundle b)
    {
        AlfrescoFragmentBuilder viewConfig = FragmentBuilderFactory.createViewConfig(getActivity(),
                JSONConverter.getString(rootConfiguration, TYPE), rootConfiguration);
        viewConfig.addExtra(b).display();
    }

}
