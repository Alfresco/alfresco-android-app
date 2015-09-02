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
package org.alfresco.mobile.android.application.providers.storage;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

public class EncodedQueryUri
{
    // //////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // //////////////////////////////////////////////////////////////////////
    private static final String TAG = EncodedQueryUri.class.getSimpleName();

    private static final String SEPARATOR = "&";

    private static final String ARGUMENT_TYPE = "t";

    private static final String ARGUMENT_ACCOUNT = "acc";

    private static final String ARGUMENT_ID = "id";

    private static final String ARGUMENT_CURSOR_ID = "cid";

    // //////////////////////////////////////////////////////////////////////
    // MEMBERS
    // //////////////////////////////////////////////////////////////////////
    int type;

    long accountId;

    String id;

    Long cid;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public EncodedQueryUri(int type, long account, String value, Long cursorId)
    {
        this.type = type;
        this.accountId = account;
        this.id = value;
        this.cid = cursorId;
    }

    public EncodedQueryUri(int type, long account, String value)
    {
        this.type = type;
        this.accountId = account;
        this.id = value;
        this.cid = null;
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

        if (params.containsKey(ARGUMENT_TYPE))
        {
            type = Integer.parseInt(params.get(ARGUMENT_TYPE));
        }
        if (params.containsKey(ARGUMENT_ACCOUNT))
        {
            accountId = Long.parseLong(params.get(ARGUMENT_ACCOUNT));
        }
        if (params.containsKey(ARGUMENT_ID))
        {
            id = params.get(ARGUMENT_ID);
        }
        if (params.containsKey(ARGUMENT_CURSOR_ID))
        {
            cid = (params.get(ARGUMENT_CURSOR_ID) != null) ? Long.parseLong(params.get(ARGUMENT_CURSOR_ID)) : null;
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // STATIC
    // //////////////////////////////////////////////////////////////////////
    public static String encodeItem(Integer prefix, Long accountId, String id, Long cid)
    {
        // t=xxx;acc=xxx;id=xxx;cid=xxx
        StringBuilder encodedItem = new StringBuilder();
        if (prefix != null && prefix > 0)
        {
            encodedItem.append(ARGUMENT_TYPE);
            encodedItem.append("=");
            encodedItem.append(Integer.toString(prefix));
        }
        if (accountId != null && accountId > 0)
        {
            if (encodedItem.length() > 0)
            {
                encodedItem.append(SEPARATOR);
            }
            encodedItem.append(ARGUMENT_ACCOUNT);
            encodedItem.append("=");
            encodedItem.append(Long.toString(accountId));
        }
        if (!TextUtils.isEmpty(id))
        {
            if (encodedItem.length() > 0)
            {
                encodedItem.append(SEPARATOR);
            }
            encodedItem.append(ARGUMENT_ID);
            encodedItem.append("=");
            encodedItem.append(id);
        }
        if (cid != null && cid > 0)
        {
            if (encodedItem.length() > 0)
            {
                encodedItem.append(SEPARATOR);
            }
            encodedItem.append(ARGUMENT_CURSOR_ID);
            encodedItem.append("=");
            encodedItem.append(cid);
        }
        // Log.d(TAG, "encodeItem : " + encodedItem.toString());
        return encodedItem.toString();
    }

    public static String encodeItem(Integer prefix, Long accountId, String id)
    {
        // t=xxx;acc=xxx;id=xxx
        return encodeItem(prefix, accountId, id, null);
    }

}
