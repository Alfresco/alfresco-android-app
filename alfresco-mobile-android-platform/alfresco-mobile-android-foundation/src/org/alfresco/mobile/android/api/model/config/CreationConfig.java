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
package org.alfresco.mobile.android.api.model.config;

import java.util.List;

/**
 * Base type for Creation Configuration.
 * 
 * @author Jean Marie Pascal
 *
 */
public interface CreationConfig
{
    /** Returns the list of mimetypes the application should allow creation of. */
    List<ItemConfig> getCreatableMimeTypes();

    /**
     * Returns the list of content types the application should allow creation
     * of.
     */
    List<ItemConfig> getCreatableDocumentTypes();

    /**
     * Returns the list of folder types the application should allow creation
     * of.
     */
    List<ItemConfig> getCreatableFolderTypes();

}
