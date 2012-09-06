/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.manager;

import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

public class ActionManager extends org.alfresco.mobile.android.ui.manager.ActionManager
{

    public static final String REFRESH_EXTRA = "refreshExtra";

    /**
     * Allow to pick file with other apps.
     * 
     * @return Activity for Result.
     */
    public static void actionRefresh(Fragment f, String refreshCategory, String type, Bundle bundle)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_REFRESH);
        i.addCategory(refreshCategory);
        if (type != null && type.length() > 0) i.setType(type);
        if (bundle != null) i.putExtra(REFRESH_EXTRA, bundle);
        f.startActivity(i);
    }

    public static void actionRefresh(Fragment f, String refreshCategory, String type)
    {
        actionRefresh(f, refreshCategory, type, null);
    }

}
