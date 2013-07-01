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
package org.alfresco.mobile.android.application.operations.sync.utils;

import org.alfresco.mobile.android.ui.utils.Formatter;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.Context;

/**
 * @author Jean Marie Pascal
 */
public final class NodeSyncPlaceHolderFormatter
{
    private NodeSyncPlaceHolderFormatter(){
    }

    public static String createContentBottomText(Context context, NodeSyncPlaceHolder node, boolean extended)
    {
        StringBuilder s = new StringBuilder();
        if (node.getCreatedAt() != null)
        {
            s.append(Formatter.formatToRelativeDate(context, node.getCreatedAt().getTime()));
            if (node.isDocument())
            {
                s.append(" - ");
                s.append(Formatter.formatFileSize(context, Long.parseLong((String) node.getPropertyValue(PropertyIds.CONTENT_STREAM_LENGTH))));

                if (extended)
                {
                    s.append(" - V:");
                    if ("0.0".equals((String)node.getPropertyValue(PropertyIds.VERSION_LABEL)))
                    {
                        s.append("1.0");
                    }
                    else
                    {
                        s.append((String)node.getPropertyValue(PropertyIds.VERSION_LABEL));
                    }
                }
            }
        }
        return s.toString();
    }
    
}
