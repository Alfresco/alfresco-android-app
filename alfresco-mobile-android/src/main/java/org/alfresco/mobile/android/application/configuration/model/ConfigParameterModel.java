package org.alfresco.mobile.android.application.configuration.model;

import java.util.List;

import org.alfresco.mobile.android.api.model.PropertyType;

/**
 * Created by jpascal on 10/08/2015.
 */
public class ConfigParameterModel
{
    public final String name;

    public final int descriptionId;

    public final PropertyType type;

    public final boolean isMandatory;

    public final boolean isExclusive;

    public final boolean isSupported;

    private final List<String> values;

    public ConfigParameterModel(String name, int descriptionId)
    {
        this.name = name;
        this.descriptionId = descriptionId;
        this.type = PropertyType.STRING;
        this.isMandatory = false;
        this.isExclusive = false;
        this.values = null;
        this.isSupported = true;
    }

    public ConfigParameterModel(String name, int descriptionId, PropertyType type, boolean mandatory,
            boolean isExclusive)
    {
        this.name = name;
        this.descriptionId = descriptionId;
        this.type = type;
        this.isMandatory = mandatory;
        this.isExclusive = isExclusive;
        this.values = null;
        this.isSupported = true;
    }

    public ConfigParameterModel(String name, int descriptionId, PropertyType type, boolean mandatory,
            boolean isExclusive, boolean isSupported)
    {
        this.name = name;
        this.descriptionId = descriptionId;
        this.type = type;
        this.isMandatory = mandatory;
        this.isExclusive = isExclusive;
        this.values = null;
        this.isSupported = isSupported;
    }

    public ConfigParameterModel(String name, int descriptionId, PropertyType type, boolean mandatory,
            boolean isExclusive, List<String> values)
    {
        this.name = name;
        this.descriptionId = descriptionId;
        this.type = type;
        this.isMandatory = mandatory;
        this.isExclusive = isExclusive;
        this.values = values;
        this.isSupported = true;
    }
}