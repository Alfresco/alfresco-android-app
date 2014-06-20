/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.file.encryption;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.file.FileOperationRequest;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;

import android.content.ContentValues;
import android.database.Cursor;

public class DataProtectionRequest extends FileOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 453;

    private static final String PROP_ENCRYPT = "doEncrypt";

    private static final String PROP_ACTION = "intentAction";

    private static final String PROP_COPIED_FILE = "copiedFile";

    private boolean doEncrypt;

    protected Map<String, Serializable> persistentProperties;

    private int intentAction;

    private File copiedFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public DataProtectionRequest(File file, boolean doEncrypt)
    {
        this(file, doEncrypt, -1);
    }

    public DataProtectionRequest(File file, boolean doEncrypt, int intentAction)
    {
        this(file, null, doEncrypt, intentAction);
    }

    public DataProtectionRequest(File file, File copiedFile, boolean doEncrypt, int intentAction)
    {
        super(file.getPath());
        requestTypeId = TYPE_ID;

        setNotificationTitle(file.getName());
        setMimeType(file.getName());

        this.doEncrypt = doEncrypt;
        this.intentAction = intentAction;
        this.copiedFile = copiedFile;

        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(PROP_ENCRYPT, doEncrypt);
        persistentProperties.put(PROP_ACTION, intentAction);
        if (copiedFile != null)
        {
            persistentProperties.put(PROP_COPIED_FILE, copiedFile.getPath());
        }
    }

    public DataProtectionRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;

        // PROPERTIES
        Map<String, String> tmpProperties = retrievePropertiesMap(cursor);
        if (tmpProperties.containsKey(PROP_ENCRYPT))
        {
            doEncrypt = Boolean.parseBoolean(tmpProperties.remove(PROP_ENCRYPT));
        }

        if (tmpProperties.containsKey(PROP_ACTION))
        {
            intentAction = Integer.parseInt(tmpProperties.remove(PROP_ACTION));
        }

        if (tmpProperties.containsKey(PROP_COPIED_FILE))
        {
            copiedFile = new File(tmpProperties.remove(PROP_COPIED_FILE));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        if (persistentProperties != null && !persistentProperties.isEmpty())
        {
            cValues.put(BatchOperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        }
        return cValues;
    }

    protected Map<String, String> retrievePropertiesMap(Cursor cursor)
    {
        // PROPERTIES
        String rawProperties = cursor.getString(BatchOperationSchema.COLUMN_PROPERTIES_ID);
        if (rawProperties != null)
        {
            return MapUtil.stringToMap(rawProperties);
        }
        else
        {
            return new HashMap<String, String>();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean doEncrypt()
    {
        return doEncrypt;
    }

    public int getIntentAction()
    {
        return intentAction;
    }
    

    public File getCopiedFile()
    {
        return copiedFile;
    }
}
