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
package org.alfresco.mobile.android.application.activity;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.application.capture.DeviceCapture;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

import android.os.Bundle;

public class MainActivityHelper
{

    public static final String TAG = MainActivityHelper.class.getName();

    private static final String ARGUMENT_ACCOUNT = "account";

    private static final String ARGUMENT_DISPLAY_FROM_SITE = "displayFromSite";

    private static final String ARGUMENT_IMPORT_PARENT = "importParent";

    private static final String ARGUMENT_FRAGMENT_QUEUE = "fragmentQueue";

    private static final String ARGUMENT_STACK_CENTRAL = "stackCentral";

    private static final String ARGUMENT_CAPTURE = "capture";

    private Bundle savedInstanceBundle = new Bundle();

    public MainActivityHelper()
    {
    }

    public MainActivityHelper(Bundle saveInstanceBundle)
    {
        this.savedInstanceBundle = saveInstanceBundle;
    }

    public static Bundle createBundle(Bundle outState, AlfrescoAccount currentAccount,
            DeviceCapture capture, int fragmentQueue, Folder importParent)
    {
        Bundle savedInstanceBundle = new Bundle();

        savedInstanceBundle.putSerializable(ARGUMENT_ACCOUNT, currentAccount);

        if (capture != null)
        {
            savedInstanceBundle.putSerializable(ARGUMENT_CAPTURE, capture);
        }

        outState.putInt(ARGUMENT_FRAGMENT_QUEUE, fragmentQueue);

        if (importParent != null)
        {
            savedInstanceBundle.putParcelable(ARGUMENT_IMPORT_PARENT, importParent);
        }

        return savedInstanceBundle;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoAccount getCurrentAccount()
    {
        return (savedInstanceBundle.containsKey(ARGUMENT_ACCOUNT)) ? (AlfrescoAccount) savedInstanceBundle
                .getSerializable(ARGUMENT_ACCOUNT) : null;
    }

    public Site getSite()
    {
        return (savedInstanceBundle.containsKey(ARGUMENT_DISPLAY_FROM_SITE)) ? (Site) savedInstanceBundle
                .getSerializable(ARGUMENT_DISPLAY_FROM_SITE) : null;
    }

    public Folder getFolder()
    {
        if (savedInstanceBundle.containsKey(ARGUMENT_IMPORT_PARENT)) { return (Folder) savedInstanceBundle
                .getSerializable(ARGUMENT_IMPORT_PARENT); }
        return null;
    }

    public Integer getFragmentQueue()
    {
        return (savedInstanceBundle.containsKey(ARGUMENT_FRAGMENT_QUEUE)) ? (Integer) savedInstanceBundle
                .getSerializable(ARGUMENT_FRAGMENT_QUEUE) : -1;
    }

    public DeviceCapture getDeviceCapture()
    {
        return (savedInstanceBundle.containsKey(ARGUMENT_CAPTURE)) ? (DeviceCapture) savedInstanceBundle
                .getSerializable(ARGUMENT_CAPTURE) : null;
    }

    public Stack<String> getStackCentral()
    {
        String[] d = savedInstanceBundle.getStringArray(ARGUMENT_STACK_CENTRAL);
        Stack<String> stackCentral = new Stack<String>();
        if (d != null)
        {
            List<String> list = Arrays.asList(d);
            stackCentral.addAll(list);
        }
        return stackCentral;
    }

}
