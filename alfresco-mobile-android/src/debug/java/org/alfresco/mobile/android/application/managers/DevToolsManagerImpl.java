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
package org.alfresco.mobile.android.application.managers;

import org.alfresco.mobile.android.application.fragments.config.ConfigMenuEditorFragment;
import org.alfresco.mobile.android.platform.extensions.DevToolsManager;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

public class DevToolsManagerImpl extends DevToolsManager
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static DevToolsManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new DevToolsManagerImpl(context);
            }

            return (DevToolsManager) mInstance;
        }
    }

    protected DevToolsManagerImpl(Context context)
    {
        super(context);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void displayMenuConfig(FragmentActivity activity)
    {
        ConfigMenuEditorFragment.with(activity).display();
    }
}
