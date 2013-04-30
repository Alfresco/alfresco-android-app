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
package org.alfresco.mobile.android.application.fragments;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.application.accounts.fragment.AccountsFragment;
import org.alfresco.mobile.android.application.fragments.activities.ActivitiesFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment;
import org.alfresco.mobile.android.application.fragments.search.KeywordSearch;

import android.app.Fragment;
import android.util.Log;

public final class FragmentFactory
{

    private FragmentFactory()
    {
    }

    private static final String TAG = "FragmentFactory";

    public static Fragment createInstance(String tag)
    {
        try
        {
            if (fragmentRegistry.containsKey(tag))
            {
                return (Fragment) fragmentRegistry.get(tag).newInstance();
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "serial" })
    public static final Map<String, Class> fragmentRegistry = new HashMap<String, Class>()
    {
        {
            put(ChildrenBrowserFragment.TAG, ChildrenBrowserFragment.class);
            put(ActivitiesFragment.TAG, ActivitiesFragment.class);
            put(MainMenuFragment.TAG, MainMenuFragment.class);
            put(AccountsFragment.TAG, AccountsFragment.class);
            put(KeywordSearch.TAG, KeywordSearch.class);
            put(FileExplorerFragment.TAG, FileExplorerFragment.class);

        }
    };

}
