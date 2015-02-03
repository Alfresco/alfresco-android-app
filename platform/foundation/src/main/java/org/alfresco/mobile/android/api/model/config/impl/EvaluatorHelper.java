/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.api.model.config.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.OperatorType;
import org.alfresco.mobile.android.api.model.RepositoryInfo;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.EvaluatorType;
import org.alfresco.mobile.android.api.model.impl.RepositoryVersionHelper;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.text.TextUtils;
import android.util.Log;

/**
 * @author Jean Marie Pascal
 */
public class EvaluatorHelper extends HelperConfig
{
    private static final String TAG = EvaluatorHelper.class.getSimpleName();

    private LinkedHashMap<String, EvaluatorConfigData> evaluatorIndex;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    EvaluatorHelper(ConfigurationImpl context, StringHelper localHelper)
    {
        super(context, localHelper);
    }

    void addEvaluators(Map<String, Object> json)
    {
        if (json == null) { return; }
        evaluatorIndex = new LinkedHashMap<String, EvaluatorConfigData>(json.size());
        EvaluatorConfigData evalConfig = null;
        for (Entry<String, Object> entry : json.entrySet())
        {
            evalConfig = EvaluatorConfigData.parse(entry.getKey(), JSONConverter.getMap(entry.getValue()));
            if (evalConfig == null)
            {
                continue;
            }
            evaluatorIndex.put(evalConfig.identifier, evalConfig);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean evaluateIfEvaluator(Map<String, Object> evaluatorsConfiguration, ConfigScope extraParameters)
    {
        if (!evaluatorsConfiguration.containsKey(ConfigConstants.EVALUATOR)) { return true; }
        return evaluate(JSONConverter.getString(evaluatorsConfiguration, ConfigConstants.EVALUATOR), extraParameters);
    }

    public boolean evaluate(String evaluatorId, ConfigScope extraParameters)
    {
        if (evaluatorId == null) { return true; }
        return resolveEvaluator(evaluatorId, extraParameters);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVALUATION
    // ///////////////////////////////////////////////////////////////////////////
    protected boolean resolveEvaluator(String evaluatorId, ConfigScope extraParameters)
    {
        Boolean result = false;

        if (!evaluatorIndex.containsKey(evaluatorId))
        {
            Log.w(TAG, "Evaluator  [" + evaluatorId + "] doesn't exist. Check your configuration.");
            return false;
        }

        EvaluatorConfigData evalConfig = evaluatorIndex.get(evaluatorId);
        if (evalConfig == null) { return false; }
        if (evalConfig.hasMatchOperator)
        {
            boolean intermediateResult = false;
            for (String evalId : evalConfig.evaluatorIds)
            {
                intermediateResult = resolveEvaluator(evalId, extraParameters);
                // If result == True && match any we can exit
                if (intermediateResult && ConfigConstants.MATCH_ANY_VALUE.equals(evalConfig.matchOperator))
                {
                    result = true;
                    break;
                }
                // If result == false && match all we can exit
                else if (!intermediateResult && ConfigConstants.MATCH_ALL_VALUE.equals(evalConfig.matchOperator))
                {
                    result = false;
                    break;
                }
                // else we continue
            }
        }
        else
        {
            result = resolveEvaluator(evalConfig, extraParameters);
        }

        return result;
    }

    protected boolean resolveEvaluator(EvaluatorConfigData evalConfig, ConfigScope extraParameters)
    {
        Boolean result = false;

        EvaluatorType configtype = EvaluatorType.fromValue(evalConfig.type);
        if (configtype == null)
        {
            Log.w(TAG, "Evaluator Type  [" + evalConfig.type + "]  for [" + evalConfig.identifier
                    + "] doesn't exist. Check your configuration.");
            return false;
        }

        switch (configtype)
        {
            case HAS_REPOSITORY_CAPABILITY:
                result = evaluateRepositoryCapability(evalConfig);
                break;
            case NODE_TYPE:
                result = evaluateNodeType(evalConfig, extraParameters);
                break;
            case HAS_ASPECT:
                result = evaluateHasAspect(evalConfig, extraParameters);
                break;
            default:
                break;
        }

        Log.d(TAG, "EVALUATOR [" + evalConfig.identifier + "] : " + ((evalConfig.hasNegation) ? !result : result));

        return (evalConfig.hasNegation) ? !result : result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVALUATION REGISTRY
    // //////////////////////////////////////////////////////////////////////////
    private Boolean evaluateHasAspect(EvaluatorConfigData evalConfig, ConfigScope extraParameters)
    {
        if (extraParameters == null || extraParameters.getContextValue(ConfigScope.NODE) == null)
        {
            Log.w(TAG, "Evaluator  [" + evalConfig.identifier + "] requires a node object.");
            return false;
        }

        String aspectName = (String) evalConfig.getParameter(ConfigConstants.ASPECT_NAME_VALUE);
        if (TextUtils.isEmpty(aspectName))
        {
            Log.w(TAG, "Evaluator  [" + evalConfig.identifier + "] requires a typeName value.");
            return false;
        }

        Node node = (Node) extraParameters.getContextValue(ConfigScope.NODE);

        return node.hasAspect(aspectName);
    }

    private Boolean evaluateNodeType(EvaluatorConfigData evalConfig, ConfigScope extraParameters)
    {
        if (extraParameters == null || extraParameters.getContextValue(ConfigScope.NODE) == null)
        {
            Log.w(TAG, "Evaluator  [" + evalConfig.identifier + "] requires a node object.");
            return false;
        }

        String typeName = (String) evalConfig.getParameter(ConfigConstants.TYPE_NAME_VALUE);
        if (TextUtils.isEmpty(typeName))
        {
            Log.w(TAG, "Evaluator  [" + evalConfig.identifier + "] requires a typeName value.");
            return false;
        }

        Node node = (Node) extraParameters.getContextValue(ConfigScope.NODE);

        return typeName.equalsIgnoreCase(node.getType());
    }

    private boolean evaluateRepositoryCapability(EvaluatorConfigData evalConfig)
    {
        boolean result = true;

        if (!hasConfiguration() || ((ConfigurationImpl) getConfiguration()).getSession() == null)
        {
            Log.w(TAG, "Evaluator  [" + evalConfig.identifier + "] requires a session.");
            return false;
        }

        // SESSION
        if (evalConfig.configMap.containsKey(ConfigConstants.SESSION_VALUE))
        {
            result = false;
            String sessionType = JSONConverter.getString(evalConfig.configMap, ConfigConstants.SESSION_VALUE);
            if (ConfigConstants.CLOUD_VALUE.equals(sessionType) && ((ConfigurationImpl) getConfiguration()).getSession() instanceof CloudSession)
            {
                result = true;
            }
            else if (ConfigConstants.ONPREMISE_VALUE.equals(sessionType) && ((ConfigurationImpl) getConfiguration()).getSession() instanceof RepositorySession)
            {
                result = true;
            }
        }

        if (!result) { return false; }

        RepositoryInfo repoInfo = ((ConfigurationImpl) getConfiguration()).getSession().getRepositoryInfo();

        // Edition
        if (evalConfig.configMap.containsKey(ConfigConstants.EDITION_VALUE))
        {
            result = repoInfo.getEdition().equalsIgnoreCase(
                    JSONConverter.getString(evalConfig.configMap, ConfigConstants.EDITION_VALUE));
        }

        if (!result) { return false; }

        OperatorType operator = OperatorType.EQUAL;
        if (evalConfig.configMap.containsKey(ConfigConstants.OPERATOR_VALUE))
        {
            operator = OperatorType.fromValue(JSONConverter.getString(evalConfig.configMap,
                    ConfigConstants.OPERATOR_VALUE));
        }

        if (operator == null)
        {
            Log.w(TAG, "Evaluator  [" + evalConfig.identifier + "] has a wrong operator [" + evalConfig.type
                    + "]. Check your configuration.");
            return false;
        }

        int versionNumber = 0;
        int repoVersionNumber = 0;
        // Major Version
        if (evalConfig.configMap.containsKey(ConfigConstants.MAJORVERSION_VALUE))
        {
            versionNumber += 100 * JSONConverter.getInteger(evalConfig.configMap, ConfigConstants.MAJORVERSION_VALUE)
                    .intValue();
            repoVersionNumber += 100 * repoInfo.getMajorVersion();
        }

        // Minor Version
        if (evalConfig.configMap.containsKey(ConfigConstants.MINORVERSION_VALUE))
        {
            versionNumber += 10 * JSONConverter.getInteger(evalConfig.configMap, ConfigConstants.MINORVERSION_VALUE)
                    .intValue();
            repoVersionNumber += 10 * repoInfo.getMinorVersion();
        }

        // Maintenance Version
        if (evalConfig.configMap.containsKey(ConfigConstants.MAINTENANCEVERSION_VALUE))
        {
            if (repoInfo.getEdition().equals(OnPremiseConstant.ALFRESCO_EDITION_ENTERPRISE))
            {
                versionNumber += JSONConverter.getInteger(evalConfig.configMap,
                        ConfigConstants.MAINTENANCEVERSION_VALUE).intValue();
                repoVersionNumber += repoInfo.getMaintenanceVersion();
            }
            else
            {
                result = evaluate(operator, RepositoryVersionHelper.getVersionString(repoInfo.getVersion(), 2),
                        JSONConverter.getString(evalConfig.configMap, ConfigConstants.MAINTENANCEVERSION_VALUE));
            }
        }

        result = evaluate(operator, repoVersionNumber, versionNumber);

        return result;
    }

    private boolean evaluate(OperatorType operator, int value, int valueExpected)
    {
        switch (operator)
        {
            case INFERIOR:
                return value < valueExpected;
            case INFERIOR_OR_EQUAL:
                return value <= valueExpected;
            case SUPERIOR_OR_EQUAL:
                return value >= valueExpected;
            case SUPERIOR:
                return value > valueExpected;
            default:
                return value == valueExpected;
        }
    }

    private boolean evaluate(OperatorType operator, String value, String valueExpected)
    {
        int compareValue = value.compareTo(valueExpected);
        switch (operator)
        {
            case INFERIOR:
                return compareValue < 0;
            case INFERIOR_OR_EQUAL:
                return compareValue <= 0;
            case SUPERIOR_OR_EQUAL:
                return compareValue >= 0;
            case SUPERIOR:
                return compareValue > 0;
            default:
                return compareValue == 0;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL UTILITY CLASS
    // ///////////////////////////////////////////////////////////////////////////
    protected static class EvaluatorConfigData extends ItemConfigImpl
    {
        public String identifier;

        public boolean hasNegation = false;

        public boolean hasMatchOperator = false;

        public String type;

        public String matchOperator;

        public ArrayList<String> evaluatorIds;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        EvaluatorConfigData()
        {
            super();
        }

        static EvaluatorConfigData parse(String identifier, Map<String, Object> json)
        {
            EvaluatorConfigData eval = new EvaluatorConfigData();
            if (identifier.startsWith(ConfigConstants.NEGATE_SYMBOL))
            {
                eval.identifier = identifier.replace(ConfigConstants.NEGATE_SYMBOL, "");
                eval.hasNegation = true;
            }
            else
            {
                eval.identifier = identifier;
            }

            eval.type = JSONConverter.getString(json, ConfigConstants.TYPE_VALUE);

            if (json.containsKey(ConfigConstants.MATCH_ALL_VALUE))
            {
                eval.matchOperator = ConfigConstants.MATCH_ALL_VALUE;
                eval.hasMatchOperator = true;
            }
            else if (json.containsKey(ConfigConstants.MATCH_ANY_VALUE))
            {
                eval.matchOperator = ConfigConstants.MATCH_ANY_VALUE;
                eval.hasMatchOperator = true;
            }
            else
            {
                eval.configMap = JSONConverter.getMap(json.get(ConfigConstants.PARAMS_VALUE));
            }

            if (eval.matchOperator != null)
            {
                List<Object> idsObject = JSONConverter.getList(json.get(eval.matchOperator));
                ArrayList<String> ids = new ArrayList<String>(idsObject.size());
                for (Object object : idsObject)
                {
                    ids.add((String) object);
                }
                eval.evaluatorIds = ids;
            }

            return eval;
        }
    }
}
