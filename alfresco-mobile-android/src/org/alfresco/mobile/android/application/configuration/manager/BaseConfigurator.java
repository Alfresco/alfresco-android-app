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
package org.alfresco.mobile.android.application.configuration.manager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.config.ConfigContext;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

public class BaseConfigurator implements ConfigurationConstant
{
    private static final String TAG = BaseConfigurator.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // EVALUATORS REGISTRY
    // ///////////////////////////////////////////////////////////////////////////
    private static final Map<String, Integer> INDEX_EVALUATOR = new HashMap<String, Integer>(1)
    {
        {
            put(EVALUATOR_REPOSITORY_INFO, INDEX_REPOSITORY_INFO);
            put(EVALUATOR_NODE_TYPE, INDEX_NODE_TYPE);
            put(EVALUATOR_HAS_ASPECT, INDEX_HAS_ASPECT);
        }
    };

    private static final int INDEX_REPOSITORY_INFO = 0;

    private static final int INDEX_NODE_TYPE = 10;

    private static final int INDEX_HAS_ASPECT = 20;

    // ///////////////////////////////////////////////////////////////////////////
    // MEMBERS
    // ///////////////////////////////////////////////////////////////////////////
    protected Map<String, Object> rootConfiguration;

    protected ViewGroup vRoot;

    protected WeakReference<Activity> activity;

