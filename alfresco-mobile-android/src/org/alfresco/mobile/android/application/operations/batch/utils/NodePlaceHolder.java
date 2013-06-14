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

import static org.alfresco.mobile.android.api.model.impl.cloud.PublicAPIPropertyIds.GUID;
import static org.alfresco.mobile.android.api.model.impl.cloud.PublicAPIPropertyIds.NAME;
import static org.alfresco.mobile.android.api.model.impl.cloud.PublicAPIPropertyIds.REQUEST_STATUS;
import static org.alfresco.mobile.android.api.model.impl.cloud.PublicAPIPropertyIds.REQUEST_TYPE;
import static org.alfresco.mobile.android.api.model.impl.cloud.PublicAPIPropertyIds.SIZEINBYTES;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.impl.PropertyImpl;

import android.os.Parcel;
import android.os.Parcelable;

public class NodePlaceHolder implements Node
{
    private static final long serialVersionUID = 1L;

    private static final String PROGRESS = "progress";

    /** Map of properties available for this Node. */
    private Map<String, Property> properties;

    public NodePlaceHolder(String name, int type, int status)
    {
        properties = new HashMap<String, Property>(4);
        properties.put(GUID, new PropertyImpl(name));
        properties.put(NAME, new PropertyImpl(name));
        properties.put(REQUEST_TYPE, new PropertyImpl(type));
        properties.put(REQUEST_STATUS, new PropertyImpl(status));
    }

    public NodePlaceHolder(String name, int type, int status, long length, long progress)
    {
        this(name, type, status);
        properties.put(SIZEINBYTES, new PropertyImpl(length));
        properties.put(PROGRESS, new PropertyImpl(progress));
    }

    public long getLength()
    {
        if (getPropertyValue(SIZEINBYTES) == null) { return (long) -1; }
        return (Long) getPropertyValue(SIZEINBYTES);
    }
    
    public NodePlaceHolder setProgress(long progress){
        properties.put(PROGRESS, new PropertyImpl(progress));
        return this;
    }

    public long getProgress()
    {
        if (getPropertyValue(PROGRESS) == null) { return (long) -1; }
        return (Long) getPropertyValue(PROGRESS);
    }

    @Override
    public String getIdentifier()
    {
        return getPropertyValue(GUID);
    }

    @Override
    public String getName()
    {
        return getPropertyValue(NAME);
    }

    @Override
    public String getTitle()
    {
        return null;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public String getType()
    {
        return null;
    }

    @Override
    public String getCreatedBy()
    {
        return null;
    }

    @Override
    public GregorianCalendar getCreatedAt()
    {
        return null;
    }

    @Override
    public String getModifiedBy()
    {
        return null;
    }

    @Override
    public GregorianCalendar getModifiedAt()
    {
        return null;
    }

    @Override
    public Property getProperty(String name)
    {
        return null;
    }

    @Override
    public Map<String, Property> getProperties()
    {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPropertyValue(String name)
    {
        if (getProp(name) != null) { return (T) getProp(name).getValue(); }
        return null;
    }

    private Property getProp(String name)
    {
        return (properties != null) ? properties.get(name) : null;
    }

    @Override
    public boolean hasAspect(String aspectName)
    {
        return false;
    }

    @Override
    public List<String> getAspects()
    {
        return null;
    }

    @Override
    public boolean hasAllProperties()
    {
        return false;
    }

    @Override
    public boolean isFolder()
    {
        return false;
    }

    @Override
    public boolean isDocument()
    {
        return true;
    }

    public static final Parcelable.Creator<NodePlaceHolder> CREATOR = new Parcelable.Creator<NodePlaceHolder>()
    {
        public NodePlaceHolder createFromParcel(Parcel in)
        {
            return new NodePlaceHolder(in);
        }

        public NodePlaceHolder[] newArray(int size)
        {
            return new NodePlaceHolder[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeMap(properties);
    }

    public NodePlaceHolder(Parcel o)
    {
        this.properties = new HashMap<String, Property>();
        o.readMap(this.properties, getClass().getClassLoader());
    }

}
