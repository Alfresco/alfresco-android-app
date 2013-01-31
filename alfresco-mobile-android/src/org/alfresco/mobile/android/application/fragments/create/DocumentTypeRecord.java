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
package org.alfresco.mobile.android.application.fragments.create;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;

/**
 * This class contains all informations about a specific file type. It's used by
 * fragment during in app creation process.
 * 
 * @author Jean Marie Pascal
 */
public class DocumentTypeRecord implements Serializable
{

    private static final long serialVersionUID = 1L;

    private static final String TEMPLATEFOLDER_PATH = "FilesTemplates/Template";

    /** Filename Extension part for Microsoft Word document (docx). */
    private static final String DOCX_EXTENSION = ".docx";

    /** Filename Extension part Microsoft Powerpoint document (pptx). */
    private static final String PPTX_EXTENSION = ".pptx";

    /** Filename Extension part for Microsoft Excem document (xslx). */
    private static final String XLSX_EXTENSION = ".xlsx";

    /** Filename Extension part for text document (txt). */
    private static final String TXT_EXTENSION = ".txt";

    /** Unique Identifier for Microsoft Word document (docx). */
    public static final int WORD_ID = 10;

    /** Unique Identifier for Microsoft Powerpoint document (pptx). */
    public static final int POWERPOINT_ID = 20;

    /** Unique Identifier for Microsoft Excem document (xslx). */
    public static final int EXCEL_ID = 30;

    /** Unique Identifier for text document (txt). */
    public static final int TEXT_ID = 40;

    /** Unique Identifier of the file type. */
    int id;

    /** Icon Ressource Identifier associated to the file type. */
    int iconId;

    /**
     * String Ressource Identifier associated to the file type. <br/>
     * Example : Word Document
     */
    int nameId;

    /**
     * String value that represents filename extension part of the document. <br/>
     * Example : .docx, .xlsx
     */
    String extension;

    /**
     * String value that represents document mimetype . <br/>
     * Example :
     * application/vnd.openxmlformats-officedocument.wordprocessingml.document
     */
    String mimetype;

    /**
     * Path value to a default template file associated to the document type. <br/>
     * By default the root folder is "assets" folder inside the application.
     */
    String templatePath;

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

    /**
     * Default List of all Document types available for creation inside the
     * application.
     */
    static List<DocumentTypeRecord> DOCUMENT_TYPES_CREATION_LIST = new ArrayList<DocumentTypeRecord>(4)
    {
        private static final long serialVersionUID = 1L;
        {
            add(new DocumentTypeRecord(WORD_ID, R.drawable.mime_doc, R.string.create_document_word, DOCX_EXTENSION,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    TEMPLATEFOLDER_PATH.concat(DOCX_EXTENSION)));
            add(new DocumentTypeRecord(POWERPOINT_ID, R.drawable.mime_ppt, R.string.create_document_powerpoint,
                    PPTX_EXTENSION, "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    TEMPLATEFOLDER_PATH.concat(PPTX_EXTENSION)));
            add(new DocumentTypeRecord(EXCEL_ID, R.drawable.mime_xls, R.string.create_document_excel, XLSX_EXTENSION,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    TEMPLATEFOLDER_PATH.concat(XLSX_EXTENSION)));
            add(new DocumentTypeRecord(TEXT_ID, R.drawable.mime_txt, R.string.create_document_text, TXT_EXTENSION,
                    "text/plain", null));
        }
    };
}