    protected ConfigContext configurationContext;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public BaseConfigurator(Activity activity, ConfigContext configurationContext)
    {
        this.configurationContext = configurationContext;
        this.activity = new WeakReference<Activity>(activity);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PARSING TOOLS
    // ///////////////////////////////////////////////////////////////////////////
    public static Map<String, Object> retrieveApplicationConfiguration(Map<String, Object> rootConfiguration)
    {
        Map<String, Object> applicationConfiguration = null;
        try
        {
            applicationConfiguration = JSONConverter.getMap(rootConfiguration.get(APPLICATION));
        }
        catch (Exception e)
        {
            // DO Nothing
        }
        return applicationConfiguration;
    }

    public static Map<String, Object> retrieveConfigurationByPath(Map<String, Object> rootConfiguration, String[] path)
    {
        Map<String, Object> configuration = rootConfiguration;
        try
        {
            for (String pathItem : path)
            {
                configuration = JSONConverter.getMap(configuration.get(pathItem));
            }
        }
        catch (Exception e)
        {
            // DO Nothing
        }
        return configuration;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PARSING
    // ///////////////////////////////////////////////////////////////////////////
   /* protected Map<String, Object> retrieveAllConfiguration(Entry<String, Object> menuConfig)
    {
        Map<String, Object> properties = new LinkedHashMap<String, Object>(), tmpProperties = null;

        tmpProperties = JSONConverter.getMap(menuConfig.getValue());

        // Check if Config ?
        if (tmpProperties.containsKey(MERGE))
        {
            // Configuration found, let's retrieve the config
            properties.putAll(retrievedLinkedConfiguration(tmpProperties));
        }
        else
        {
            properties.putAll(tmpProperties);
        }

        return properties;
    }

    protected Map<String, Object> retrievedLinkedConfiguration(Map<String, Object> currentProps)
    {
        Map<String, Object> propertiesResult = new LinkedHashMap<String, Object>();
        Map<String, Object> properties;

        for (Entry<String, Object> currentItem : currentProps.entrySet())
        {
            if (MERGE.equalsIgnoreCase(currentItem.getKey()))
            {
                String[] path = ((String) currentItem.getValue()).split("/");
                properties = retrieveConfigurationByPath(configurationContext.getJson(), path);

                for (Entry<String, Object> menuConfig : properties.entrySet())
                {
                    if (MERGE.equalsIgnoreCase(menuConfig.getKey()))
                    {
                        path = ((String) currentItem.getValue()).split("/");
                        propertiesResult.putAll(retrievedLinkedConfiguration(retrieveConfigurationByPath(
                                configurationContext.getJson(), path)));
                    }
                    else if (menuConfig.getValue() instanceof HashMap)
                    {
                        propertiesResult.put(menuConfig.getKey(),
                                retrievedLinkedConfiguration(JSONConverter.getMap(menuConfig.getValue())));
                    }
                    else
                    {
                        propertiesResult.put(menuConfig.getKey(), menuConfig.getValue());
                    }
                }
            }
            else
            {
                propertiesResult.put(currentItem.getKey(), currentItem.getValue());
            }
        }

        return propertiesResult;
    }*/

    // ///////////////////////////////////////////////////////////////////////////
    // EVALUATORS
    // ///////////////////////////////////////////////////////////////////////////
    protected boolean doesRespectEvaluators(Map<String, Object> evaluatorsConfiguration)
    {
        return doesRespectEvaluators(evaluatorsConfiguration, null);
    }

    protected boolean doesRespectEvaluators(Map<String, Object> evaluatorsConfiguration, Bundle extraParameters)
    {
        if (!evaluatorsConfiguration.containsKey(EVALUATORS)) { return true; }
        return evaluate(JSONConverter.getMap(evaluatorsConfiguration.get(EVALUATORS)), extraParameters);
    }

    protected boolean evaluate(Map<String, Object> evaluatorsConfiguration, Bundle extraParameters)
    {
        Boolean result = true;
        Map<String, Object> properties;
        for (Entry<String, Object> menuConfig : evaluatorsConfiguration.entrySet())
        {
            // Retrieve Configuration with pointer resolution
            /*properties = retrieveAllConfiguration(menuConfig);

            // Create Evaluators and evaluate
            if (properties.containsKey(TYPE))
            {
                result = resolveEvaluator(properties, extraParameters);
            }

            properties.clear();*/
        }

        return result;
    }

    protected Boolean resolveEvaluator(Map<String, Object> item, Bundle extraParameters)
    {
        Boolean result = false;
        Integer index = INDEX_EVALUATOR.get(item.get(TYPE));
        Map<String, Object> params = JSONConverter.getMap(item.get(PARAMS));
        if (index == null || params == null) { return false; }

        switch (index)
        {
            case INDEX_REPOSITORY_INFO:
                if (!params.containsKey(TYPE))
                {
                    break;
                }
                if (REPOSITORY_INFO_TYPE_CLOUD.equalsIgnoreCase(JSONConverter.getString(params, TYPE)))
                {
                    result = (SessionUtils.getSession(getActivity()) instanceof CloudSession);
                }

                if (REPOSITORY_INFO_TYPE_ONPREMISE.equalsIgnoreCase(JSONConverter.getString(params, TYPE)))
                {
                    result = (SessionUtils.getSession(getActivity()) instanceof RepositorySession);
                }
                break;
            case INDEX_HAS_ASPECT:
                if (!params.containsKey(ASPECT) || extraParameters == null
                        || !extraParameters.containsKey(EVALUATOR_ARGUMENT_NODE))
                {
                    break;
                }
                String aspect = JSONConverter.getString(params, ASPECT);
                Node currentNode = (Node) extraParameters.get(EVALUATOR_ARGUMENT_NODE);
                result = currentNode.hasAspect(aspect);

                break;
            case INDEX_NODE_TYPE:
                if (!params.containsKey(TYPE) || extraParameters == null
                        || !extraParameters.containsKey(EVALUATOR_ARGUMENT_NODE))
                {
                    break;
                }
                String type = JSONConverter.getString(params, TYPE);
                Node node = (Node) extraParameters.get(EVALUATOR_ARGUMENT_NODE);
                result = type.equalsIgnoreCase(node.getType());

                break;
            default:
                return false;
        }

        if (params.containsKey(NEGATE) && JSONConverter.getBoolean(params, NEGATE)) { return !result; }

        Log.d(TAG, "EVALUATOR : " + result + " [" + item + "]");

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILITY
    // ///////////////////////////////////////////////////////////////////////////
    public Activity getActivity()
    {
        return activity.get();
    }

}
