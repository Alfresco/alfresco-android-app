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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

/**
 * @author Jean Marie Pascal
 */
public class ProfileHelper extends HelperConfig
{
    private LinkedHashMap<String, ProfileConfig> profilesIndex;

    private String defaultProfileConfigId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    ProfileHelper(ConfigurationImpl context, StringHelper localHelper)
    {
        super(context, localHelper);
    }

    ProfileHelper(ConfigurationImpl context, StringHelper localHelper,
            LinkedHashMap<String, ProfileConfig> profilesIndex)
    {
        super(context, localHelper);
        this.profilesIndex = profilesIndex;
    }

    public void addProfiles(Map<String, Object> profilesMap)
    {
        profilesIndex = new LinkedHashMap<>(profilesMap.size());
        ProfileConfig profileConfig = null;
        for (Entry<String, Object> entry : profilesMap.entrySet())
        {
            profileConfig = ProfileConfigImpl.parse(entry.getKey(), JSONConverter.getMap(entry.getValue()),
                    getConfiguration());
            if (profileConfig == null)
            {
                continue;
            }

            if (profileConfig.isDefault())
            {
                defaultProfileConfigId = profileConfig.getIdentifier();
            }
            profilesIndex.put(profileConfig.getIdentifier(), profileConfig);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public ProfileConfig getProfileById(String profileId)
    {
        if (profilesIndex == null) { return null; }
        return (profilesIndex.containsKey(profileId)) ? profilesIndex.get(profileId) : null;
    }

    public List<ProfileConfig> getProfiles()
    {
        if (profilesIndex != null)
        {
            ArrayList<ProfileConfig> profiles = new ArrayList<>(profilesIndex.values());
            ArrayList<ProfileConfig> result = new ArrayList<>(profilesIndex.size());
            for (ProfileConfig config : profiles)
            {
                if (((ProfileConfigImpl) config).getEvaluator() != null)
                {
                    if (getEvaluatorHelper().evaluate(((ProfileConfigImpl) config).getEvaluator(), null))
                    {
                        result.add(config);
                    }
                }
                else
                {
                    result.add(config);
                }
            }
            return result;
        }
        return new ArrayList<>(0);
    }

    public ProfileConfig getDefaultProfile()
    {
        if (profilesIndex == null) { return null; }
        return (profilesIndex.containsKey(defaultProfileConfigId)) ? profilesIndex.get(defaultProfileConfigId) : null;
    }
}
