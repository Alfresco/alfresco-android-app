/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.utils;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.fragments.browser.AddContentDialogFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;

public abstract class DeviceCapture implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Folder repositoryFolder = null;

    transient protected Context context = null;

    transient protected Activity parentActivity = null;

    protected File payload = null;
    protected String MIMEType = null;

    abstract public boolean hasDevice();

    abstract public boolean captureData();

    abstract protected void payloadCaptured(int requestCode, int resultCode, Intent data);

    public void capturedCallback(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == getRequestCode() && resultCode == Activity.RESULT_OK)
        {
            payloadCaptured(requestCode, resultCode, data);

            upload();
        }
    }

    public void setActivity(Activity parentActivity)
    {
        this.parentActivity = parentActivity;
    }

    DeviceCapture(Context ctxt)
    {
        this.context = ctxt;
    }

    DeviceCapture(Activity parentActivity, Folder repositoryFolder)
    {
        this.context = parentActivity;
        this.parentActivity = parentActivity;
        this.repositoryFolder = repositoryFolder;
    }

    private void upload()
    {
        FragmentTransaction ft = parentActivity.getFragmentManager().beginTransaction();
        Fragment prev = parentActivity.getFragmentManager().findFragmentByTag(AddContentDialogFragment.TAG);

        if (prev != null) ft.remove(prev);

        ft.addToBackStack(null);

        AddContentDialogFragment newFragment;
        
        if (MIMEType != null)
        {
            newFragment = AddContentDialogFragment.newInstance(repositoryFolder, payload, MIMEType);
        }
        else
        {
            newFragment = AddContentDialogFragment.newInstance(repositoryFolder, payload);
        }
        
        newFragment.show(ft, AddContentDialogFragment.TAG);
    }

    protected void finalize()
    {
        if (payload != null)
        {
            payload.delete();
            payload = null;
        }
    }

    public int getRequestCode()
    {
        return Math.abs(getClass().hashCode());
    }

    protected String createFilename(String prefix, String extension)
    {
        String timeStamp = new SimpleDateFormat("yyyyddMM_HHmmss").format(new Date());

        return prefix + timeStamp + "." + extension;
    }
}
