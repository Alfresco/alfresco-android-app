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
package org.alfresco.mobile.android.application.operations.batch.utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapUtil
{
    public static String mapToString(Map<String, Serializable> map)
    {
        if (map == null || map.size() <= 0) { return ""; }
        StringBuilder stringBuilder = new StringBuilder();

        for (Entry<String, Serializable> entry : map.entrySet())
        {
            if (stringBuilder.length() > 0)
            {
                stringBuilder.append("&");
            }
            try
            {
                stringBuilder.append((entry.getKey() != null ? URLEncoder.encode(entry.getKey(), "UTF-8") : ""));
                stringBuilder.append("=");
                stringBuilder.append(entry.getValue() != null ? URLEncoder.encode(entry.getValue().toString(), "UTF-8")
                        : "");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return stringBuilder.toString();
    }

    public static Map<String, String> stringToMap(String input)
    {
        Map<String, String> map = new HashMap<String, String>();

        String[] nameValuePairs = input.split("&");
        for (String nameValuePair : nameValuePairs)
        {
            String[] nameValue = nameValuePair.split("=");
            try
            {
                map.put(URLDecoder.decode(nameValue[0], "UTF-8"),
                        nameValue.length > 1 ? URLDecoder.decode(nameValue[1], "UTF-8") : "");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return map;
    }
}
