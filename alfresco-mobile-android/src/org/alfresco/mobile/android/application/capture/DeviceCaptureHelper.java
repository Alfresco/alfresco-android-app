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
package org.alfresco.mobile.android.application.capture;

import java.io.File;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.capture.FujitsuCapture;

public final class DeviceCaptureHelper
{

    private DeviceCaptureHelper()
    {
    }

    public static DeviceCapture createDeviceCapture(BaseActivity c, int id)
    {
        DeviceCapture capture = null;

        Folder parentRepositoryFolder = null;
        File parentFolder = null;

        if (((DocumentFolderBrowserFragment) c.getFragment(DocumentFolderBrowserFragment.TAG)) != null)
        {
            parentRepositoryFolder = ((DocumentFolderBrowserFragment) c.getFragment(DocumentFolderBrowserFragment.TAG))
                    .getParent();
        }

        if (((FileExplorerFragment) c.getFragment(FileExplorerFragment.TAG)) != null)
        {
            parentFolder = ((FileExplorerFragment) c.getFragment(FileExplorerFragment.TAG)).getParent();
        }

        switch (id)
        {
            case MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO:
                capture = new PhotoCapture(c, parentRepositoryFolder, parentFolder);
                break;
            case MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO:
                capture = new VideoCapture(c, parentRepositoryFolder, parentFolder);
                break;
            case MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO:
                capture = new AudioCapture(c, parentRepositoryFolder, parentFolder);
                break;
            case MenuActionItem.MENU_DEVICE_SCAN_DOCUMENT:
                capture = new FujitsuCapture(c, parentRepositoryFolder, parentFolder);
            default:
                break;
        }
        if (capture != null)
        {
            capture.captureData();
        }

        return capture;
    }
}
