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
package org.alfresco.mobile.android.application.extension.samsung.impl;

import org.alfresco.mobile.android.application.commons.data.DocumentTypeRecord;
import org.alfresco.mobile.android.application.commons.extensions.SamsungManager;

import android.content.Context;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;

public class SamsungManagerImpl extends SamsungManager
{
    /** Filename Extension part for  Samsung Note Document. */
    private static final String SAMSUNG_NOTE_EXTENSION = ".spd";
    
    /** Unique Identifier for Samsung Note Document. */
    public static final int SAMSUNG_NOTE_ID = 100;

    
    private Context context;

    public SamsungManagerImpl(Context context)
    {
        this.context = context;
    }

    public boolean hasPenEnable()
    {
        try
        {
            Spen spenPackage = new Spen();
            spenPackage.initialize(context);
            spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
            return true;
        }
        catch (SsdkUnsupportedException e)
        {
            int eType = e.getType();
            if (eType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED)
            {
                return false;
            }
            else if (eType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED)
            {
                return false;
            }
            else if (eType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED)
            {
                return true;
            }
            else if (eType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED)
            {
                return true;
            }
            else if (eType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED)
            {
                return true;
            }
        }
        catch (Exception e1)
        {
        }
        return false;
    }

  
    @Override
    public DocumentTypeRecord addDocumentTypeRecord()
    {
        return new DocumentTypeRecord(SAMSUNG_NOTE_ID, R.drawable.mime_pages, R.string.create_samsung_note,
                SAMSUNG_NOTE_EXTENSION, "application/samsung_note", null);
    }
}
