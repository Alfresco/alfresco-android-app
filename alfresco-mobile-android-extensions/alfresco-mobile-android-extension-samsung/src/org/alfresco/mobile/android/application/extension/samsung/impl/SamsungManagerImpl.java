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
        Spen spenPackage = new Spen();
        try
        {
            spenPackage.initialize(context);
            return spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        }
        catch (SsdkUnsupportedException e)
        {
            int eType = e.getType();
            if (eType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED)
            {
                // The device is not a Samsung device.
            }
            else if (eType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED)
            {
                // The device does not support the Pen package.
            }
            else if (eType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED)
            {
                // The Pen package APK is not installed on the device.
            }
            else if (eType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED)
            {
                // The Pen package library requires updates.
            }
            else if (eType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED)
            {
                // It is recommended that the Pen package library be updated to
                // the latest version available.
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
