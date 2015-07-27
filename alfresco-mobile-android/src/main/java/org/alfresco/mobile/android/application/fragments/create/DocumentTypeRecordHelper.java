/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.create;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.CreateConfigManager;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.platform.data.DocumentTypeRecord;
import org.alfresco.mobile.android.platform.extensions.SamsungManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

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
    public static List<DocumentTypeRecord> getCreationDocumentTypeList(FragmentActivity context)
    {
        List<DocumentTypeRecord> documentTypes = new ArrayList<DocumentTypeRecord>(0);
        ConfigService service = ConfigManager.getInstance(context).getConfig(SessionUtils.getAccount(context).getId(),
                ConfigTypeIds.CREATION);
        if (service != null)
        {
            documentTypes = new CreateConfigManager(context, service, null).retrieveCreationDocumentList();
        }

        if (SamsungManager.getInstance(context) != null && SamsungManager.getInstance(context).hasPenEnable())
        {
            documentTypes.add(SamsungManager.getInstance(context).addDocumentTypeRecord());
        }
        return documentTypes;
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
