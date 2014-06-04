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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.config.Configuration;
import org.alfresco.mobile.android.platform.data.DocumentTypeRecord;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.ViewGroup;

public class CreationConfigurator extends BaseConfigurator
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANT
    // ///////////////////////////////////////////////////////////////////////////
    private static final String CATEGORY_CREATION = "creation";

    public static final String MIMETYPES = "mimetypes";

    private static final String TYPES = "types";

    private static final String TYPE = "type";

    private static final String NAME = "name";

    private static final String MIMETYPE = "mimetype";

    private static final String EXTENSION = "extension";

    private static final String PLATFORM = "platform";

    private static final String ANDROID = "android";

    private static final String TEMPLATEFOLDER_PATH = "FilesTemplates/Template";

    private static final String ENABLE = "enable";

    // ///////////////////////////////////////////////////////////////////////////
    // MEMBERS
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public CreationConfigurator(Activity activity, Configuration configurationContext)
    {
        super(activity, configurationContext);
    }

    public CreationConfigurator(Activity activity, Configuration configurationContext, ViewGroup vRoot)
    {
        super(activity, configurationContext);
        /*this.rootConfiguration = retrieveConfigurationByPath(configurationContext.getJson(),
                ConfigurationConstant.PATH_DEFAULT_CREATION);*/
        this.vRoot = vRoot;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public List<DocumentTypeRecord> retrieveCreationDocumentList()
    {
        List<DocumentTypeRecord> fileTypes = new ArrayList<DocumentTypeRecord>();
        List<Object> mimetypesProperties = JSONConverter.getList(rootConfiguration.get(MIMETYPES));
        for (Object mProperty : mimetypesProperties)
        {
            Map<String, Object> mimetypeProperty = JSONConverter.getMap(mProperty);

            // Check Platform restriction
            if (mimetypeProperty.containsKey(PLATFORM))
            {
                Boolean b = JSONConverter.getBoolean(JSONConverter.getMap(mimetypeProperty.get(PLATFORM)), ANDROID);
                if (b == null || !b)
                {
                    continue;
                }
            }
            String extension = JSONConverter.getString(mimetypeProperty, EXTENSION);
            fileTypes.add(new DocumentTypeRecord(MimeTypeManager.getInstance(getActivity()).getIcon(
                    "t".concat(extension)), JSONConverter.getString(mimetypeProperty, NAME), extension, JSONConverter
                    .getString(mimetypeProperty, MIMETYPE), (TextUtils.isEmpty(extension)) ? null : TEMPLATEFOLDER_PATH
                    .concat(extension)));
        }
        return fileTypes;
    }

    public List<String> retrieveCreationDocumentTypeList()
    {
        List<String> fileTypes = new ArrayList<String>();
        Map<String, Object> typesProperties = JSONConverter.getMap(rootConfiguration.get(TYPES));
        for (Entry<String, Object> mProperty : typesProperties.entrySet())
        {
            Map<String, Object> mimetypeProperty = JSONConverter.getMap(mProperty.getValue());

            // Check Platform restriction
            if (mimetypeProperty.containsKey(PLATFORM))
            {
                Boolean b = JSONConverter.getBoolean(JSONConverter.getMap(mimetypeProperty.get(PLATFORM)), ANDROID);
                if (b == null || !b)
                {
                    continue;
                }
            }

            // Check Enable flag
            if (mimetypeProperty.containsKey(ENABLE))
            {
                Boolean b = JSONConverter.getBoolean(mimetypeProperty, ENABLE);
                if (b == null || !b)
                {
                    continue;
                }
            }

            String type = mProperty.getKey();
            fileTypes.add(type);
        }
        return fileTypes;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
}
