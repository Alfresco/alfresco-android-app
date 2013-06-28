/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.sync.utils;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.api.model.impl.PropertyImpl;
import org.alfresco.mobile.android.api.services.impl.AbstractDocumentFolderServiceImpl;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Action;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Jean Marie Pascal
 */
public class NodeSyncPlaceHolder implements Node
{
    private static final long serialVersionUID = 1L;

    /** NodeRef of the Node (Unique reference). */
    private String identifier;

    /** Map of properties available for this Node. */
    private Map<String, Property> properties;

    /** List of Aspects available for this Node. */
    private List<String> aspects;

    /** List of allowable actions. */
    private List<String> allowableActions;

    /** Indicates whether the node has all it’s metadata populated. */
    private boolean hasAllProperties = true;

    // ////////////////////////////////////////////////////
    // Constructors
    // ////////////////////////////////////////////////////
    public NodeSyncPlaceHolder()
    {
    }

    public NodeSyncPlaceHolder(Map<String, String> props)
    {
        this.hasAllProperties = false;
        properties = new HashMap<String, Property>(props.size());
        for (Entry<String, String> entry : props.entrySet())
        {
            if (entry.getKey().equals(PropertyIds.LAST_MODIFICATION_DATE) || entry.getKey().equals(PropertyIds.CREATION_DATE))
            {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(Long.parseLong(entry.getValue()));
                properties.put(entry.getKey(), new PropertyImpl(calendar, PropertyType.DATETIME));
                continue;
            }

            properties.put(entry.getKey(), new PropertyImpl(entry.getValue()));
        }
    }

    // ////////////////////////////////////////////////////
    // Shortcut and common methods
    // ////////////////////////////////////////////////////
    /** {@inheritDoc} */
    public String getIdentifier()
    {
        return getPropertyValue(PropertyIds.OBJECT_ID);
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return getPropertyValue(PropertyIds.NAME);
    }

    /** {@inheritDoc} */
    public String getTitle()
    {
        return getPropertyValue(ContentModel.PROP_TITLE);
    }

    /** {@inheritDoc} */
    public String getDescription()
    {
        return getPropertyValue(ContentModel.PROP_DESCRIPTION);
    }

    /** {@inheritDoc} */
    public String getType()
    {
        if (getPropertyValue(PropertyIds.OBJECT_TYPE_ID) != null)
        {
            if (((String) getPropertyValue(PropertyIds.OBJECT_TYPE_ID))
                    .startsWith(AbstractDocumentFolderServiceImpl.CMISPREFIX_DOCUMENT))
            {
                return ((String) getPropertyValue(PropertyIds.OBJECT_TYPE_ID)).replaceFirst(
                        AbstractDocumentFolderServiceImpl.CMISPREFIX_DOCUMENT, "");
            }
            else if (((String) getPropertyValue(PropertyIds.OBJECT_TYPE_ID))
                    .startsWith(AbstractDocumentFolderServiceImpl.CMISPREFIX_FOLDER))
            {
                return ((String) getPropertyValue(PropertyIds.OBJECT_TYPE_ID)).replaceFirst(
                        AbstractDocumentFolderServiceImpl.CMISPREFIX_FOLDER, "");
            }
            else if (ObjectType.DOCUMENT_BASETYPE_ID.equals(getPropertyValue(PropertyIds.OBJECT_TYPE_ID)))
            {
                return ContentModel.TYPE_CONTENT;
            }
            else if (ObjectType.FOLDER_BASETYPE_ID.equals(getPropertyValue(PropertyIds.OBJECT_TYPE_ID))) { return ContentModel.TYPE_FOLDER; }
            return getPropertyValue(PropertyIds.OBJECT_TYPE_ID);
        }
        else
        {
            return null;
        }
    }

    /** {@inheritDoc} */
    public String getCreatedBy()
    {
        return getPropertyValue(PropertyIds.CREATED_BY);
    }

    /** {@inheritDoc} */
    public GregorianCalendar getCreatedAt()
    {
        return getPropertyValue(PropertyIds.CREATION_DATE);
    }

    /** {@inheritDoc} */
    public String getModifiedBy()
    {
        return getPropertyValue(PropertyIds.LAST_MODIFIED_BY);
    }

    /** {@inheritDoc} */
    public GregorianCalendar getModifiedAt()
    {
        return getPropertyValue(PropertyIds.LAST_MODIFICATION_DATE);
    }

    // ////////////////////////////////////////////////////
    // Properties and Aspects
    // ////////////////////////////////////////////////////

    /** {@inheritDoc} */
    public Property getProperty(String name)
    {
        // Match specific alfresco metadata name to its translated cmis version
        // if necessary.
        return getProp(AbstractDocumentFolderServiceImpl.getPropertyName(name));
    }

