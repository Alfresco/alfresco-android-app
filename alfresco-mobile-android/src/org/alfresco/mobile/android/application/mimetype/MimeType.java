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
package org.alfresco.mobile.android.application.mimetype;

import java.io.Serializable;

import android.content.Context;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class MimeType implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String TYPE_APPLICATION = "application";

    public static final String TYPE_AUDIO = "audio";

    public static final String TYPE_IMAGE = "image";

    public static final String TYPE_MESSAGE = "message";

    public static final String TYPE_MODEL = "model";

    public static final String TYPE_MULTIPART = "multipart";

    public static final String TYPE_TEXT = "text";

    public static final String TYPE_VIDEO = "video";

    private static final String PREFIX = "R.drawable.";

    private static final String DRAWABLE = "drawable";
    
    private static final String PACKAGE_NAME = "org.alfresco.mobile.android.application";

    private long id;

    private String extension;

    private String type;

    private String subType;

    private String description;

    private String smallIcon;

    private String largeIcon;

    
    public MimeType(String type, String subType)
    {
        super();
        this.type = type;
        this.subType = subType;
    }
    
    public MimeType(String extension, String type, String subType)
    {
        super();
        this.extension = extension;
        this.type = type;
        this.subType = subType;
    }
    
    public MimeType(long id, String extension, String type, String subType, String description, String smallIcon,
            String largeIcon)
    {
        super();
        this.id = id;
        this.extension = extension;
        this.type = type;
        this.subType = subType;
        this.description = description;
        this.smallIcon = smallIcon;
        this.largeIcon = largeIcon;
    }

    public long getId()
    {
        return id;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getType()
    {
        return type;
    }

    public String getSubType()
    {
        return subType;
    }

    public String getDescription()
    {
        return description;
    }

    public String getSmallIcon()
    {
        return smallIcon;
    }

    public String getLargeIcon()
    {
        return largeIcon;
    }

    public String getMimeType()
    {
        return type + "/" + subType;
    }

    // /////////////////////////////////////////////////////////////////
    // EXTRAS
    // /////////////////////////////////////////////////////////////////

    public Integer getSmallIconId(Context context)
    {
        return context.getResources().getIdentifier(smallIcon.substring(PREFIX.length()), DRAWABLE,
                PACKAGE_NAME);
    }

    public Integer getLargeIconId(Context context)
    {
        return context.getResources().getIdentifier(largeIcon.substring(PREFIX.length()), DRAWABLE,
                PACKAGE_NAME);
    }

    public static Integer getRessourceId(Context context, String rLabel)
    {
        return context.getResources().getIdentifier(rLabel.substring(PREFIX.length()), DRAWABLE,
                PACKAGE_NAME);
    }

    // /////////////////////////////////////////////////////////////////
    // FLAG
    // /////////////////////////////////////////////////////////////////
    public boolean isType(String typeRequested)
    {
        if (type != null)
        {
            return type.contains(typeRequested);
        }
        else
        {
            return false;
        }
    }

}
