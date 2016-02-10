/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.capture;

import java.io.File;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;

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

        if ((c.getFragment(DocumentFolderBrowserFragment.TAG)) != null)
        {
            parentRepositoryFolder = ((DocumentFolderBrowserFragment) c.getFragment(DocumentFolderBrowserFragment.TAG))
                    .getParent();
        }

        if ((c.getFragment(FileExplorerFragment.TAG)) != null)
        {
            parentFolder = ((FileExplorerFragment) c.getFragment(FileExplorerFragment.TAG)).getParent();
        }

        String analyzeId = null;

        switch (id)
        {

            case R.id.menu_device_capture_camera_photo:
                capture = new PhotoCapture(c, parentRepositoryFolder, parentFolder);
                analyzeId = AnalyticsManager.ACTION_TAKE_PHOTO;
                break;
            case R.id.menu_device_capture_camera_video:
                capture = new VideoCapture(c, parentRepositoryFolder, parentFolder);
                analyzeId = AnalyticsManager.ACTION_RECORD_VIDEO;
                break;
            case R.id.menu_device_capture_mic_audio:
                capture = new AudioCapture(c, parentRepositoryFolder, parentFolder);
                analyzeId = AnalyticsManager.ACTION_RECORD_AUDIO;
                break;
            default:
                break;
        }
        if (capture != null)
        {
            AnalyticsHelper.reportOperationEvent(c, AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                    AnalyticsManager.ACTION_QUICK_ACTIONS, analyzeId, 1, false);

            capture.captureData();
        }

        return capture;
    }
}