    /** {@inheritDoc} */
    public Map<String, Property> getProperties()
    {
        if (properties != null) { return properties; }
        return null;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <T> T getPropertyValue(String name)
    {
        if (getProp(name) != null) { return (T) getProp(name).getValue(); }
        return null;
    }

    /**
     * Properties can be stored in 2 different ways depending on Node State. <br>
     * At creation, it's the cmisobject that is reponsible for properties.<br>
     * If configuration change occurs, properties are saved (serialize) as
     * "Parcel object" into generic object.<br>
     * After configuration change, node is restore with simple data object like
     * a list of properties<br>
     * 
     * @return Property object.
     */
    private Property getProp(String name)
    {
        if (properties != null)
        {
            return properties.get(name);
        }
        else
        {
            return null;
        }
    }

    /** {@inheritDoc} */
    public boolean hasAspect(String aspectName)
    {
        String tmpAspectName = aspectName;
        if (!aspectName.startsWith(AbstractDocumentFolderServiceImpl.CMISPREFIX_ASPECTS))
        {
            tmpAspectName = AbstractDocumentFolderServiceImpl.CMISPREFIX_ASPECTS + aspectName;
        }
        if (aspects != null)
        {
            return aspects.contains(tmpAspectName);
        }
        else
        {
            return false;
        }
    }

    /** {@inheritDoc} */
    public List<String> getAspects()
    {
        return null;
    }

    @Override
    public boolean hasAllProperties()
    {
        return hasAllProperties;
    }

    // ////////////////////////////////////////////////////
    // Types
    // ////////////////////////////////////////////////////

    /** {@inheritDoc} */
    public boolean isFolder()
    {
        return ObjectType.FOLDER_BASETYPE_ID.equals(getPropertyValue(PropertyIds.BASE_TYPE_ID));
    }

    /** {@inheritDoc} */
    public boolean isDocument()
    {
        return ObjectType.DOCUMENT_BASETYPE_ID.equals(getPropertyValue(PropertyIds.BASE_TYPE_ID));
    }

    // ////////////////////////////////////////////////////
    // PERMISSION
    // ////////////////////////////////////////////////////
    /**
     * @param action : cmis type of action like move, delete, create...
     * @return Returns true if the specific action is allowable
     * @see org.apache.chemistry.opencmis.commons.enums.Action
     */
    public boolean hasAllowableAction(Action action)
    {
        if (allowableActions != null)
        {
            return allowableActions.contains(action.value());
        }
        else
        {
            return false;
        }
    }

    /**
     * @param action : cmis type of action like move, delete, create...
     * @return Returns true if the specific action is allowable
     * @see org.apache.chemistry.opencmis.commons.enums.Action
     */
    public boolean hasAllowableAction(String action)
    {
        if (allowableActions != null)
        {
            return allowableActions.contains(action);
        }
        else
        {
            return false;
        }
    }

    /**
     * @return Returns a set of all available allowable actions. If no allowable
     *         actions available returns an empty collection.
     */
    public Set<String> getAllowableActions()
    {
        Set<String> s = new HashSet<String>();
        if (allowableActions != null)
        {
            s = new HashSet<String>(allowableActions);
        }
        return s;
    }

    // ////////////////////////////////////////////////////
    // EXTRA TBD
    // ////////////////////////////////////////////////////

    public String getPath()
    {
        if (getProperty(PropertyIds.PATH) != null && getProperty(PropertyIds.PATH).getValue() != null) { return getProperty(
                PropertyIds.PATH).getValue().toString(); }
        return null;
    }

    // ////////////////////////////////////////////////////
    // Save State - serialization / deserialization
    // ////////////////////////////////////////////////////
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Android specific internal methods to save information depending on state.
     * </br> By state it means that
     * <ul>
     * <li>a node is created by a "cmisobject" so parameter object is not null</li>
     * <li>if configuration change (orientation screen change...) the parameter
     * object is not saved so become null.</li>
     * <li>To save informations (properties, aspects...), Android can use Parcel
     * object that can store important information.</li>
     * </ul>
     * This method is similar as "serialization" in java world.
     */
    @Override
    public void writeToParcel(Parcel dest, int arg1)
    {
        dest.writeString(identifier);
        dest.writeMap(properties);
        dest.writeList(aspects);
        dest.writeList(allowableActions);
        dest.writeString(Boolean.toString(hasAllProperties));
    }

    /**
     * Android specific internal methods to retrieve information depending on
     * state.</br> By state it means that
     * <ul>
     * <li>a node is created by a "cmisobject" so parameter object is not
     * null</br></li>
     * <li>if configuration change (orientation screen change...) the parameter
     * object is not saved so become null.</li>
     * <li>To save informations (properties, aspects...), Android can use Parcel
     * object that can store important information.</li>
     * </ul>
     * This method is similar as "deserialization" in java world.
     */
    public static final Parcelable.Creator<NodeSyncPlaceHolder> CREATOR = new Parcelable.Creator<NodeSyncPlaceHolder>()
    {
        public NodeSyncPlaceHolder createFromParcel(Parcel in)
        {
            return new NodeSyncPlaceHolder(in);
        }

        public NodeSyncPlaceHolder[] newArray(int size)
        {
            return new NodeSyncPlaceHolder[size];
        }
    };

    /**
     * Constructor of a Node object depending of a Parcel object previously
     * created by writeToParcel method.
     * 
     * @param o the Parcel object
     */
    public NodeSyncPlaceHolder(Parcel o)
    {
        this.identifier = o.readString();
        this.properties = new HashMap<String, Property>();
        o.readMap(this.properties, getClass().getClassLoader());
        this.aspects = new ArrayList<String>();
        o.readList(this.aspects, getClass().getClassLoader());
        this.allowableActions = new ArrayList<String>();
        o.readList(this.allowableActions, getClass().getClassLoader());
        this.hasAllProperties = Boolean.parseBoolean(o.readString());
    }
}
