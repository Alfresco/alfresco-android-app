/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.storage.provider;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

public class EncodedQueryUri
{
    // //////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // //////////////////////////////////////////////////////////////////////
    private static final String TAG = EncodedQueryUri.class.getSimpleName();
    
    private static final String SEPARATOR = "&";
    
    private static final String PARAM_TYPE = "t";

    private static final String PARAM_ACCOUNT = "acc";

    private static final String PARAM_ID = "id";
    
    // //////////////////////////////////////////////////////////////////////
    // MEMBERS
    // //////////////////////////////////////////////////////////////////////
    int type;

    long account;

    String id;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public EncodedQueryUri(int type, long account, String value)
    {
        this.type = type;
        this.account = account;
        this.id = value;
    }

    public EncodedQueryUri(String encodedRow)
    {
        if (TextUtils.isEmpty(encodedRow)) { return; }

        Map<String, String> params = new HashMap<String, String>(3);
        String[] pair;
        String key, value;
        for (String param : encodedRow.split(SEPARATOR))
        {
            pair = param.split("=");
            key = pair[0];
            value = "";
            if (pair.length > 1)
            {
                value = pair[1];
            }
            params.put(key, value);
        }

        if (params.containsKey(PARAM_TYPE))
        {
            type = Integer.parseInt(params.get(PARAM_TYPE));
        }
        if (params.containsKey(PARAM_ACCOUNT))
        {
            account = Long.parseLong(params.get(PARAM_ACCOUNT));
        }
        if (params.containsKey(PARAM_ID))
        {
            id = params.get(PARAM_ID);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // STATIC
    // //////////////////////////////////////////////////////////////////////
    public static String encodeItem(Integer prefix, Long accountId, String id)
    {
        // t=xxx;acc=xxx;id=xxx
        StringBuilder encodedItem = new StringBuilder();
        if (prefix != null && prefix > 0)
        {
            encodedItem.append(PARAM_TYPE);
            encodedItem.append("=");
            encodedItem.append(Integer.toString(prefix));
        }
        if (accountId != null && accountId > 0)
        {
            if (encodedItem.length() > 0)
            {
                encodedItem.append(SEPARATOR);
            }
            encodedItem.append(PARAM_ACCOUNT);
            encodedItem.append("=");
            encodedItem.append(Long.toString(accountId));
        }
        if (!TextUtils.isEmpty(id))
        {
            if (encodedItem.length() > 0)
            {
                encodedItem.append(SEPARATOR);
            }
            encodedItem.append(PARAM_ID);
            encodedItem.append("=");
            encodedItem.append(id);
        }
        Log.d(TAG, "encodeItem : " + encodedItem.toString());
        return encodedItem.toString();
    }
    
}
