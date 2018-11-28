/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.alfresco.mobile.android.application.configuration;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.config.ActionConfig;
import org.alfresco.mobile.android.api.model.config.ActionGroupConfig;
import org.alfresco.mobile.android.api.model.config.impl.ActionConfigImpl;
import org.alfresco.mobile.android.api.model.impl.DocumentImpl;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.apache.chemistry.opencmis.commons.enums.Action;

import android.content.Context;

/**
 * Created by jpascal on 21/03/2016.
 */
public final class ConfigurableActionHelper
{

    // public static final int ACTION_NODE_CREATE = 0;

    public static final int ACTION_NODE_DELETE = 1;

    public static final int ACTION_NODE_FAVORITE = 2;

    public static final int ACTION_NODE_LIKE = 3;

    public static final int ACTION_NODE_REVIEW = 4;

    public static final int ACTION_NODE_COMMENT = 5;

    public static final int ACTION_NODE_UPDATE = 6;

    public static final int ACTION_NODE_EDIT = 7;

    public static final int ACTION_NODE_RENAME = 8;

    public static final int ACTION_NODE_DOWNLOAD = 9;

    public static final int ACTION_NODE_SYNC = 10;

    public static final int ACTION_NODE_OPEN = 11;

    public static final int ACTION_NODE_SHARE = 12;

    public static final int ACTION_CREATE_FOLDER = 13;

    public static final int ACTION_CREATE_DOC = 14;

    public static final int ACTION_NODE_UPLOAD = 15;

    public static final int ACTION_NODE_EDIT_WITH_ALFRESCO = 16;

    public static final boolean isVisible(Context context, AlfrescoAccount account, int actionId)
    {
        return isVisible(context, account, null, null, actionId);
    }

    public static final boolean isVisible(Context context, AlfrescoAccount account, AlfrescoSession session, Node node,
            int actionId)
    {
        try
        {
            // First we check if configuration
            // No Config ==> Everything is ON
            boolean allowedByConfig = true;
            boolean permissionRequired = false;
            ConfigManager configManager = ConfigManager.getInstance(context);
            if (configManager != null && configManager.getConfig(account.getId()) != null
                    && configManager.getConfig(account.getId()).hasActionConfig())
            {
                ConfigService service = configManager.getConfig(account.getId());
                if (service == null) { return true; }

                String profileId = configManager.getCurrentProfileId();
                if (profileId == null)
                {
                    profileId = service.getDefaultProfile().getIdentifier();
                }

                String rootActionId = service.getProfile(profileId).getRootActionId();
                if (rootActionId == null || service.getActionConfig(rootActionId) == null) { return true; }

                String actionNameId = null;
                // Config available Let's check
                switch (actionId)
                {
                    case ACTION_CREATE_FOLDER:
                        actionNameId = ConfigurationConstant.KEY_ACTION_CREATE_FOLDER;
                        permissionRequired = true;
                        break;
                    case ACTION_CREATE_DOC:
                        actionNameId = ConfigurationConstant.KEY_ACTION_CREATE_DOCUMENT;
                        permissionRequired = true;
                        break;
                    case ACTION_NODE_UPLOAD:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_UPLOAD;
                        permissionRequired = true;
                        break;
                    case ACTION_NODE_OPEN:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_OPEN;
                        break;
                    case ACTION_NODE_DOWNLOAD:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_DOWNLOAD;
                        break;
                    case ACTION_NODE_FAVORITE:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_FAVORITE;
                        break;
                    case ACTION_NODE_LIKE:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_LIKE;
                        break;
                    case ACTION_NODE_EDIT:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_EDIT;
                        permissionRequired = true;
                        break;
                    case ACTION_NODE_UPDATE:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_UPDATE;
                        permissionRequired = true;
                        break;
                    case ACTION_NODE_DELETE:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_DELETE;
                        permissionRequired = true;
                        break;
                    case ACTION_NODE_COMMENT:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_COMMENT;
                        permissionRequired = true;
                        break;
                    case ACTION_NODE_REVIEW:
                        actionNameId = ConfigurationConstant.KEY_ACTION_WORKFLOW;
                        break;
                    case ACTION_NODE_SYNC:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_SYNC;
                        break;
                    case ACTION_NODE_SHARE:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_SHARE;
                        break;
                    case ACTION_NODE_EDIT_WITH_ALFRESCO:
                        actionNameId = ConfigurationConstant.KEY_ACTION_NODE_EDIT_WITH_ALFRESCO;
                        permissionRequired = true;
                        break;
                }
                ActionConfig config = ((ActionGroupConfig) service.getActionConfig(rootActionId))
                        .getChildById(actionNameId);
                // Config disable the action
                if (config != null && !config.isEnable()) { return false; }

                ActionConfigImpl actionConfig = ((ActionConfigImpl)service.getActionConfig(actionNameId));

                if (actionConfig != null && !actionConfig.isEnable()) {
                    return false;
                }
                
                // Config not present or is enable
                allowedByConfig = (config == null) || config.isEnable();
            }

            // Configuration disable action
            if (!permissionRequired) { return allowedByConfig; }

            // If configuration doesn't disable we can check permission.
            boolean hasPermission = true;

            Permissions permission = session.getServiceRegistry().getDocumentFolderService().getPermissions(node);
            switch (actionId)
            {
                case ACTION_NODE_UPLOAD:
                case ACTION_CREATE_DOC:
                case ACTION_CREATE_FOLDER:
                    hasPermission = permission.canAddChildren();
                    break;
                case ACTION_NODE_EDIT:
                case ACTION_NODE_EDIT_WITH_ALFRESCO:
                    hasPermission = permission.canEdit();
                    break;
                case ACTION_NODE_UPDATE:
                    hasPermission = ((DocumentImpl) node).hasAllowableAction(Action.CAN_SET_CONTENT_STREAM.value());
                    break;
                case ACTION_NODE_DELETE:
                    hasPermission = permission.canDelete();
                    break;
                case ACTION_NODE_COMMENT:
                    hasPermission = permission.canComment();
                    break;
            }

            return hasPermission;
        }
        catch (Exception e)
        {
            return true;
        }
    }
}
