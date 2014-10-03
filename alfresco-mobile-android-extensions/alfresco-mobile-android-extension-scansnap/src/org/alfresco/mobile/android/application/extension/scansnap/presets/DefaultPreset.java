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
import org.alfresco.mobile.android.application.extension.scansnap.ScanSnapResultActivity;

/**
 * All scanned pages are merged into a single PDF Image File.
 * 
 * @author Jean Marie Pascal
 */
public class DefaultPreset extends ScanSnapPreset
{
    public static final int ID = 0;

    public DefaultPreset()
    {
        // UI parameters
        titleId = R.string.scan_preset_default;
        iconId = org.alfresco.mobile.android.foundation.R.drawable.mime_generic;

        // Scan parameters
        paperSize = PAPERSIZE_AUTO;
        fileNameFormat = FILENAMEFORMAT_ATTACHED;
        outMode = OUTMODE_OPEN;
        reduceBleedThrough = REDUCEBLEEDTHROUGH_ENABLE;
        blankPageSkip = BLANKPAGESKIP_ENABLE;
        scanMode = SCANMODE_AUTO;
        callBack = "org.alfresco.mobile.android.application,".concat(ScanSnapResultActivity.class.getName());
    }

    @Override
    public int getIdentifier()
    {
        return ID;
    }
}
