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
package org.alfresco.mobile.android.application.activity;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.operations.batch.capture.DeviceCapture;

import android.os.Bundle;

public class MainActivityHelper
{

    public static final String TAG = MainActivityHelper.class.getName();

    private static final String PARAM_ACCOUNT = "account";

    private static final String PARAM_DISPLAY_FROM_SITE = "displayFromSite";

    private static final String PARAM_IMPORT_PARENT = "importParent";

    private static final String PARAM_FRAGMENT_QUEUE = "fragmentQueue";

    private static final String PARAM_STACK_CENTRAL = "stackCentral";

    private static final String PARAM_CAPTURE = "capture";

    private Bundle savedInstanceBundle = new Bundle();

    public MainActivityHelper()
    {
    }

    public MainActivityHelper(Bundle saveInstanceBundle)
    {
        this.savedInstanceBundle = saveInstanceBundle;
    }

    public static Bundle createBundle(Bundle outState, Stack<String> stackCentral, Account currentAccount,
            DeviceCapture capture, int fragmentQueue, Folder importParent)
    {
        Bundle savedInstanceBundle = new Bundle();

        String[] stringArray = Arrays.copyOf(stackCentral.toArray(), stackCentral.size(), String[].class);
        savedInstanceBundle.putStringArray(PARAM_STACK_CENTRAL, stringArray);
        savedInstanceBundle.putSerializable(PARAM_ACCOUNT, currentAccount);

        if (capture != null)
        {
            savedInstanceBundle.putSerializable(PARAM_CAPTURE, capture);
        }

        outState.putInt(PARAM_FRAGMENT_QUEUE, fragmentQueue);

        if (importParent != null)
        {
            savedInstanceBundle.putParcelable(PARAM_IMPORT_PARENT, importParent);
        }

        return savedInstanceBundle;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Account getCurrentAccount()
    {
        return (savedInstanceBundle.containsKey(PARAM_ACCOUNT)) ? (Account) savedInstanceBundle
                .getSerializable(PARAM_ACCOUNT) : null;
    }

    public Site getSite()
    {
        return (savedInstanceBundle.containsKey(PARAM_DISPLAY_FROM_SITE)) ? (Site) savedInstanceBundle
                .getSerializable(PARAM_DISPLAY_FROM_SITE) : null;
    }

    public Folder getFolder()
    {
        if (savedInstanceBundle.containsKey(PARAM_IMPORT_PARENT)) { return (Folder) savedInstanceBundle
                .getSerializable(PARAM_IMPORT_PARENT); }
        return null;
    }

    public Integer getFragmentQueue()
    {
        return (savedInstanceBundle.containsKey(PARAM_FRAGMENT_QUEUE)) ? (Integer) savedInstanceBundle
                .getSerializable(PARAM_FRAGMENT_QUEUE) : -1;
    }

    public DeviceCapture getDeviceCapture()
    {
        return (savedInstanceBundle.containsKey(PARAM_CAPTURE)) ? (DeviceCapture) savedInstanceBundle
                .getSerializable(PARAM_CAPTURE) : null;
    }

    public Stack<String> getStackCentral()
    {
        String[] d = savedInstanceBundle.getStringArray(PARAM_STACK_CENTRAL);
        Stack<String> stackCentral = new Stack<String>();
        if (d != null)
        {
            List<String> list = Arrays.asList(d);
            stackCentral.addAll(list);
        }
        return stackCentral;
    }

}
