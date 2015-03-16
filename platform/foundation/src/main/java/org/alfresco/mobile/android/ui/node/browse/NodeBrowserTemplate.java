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
package org.alfresco.mobile.android.ui.node.browse;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.ui.template.ListingTemplate;

public interface NodeBrowserTemplate extends ListingTemplate
{
    String ARGUMENT_FOLDER_NODEREF = "nodeRef";

    String ARGUMENT_FOLDER = "parentFolder";

    String ARGUMENT_SITE = "site";

    String ARGUMENT_SITE_SHORTNAME = ConfigConstants.SITE_SHORTNAME_VALUE;

    String ARGUMENT_PATH = "path";

    String ARGUMENT_FOLDER_TYPE_ID = "folderTypeId";

    String FOLDER_TYPE_SHARED = "shared";

    String FOLDER_TYPE_USERHOME = "userhome";

    /**
     * Allowable sorting property : Name of the document or folder.
     */
    String ORDER_BY_NAME = DocumentFolderService.SORT_PROPERTY_NAME;

    /**
     * Allowable sorting property : Title of the document or folder.
     */
    String ORDER_BY_TITLE = DocumentFolderService.SORT_PROPERTY_TITLE;

    /**
     * Allowable sorting property : Description
     */
    String ORDER_BY_DESCRIPTION = DocumentFolderService.SORT_PROPERTY_DESCRIPTION;

    /**
     * Allowable sorting property : Creation Date
     */
    String ORDER_BY_CREATED_AT = DocumentFolderService.SORT_PROPERTY_CREATED_AT;

    /**
     * Allowable sorting property : Modification Date
     */
    String ORDER_BY_MODIFIED_AT = DocumentFolderService.SORT_PROPERTY_MODIFIED_AT;
}
