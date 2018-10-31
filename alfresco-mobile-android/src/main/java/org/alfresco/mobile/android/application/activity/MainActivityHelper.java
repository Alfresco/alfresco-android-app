/*******************************************************************************
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
import org.alfresco.mobile.android.application.capture.AudioCapture;
import org.alfresco.mobile.android.application.capture.DeviceCapture;
import org.alfresco.mobile.android.application.capture.PhotoCapture;
import org.alfresco.mobile.android.application.capture.VideoCapture;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

import android.os.Bundle;

import com.google.gson.Gson;

public class MainActivityHelper
{

    public static final String TAG = MainActivityHelper.class.getName();

    private static final String ARGUMENT_ACCOUNT = "account";

    private static final String ARGUMENT_IMPORT_PARENT = "importParent";

    private static final String ARGUMENT_FRAGMENT_QUEUE = "fragmentQueue";

    private static final String ARGUMENT_STACK_CENTRAL = "stackCentral";

    private static final String ARGUMENT_CAPTURE = "capture";

    private static final String ARGUMENT_CAPTURE_TYPE = "captureType";

    private static final String ARGUMENT_SESSION_STATE = "sessionState";

    private static final String ARGUMENT_SESSION_STATE_ERROR_ID = "sessionStateErrorMessageId";

    private Bundle savedInstanceBundle = new Bundle();

    public MainActivityHelper()
    {
    }

    public MainActivityHelper(Bundle saveInstanceBundle)
    {
        this.savedInstanceBundle = saveInstanceBundle;
    }

    public static Bundle createBundle(Bundle outState, AlfrescoAccount currentAccount,
            DeviceCapture capture, String deviceCaptureType, int fragmentQueue, Folder importParent, int sessionState,
            int sessionStateErrorMessageId)
    {
        Bundle savedInstanceBundle = new Bundle();

        Gson gson = new Gson();
        savedInstanceBundle.putString(ARGUMENT_ACCOUNT, gson.toJson(currentAccount));

        if (capture != null)
        {
            savedInstanceBundle.putString(ARGUMENT_CAPTURE, gson.toJson(capture));
        }

        if (deviceCaptureType != null) {
            savedInstanceBundle.putString(ARGUMENT_CAPTURE_TYPE, gson.toJson(capture));
        }

        outState.putInt(ARGUMENT_FRAGMENT_QUEUE, fragmentQueue);

        if (importParent != null)
        {
            savedInstanceBundle.putString(ARGUMENT_IMPORT_PARENT, gson.toJson(importParent));
        }

        outState.putInt(ARGUMENT_SESSION_STATE, sessionState);
        outState.putInt(ARGUMENT_SESSION_STATE_ERROR_ID, sessionStateErrorMessageId);

        return savedInstanceBundle;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoAccount getCurrentAccount()
    {
        return (savedInstanceBundle.containsKey(ARGUMENT_ACCOUNT)) ? new Gson().fromJson(savedInstanceBundle
                .getString(ARGUMENT_ACCOUNT),AlfrescoAccount.class) : null;
    }

    public Folder getFolder()
    {
        return (savedInstanceBundle.containsKey(ARGUMENT_IMPORT_PARENT)) ? new Gson().fromJson(savedInstanceBundle
                .getString(ARGUMENT_IMPORT_PARENT), Folder.class) : null;
    }

    public Integer getFragmentQueue()
    {
        return (savedInstanceBundle.containsKey(ARGUMENT_FRAGMENT_QUEUE)) ? savedInstanceBundle
                .getInt(ARGUMENT_FRAGMENT_QUEUE) : -1;
    }

    public Integer getSessionState()
    {
        return (savedInstanceBundle.containsKey(ARGUMENT_SESSION_STATE))
                ? savedInstanceBundle.getInt(ARGUMENT_SESSION_STATE) : -1;
    }

    public Integer getSessionErrorMessageId()
    {
        return (savedInstanceBundle.containsKey(ARGUMENT_SESSION_STATE_ERROR_ID))
                ? savedInstanceBundle.getInt(ARGUMENT_SESSION_STATE_ERROR_ID) : -1;
    }

    public DeviceCapture getDeviceCapture()
    {
        String captureType = getDeviceCaptureType();
        if (captureType == null) {
            return null;
        }
        
        if (captureType.equals(PhotoCapture.class.getSimpleName())) {
            return (savedInstanceBundle.containsKey(ARGUMENT_CAPTURE)) ? new Gson().fromJson(savedInstanceBundle
                    .getString(ARGUMENT_CAPTURE), PhotoCapture.class) : null;
        } else if (captureType.equals(AudioCapture.class.getSimpleName())) {
            return (savedInstanceBundle.containsKey(ARGUMENT_CAPTURE)) ? new Gson().fromJson(savedInstanceBundle
                    .getString(ARGUMENT_CAPTURE), AudioCapture.class) : null;
        } else if (captureType.equals(VideoCapture.class.getSimpleName())) {
            return (savedInstanceBundle.containsKey(ARGUMENT_CAPTURE)) ? new Gson().fromJson(savedInstanceBundle
                    .getString(ARGUMENT_CAPTURE), VideoCapture.class) : null;
        } else {
            return null;
        }
    }

    private String getDeviceCaptureType() {
        return (savedInstanceBundle.containsKey(ARGUMENT_CAPTURE_TYPE))
                ? savedInstanceBundle.getString(ARGUMENT_CAPTURE_TYPE) : null;
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
