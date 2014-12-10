/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.api.model.config.impl;

import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.RepositoryConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class RepositoryConfigImpl implements RepositoryConfig
{
    private String shareUrl;

    private String repoCMISUrl;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    static RepositoryConfigImpl parseJson(Map<String, Object> json)
    {
        if (json == null || json.isEmpty()) { return null; }
        RepositoryConfigImpl repoConfig = new RepositoryConfigImpl();

        repoConfig.shareUrl = JSONConverter.getString(json, ConfigConstants.SHARE_URL_VALUE);
        repoConfig.repoCMISUrl = JSONConverter.getString(json, ConfigConstants.CMIS_URL_VALUE);

        return repoConfig;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getShareURL()
    {
        return shareUrl;
    }

    @Override
    public String getCMISURL()
    {
        return repoCMISUrl;
    }
}
