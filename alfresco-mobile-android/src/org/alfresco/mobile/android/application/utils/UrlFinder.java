/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.utils;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.utils.NodeRefUtils;

import android.text.TextUtils;

public final class UrlFinder
{

    private UrlFinder()
    {
    }

    private static final String NODEREF = "noderef=";

    private static final String ID = "id=";

    private static final List<String> PATTERNS = new ArrayList<String>(2);
    static
    {
        PATTERNS.add(NODEREF);
        PATTERNS.add(ID);
    }

    public static String getIdentifier(String url)
    {
        String identifier = null, tmp = null;
        tmp = url.toLowerCase();
        for (String pattern : PATTERNS)
        {
            if (tmp.contains(pattern.toLowerCase()))
            {
                identifier = TextUtils.substring(tmp, tmp.lastIndexOf(pattern) + pattern.length(), tmp.length());

                if (identifier.contains("&"))
                {
                    identifier = TextUtils.substring(identifier, 0, identifier.indexOf("&"));
                }

                if (NodeRefUtils.isNodeRef(identifier)) { return identifier; }
                if (NodeRefUtils.isVersionIdentifier(identifier)) { return identifier; }
                if (NodeRefUtils.isIdentifier(identifier)) { return identifier; }
            }
        }
        return null;
    }

}
