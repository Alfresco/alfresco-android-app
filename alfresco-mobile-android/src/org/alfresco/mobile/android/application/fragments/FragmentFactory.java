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
package org.alfresco.mobile.android.application.fragments;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.application.accounts.fragment.AccountFragment;
import org.alfresco.mobile.android.application.fragments.activities.ActivitiesFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.browser.local.LocalFileBrowserFragment;
import org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment;

import android.app.Fragment;
import android.util.Log;

public class FragmentFactory
{

    private static final String TAG = "FragmentFactory";

    public static Fragment createInstance(String tag)
    {
        try
        {
            if (FragmentRegistry.containsKey(tag))
                return (Fragment) FragmentRegistry.get(tag).newInstance();
            else
                return null;
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "serial" })
    public static Map<String, Class> FragmentRegistry = new HashMap<String, Class>()
    {
        {
            put(ChildrenBrowserFragment.TAG, ChildrenBrowserFragment.class);
            put(ActivitiesFragment.TAG, ActivitiesFragment.class);
            put(MainMenuFragment.TAG, MainMenuFragment.class);
            put(AccountFragment.TAG, AccountFragment.class);
            put(KeywordSearch.TAG, KeywordSearch.class);
            put(LocalFileBrowserFragment.TAG, LocalFileBrowserFragment.class);

        }
    };

}
