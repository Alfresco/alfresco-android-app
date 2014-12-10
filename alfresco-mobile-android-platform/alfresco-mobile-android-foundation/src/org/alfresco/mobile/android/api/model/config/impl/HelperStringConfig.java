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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;

import android.util.Log;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class HelperStringConfig
{
    private static final String TAG = HelperStringConfig.class.getSimpleName();

    private Properties properties;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public static HelperStringConfig load(File file)
    {
        HelperStringConfig config = null;
        try
        {
            config = load(new FileInputStream(file));
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return config;
    }

    public static HelperStringConfig load(InputStream inputStream)
    {
        HelperStringConfig config = null;
        try
        {
            Properties properties = new Properties();
            properties.load(inputStream);
            config = new HelperStringConfig(properties);
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return config;
    }

    private HelperStringConfig(Properties properties)
    {
        this.properties = properties;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILITIES
    // ///////////////////////////////////////////////////////////////////////////
    public static String getLocalizedFileName()
    {
        String filename = ConfigConstants.CONFIG_LOCALIZATION_FILENAME;
        if (!Locale.ENGLISH.equals(Locale.getDefault().getLanguage()))
        {
            filename = String.format(ConfigConstants.CONFIG_LOCALIZATION_FILENAME_PATTERN, Locale.getDefault()
                    .getLanguage());
        }
        return filename;
    }
    
    public static String getDefaultLocalizedFileName()
    {
        return ConfigConstants.CONFIG_LOCALIZATION_FILENAME;
    }

    public static String getRepositoryLocalizedFilePath()
    {
        return ConfigConstants.CONFIG_LOCALIZATION_FOLDER_PATH.concat(getLocalizedFileName());
    }

    public static String getDefaultRepositoryLocalizedFilePath()
    {
        return ConfigConstants.CONFIG_LOCALIZATION_FOLDER_PATH
                .concat(ConfigConstants.CONFIG_LOCALIZATION_FILENAME);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public String getString(String key)
    {
        String value = key;
        if (properties == null) { return key; }
        value = properties.getProperty(key);
        return (value == null) ? key : value;
    }
}
