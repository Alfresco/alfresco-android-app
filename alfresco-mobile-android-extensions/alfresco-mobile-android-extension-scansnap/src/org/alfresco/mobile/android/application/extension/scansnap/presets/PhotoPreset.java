/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.extension.scansnap.presets;

import org.alfresco.mobile.android.application.extension.scansnap.R;

/**
 * Each photograph is stored as a high-resolution scanned image.
 * 
 * @author Jean Marie Pascal
 */
public class PhotoPreset extends DefaultPreset
{
    public static final int ID = 2;

    public PhotoPreset()
    {
        super();
        // UI parameters
        titleId = R.string.scan_preset_photo;
        iconId = org.alfresco.mobile.android.foundation.R.drawable.mime_img;

        // Scan parameters
        savetogether = SAVETOGETHER_DISABLE;
        format = FORMAT_JPEG;
        scanMode = SCANMODE_BETTER;
    }

    @Override
    public int getIdentifier()
    {
        return ID;
    }
}
