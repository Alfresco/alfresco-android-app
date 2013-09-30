package org.alfresco.mobile.android.application.configuration;

import java.io.Serializable;
import java.util.Map;

public class ConfigurationContext implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Map<String, Object> json;

    public static ConfigurationContext parseJson(Map<String, Object> json)
    {
        ConfigurationContext config = new ConfigurationContext();
        config.json = json;
        return config;
    }

    public Map<String, Object> getJson()
    {
        return json;
    }
}
