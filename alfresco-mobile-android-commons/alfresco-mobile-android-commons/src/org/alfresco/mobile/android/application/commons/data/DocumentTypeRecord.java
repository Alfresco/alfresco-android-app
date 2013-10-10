/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.commons.data;

import java.io.Serializable;

/**
 * This class contains all informations about a specific file type. It's used by
 * fragment during in app creation process.
 * 
 * @author Jean Marie Pascal
 */
public class DocumentTypeRecord implements Serializable
{

    private static final long serialVersionUID = 1L;

    /** Unique Identifier of the file type. */
    public int id;

    /** Icon Ressource Identifier associated to the file type. */
    public int iconId;

    /**
     * String Ressource Identifier associated to the file type. <br/>
     * Example : Word Document
     */
    public int nameId;

    /**
     * String value that represents filename extension part of the document. <br/>
     * Example : .docx, .xlsx
     */
    public String extension;

    /**
     * String value that represents document mimetype . <br/>
     * Example :
     * application/vnd.openxmlformats-officedocument.wordprocessingml.document
     */
    public String mimetype;

    /**
     * Path value to a default template file associated to the document type. <br/>
     * By default the root folder is "assets" folder inside the application.
     */
    public String templatePath;

    public DocumentTypeRecord(int id, int iconId, int nameId, String extension, String mimetype, String template)
    {
        super();
        this.id = id;
        this.iconId = iconId;
        this.nameId = nameId;
        this.extension = extension;
        this.mimetype = mimetype;
        this.templatePath = template;
    }
}
