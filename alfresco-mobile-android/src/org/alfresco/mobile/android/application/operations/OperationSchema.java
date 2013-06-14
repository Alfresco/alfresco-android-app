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
package org.alfresco.mobile.android.application.operations;


public abstract class OperationSchema
{
    
    // ////////////////////////////////////////////////////
    // COLUMNS
    // ////////////////////////////////////////////////////
    public static final String COLUMN_ID = "_id";

    public static final int COLUMN_ID_ID = 0;

    public static final String COLUMN_ACCOUNT_ID = "account_identifier";

    public static final int COLUMN_ACCOUNT_ID_ID = 1;
    
    public static final String COLUMN_TENANT_ID = "tenant_identifier";

    public static final int COLUMN_TENANT_ID_ID = COLUMN_ACCOUNT_ID_ID + 1;
    
    //STATUS
    public static final String COLUMN_STATUS = "status";

    public static final int COLUMN_STATUS_ID = COLUMN_TENANT_ID_ID + 1;
    
    public static final String COLUMN_REASON = "reason";

    public static final int COLUMN_REASON_ID = COLUMN_STATUS_ID + 1;
    
    //REQUEST TYPE
    public static final String COLUMN_REQUEST_TYPE = "request_type";

    public static final int COLUMN_REQUEST_TYPE_ID = COLUMN_REASON_ID + 1;

    //NOTIFICATION
    public static final String COLUMN_TITLE = "notification_title";

    public static final int COLUMN_TITLE_ID = COLUMN_REQUEST_TYPE_ID + 1;
    
    public static final String COLUMN_NOTIFICATION_VISIBILITY = "notification_visibility";

    public static final int COLUMN_NOTIFICATION_VISIBILITY_ID = COLUMN_TITLE_ID + 1;

    //NODE REQUEST
    public static final String COLUMN_NODE_ID = "node_identifier";

    public static final int COLUMN_NODE_ID_ID = COLUMN_NOTIFICATION_VISIBILITY_ID + 1;
    
    public static final String COLUMN_PARENT_ID = "parent_identifier";

    public static final int COLUMN_PARENT_ID_ID = COLUMN_NODE_ID_ID + 1;

    public static final String COLUMN_MIMETYPE = "mime_type";

    public static final int COLUMN_MIMETYPE_ID = COLUMN_PARENT_ID_ID + 1;
    
    public static final String COLUMN_PROPERTIES = "properties";

    public static final int COLUMN_PROPERTIES_ID = COLUMN_MIMETYPE_ID + 1;

    //UPx REQUEST
    public static final String COLUMN_TOTAL_SIZE_BYTES = "total_size";

    public static final int COLUMN_TOTAL_SIZE_BYTES_ID = COLUMN_PROPERTIES_ID + 1;

    public static final String COLUMN_BYTES_DOWNLOADED_SO_FAR = "bytes_so_far";

    public static final int COLUMN_BYTES_DOWNLOADED_SO_FAR_ID = COLUMN_TOTAL_SIZE_BYTES_ID + 1;
    
    public static final String COLUMN_LOCAL_URI = "local_uri";

    public static final int COLUMN_LOCAL_URI_ID = COLUMN_BYTES_DOWNLOADED_SO_FAR_ID + 1;
  
    // ////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////
    protected static final String[] COLUMNS = { 
            COLUMN_ID, 
            COLUMN_ACCOUNT_ID,
            COLUMN_TENANT_ID,
            COLUMN_STATUS,
            COLUMN_REASON,
            COLUMN_REQUEST_TYPE,
            COLUMN_TITLE, 
            COLUMN_NOTIFICATION_VISIBILITY,
            COLUMN_NODE_ID, 
            COLUMN_PARENT_ID,
            COLUMN_MIMETYPE,
            COLUMN_PROPERTIES,
            COLUMN_TOTAL_SIZE_BYTES, 
            COLUMN_BYTES_DOWNLOADED_SO_FAR, 
            COLUMN_LOCAL_URI 
            };

    protected static final String QUERY_CREATE_COLUMNS =
              COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
            + COLUMN_ACCOUNT_ID + " LONG," 
            + COLUMN_TENANT_ID + " TEXT,"
            + COLUMN_STATUS + " INTEGER," 
            + COLUMN_REASON + " INTEGER,"
            + COLUMN_REQUEST_TYPE + " INTEGER,"
            + COLUMN_TITLE + " TEXT," 
            + COLUMN_NOTIFICATION_VISIBILITY + " INTEGER," 
            + COLUMN_NODE_ID + " TEXT," 
            + COLUMN_PARENT_ID + " TEXT," 
            + COLUMN_MIMETYPE + " TEXT,"
            + COLUMN_PROPERTIES + " TEXT,"
            + COLUMN_TOTAL_SIZE_BYTES + " LONG,"
            + COLUMN_BYTES_DOWNLOADED_SO_FAR + " LONG," 
            + COLUMN_LOCAL_URI + " TEXT";
    
    // ////////////////////////////////////////////////////
    // UTILS
    // ////////////////////////////////////////////////////
    protected static String[] join(String[]... parms)
    {
        // calculate size of target array
        int size = 0;
        for (String[] array : parms)
        {
            size += array.length;
        }

        String[] result = new String[size];

        int j = 0;
        for (String[] array : parms)
        {
            for (String s : array)
            {
                result[j++] = s;
            }
        }
        return result;
    }
}
