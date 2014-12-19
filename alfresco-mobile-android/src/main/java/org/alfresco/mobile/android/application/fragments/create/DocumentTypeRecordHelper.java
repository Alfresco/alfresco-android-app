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
package org.alfresco.mobile.android.application.fragments.create;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.config.CreationConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.config.ConfigManager;
import org.alfresco.mobile.android.application.config.manager.CreateConfigManager;
import org.alfresco.mobile.android.platform.data.DocumentTypeRecord;
import org.alfresco.mobile.android.platform.extensions.SamsungManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;

import android.app.Activity;
import android.content.Context;

/**
 * This class contains all informations about a specific file type. It's used by
 * fragment during in app creation process.
 * 
 * @author Jean Marie Pascal
 */
public class DocumentTypeRecordHelper
{
    public static final String TEMPLATEFOLDER_PATH = "FilesTemplates/Template";

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

    /** Unique Identifier for text document (txt). */
    public static final int IMAGE_ID = 50;

    /** Unique Identifier for text document (txt). */
    public static final int AUDIO_ID = 60;

    /** Unique Identifier for text document (txt). */
    public static final int VIDEO_ID = 70;

    private static CreateConfigManager creationConfig;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    private DocumentTypeRecordHelper()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public static List<DocumentTypeRecord> getCreationDocumentTypeList(Activity context)
    {
        ConfigManager configurationManager = ConfigManager.getInstance(context);
        if (configurationManager != null && configurationManager.hasConfig(SessionUtils.getAccount(context).getId()))
        {
            ConfigService service = configurationManager.getConfig(SessionUtils.getAccount(context).getId());
            if (service.getCreationConfig() != null)
            {
                creationConfig = new CreateConfigManager(context, service, null);
                return creationConfig.retrieveCreationDocumentList();
            }
        }
        return getInternalCreationDocumentTypeList(context);
    }

    private static List<DocumentTypeRecord> getInternalCreationDocumentTypeList(Context context)
    {
        List<DocumentTypeRecord> fileTypes = new ArrayList<DocumentTypeRecord>();
        fileTypes.add(new DocumentTypeRecord(R.drawable.mime_doc, context.getString(R.string.create_document_word),
                DOCX_EXTENSION, "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                TEMPLATEFOLDER_PATH.concat(DOCX_EXTENSION)));
        fileTypes.add(new DocumentTypeRecord(R.drawable.mime_ppt, context
                .getString(R.string.create_document_powerpoint), PPTX_EXTENSION,
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", TEMPLATEFOLDER_PATH
                        .concat(PPTX_EXTENSION)));
        fileTypes.add(new DocumentTypeRecord(R.drawable.mime_xls, context.getString(R.string.create_document_excel),
                XLSX_EXTENSION, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                TEMPLATEFOLDER_PATH.concat(XLSX_EXTENSION)));
        fileTypes.add(new DocumentTypeRecord(R.drawable.mime_txt, context.getString(R.string.create_document_text),
                TXT_EXTENSION, "text/plain", null));

        if (SamsungManager.getInstance(context) != null && SamsungManager.getInstance(context).hasPenEnable())
        {
            fileTypes.add(SamsungManager.getInstance(context).addDocumentTypeRecord());
        }
        return fileTypes;
    }

    public static List<DocumentTypeRecord> getOpenAsDocumentTypeList(Context context)
    {
        List<DocumentTypeRecord> fileTypes = new ArrayList<DocumentTypeRecord>();
        fileTypes.add(new DocumentTypeRecord(R.drawable.mime_img, context.getString(R.string.open_as_image), null,
                "image/jpeg", null));
        fileTypes.add(new DocumentTypeRecord(R.drawable.mime_video, context.getString(R.string.open_as_video), null,
                "video/mpeg", null));
        fileTypes.add(new DocumentTypeRecord(R.drawable.mime_audio, context.getString(R.string.open_as_audio), null,
                "audio/mpeg", null));
        fileTypes.add(new DocumentTypeRecord(R.drawable.mime_txt, context.getString(R.string.open_as_txt), null,
                "text/plain", null));

        if (SamsungManager.getInstance(context) != null)
        {
            fileTypes.add(SamsungManager.getInstance(context).addDocumentTypeRecord());
        }
        return fileTypes;
    }
}
