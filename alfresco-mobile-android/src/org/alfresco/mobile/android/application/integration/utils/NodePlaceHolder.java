package org.alfresco.mobile.android.application.integration.utils;

import static org.alfresco.mobile.android.api.model.impl.cloud.PublicAPIPropertyIds.GUID;
import static org.alfresco.mobile.android.api.model.impl.cloud.PublicAPIPropertyIds.NAME;
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

    public NodePlaceHolder(String name)
    {
        properties = new HashMap<String, Property>(4);
        properties.put(GUID, new PropertyImpl(name));
        properties.put(NAME, new PropertyImpl(name));
    }

    public NodePlaceHolder(String name, long length, long progress)
    {
        this(name);
        properties.put(SIZEINBYTES, new PropertyImpl(length));
        properties.put(PROGRESS, new PropertyImpl(progress));
    }

    public long getLength()
    {
        if (getPropertyValue(SIZEINBYTES) == null) { return -1; }
        return getPropertyValue(SIZEINBYTES);
    }
    
    public NodePlaceHolder setProgress(long progress){
        properties.put(PROGRESS, new PropertyImpl(progress));
        return this;
    }

    public long getProgress()
    {
        if (getPropertyValue(PROGRESS) == null) { return -1; }
        return getPropertyValue(PROGRESS);
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
