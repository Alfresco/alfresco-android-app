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
package org.alfresco.mobile.android.application.config.manager;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.config.CreationConfig;
import org.alfresco.mobile.android.api.model.config.ItemConfig;
import org.alfresco.mobile.android.api.services.ConfigService;

import android.app.Activity;
import android.view.ViewGroup;

public class CreateConfigManager  extends BaseConfigManager
{
    private CreationConfig creationConfig;
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateConfigManager(Activity activity, ConfigService configService, ViewGroup vRoot)
    {
        super(activity, configService);
        if (configService != null && configService.getCreationConfig(null) != null)
        {
            creationConfig = configService.getCreationConfig(null);
        }
        this.vRoot = vRoot;
    }

    public List<String> retrieveCreationDocumentTypeList()
    {
        List<String> fileTypes = new ArrayList<String>();
        
        if (creationConfig == null){return fileTypes;}
        for (ItemConfig itemConfig : creationConfig.getCreatableDocumentTypes())
        {
            fileTypes.add(itemConfig.getLabel());
        }
        
        return fileTypes;
    }
}
