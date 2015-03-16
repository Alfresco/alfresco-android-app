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
package org.alfresco.mobile.android.application;

import org.alfresco.mobile.android.application.managers.upgrade.UpgradeManager;
import org.alfresco.mobile.android.async.OperationsUtils;
import org.alfresco.mobile.android.platform.SessionManager;

import android.app.Application;

public class AlfrescoApplication extends Application
{
    protected SessionManager sessionManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        sessionManager = SessionManager.getInstance(this);

        // Execute some upgrade if necessary
        UpgradeManager.execute(this);

        // Remove operations if necessary
        OperationsUtils.clean(this);
    }
}
