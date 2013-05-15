package org.alfresco.mobile.android.application.integration.capture;

import java.io.File;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;

public class DeviceCaptureHelper
{

    public static DeviceCapture createDeviceCapture(BaseActivity c, int id)
    {
        DeviceCapture capture = null;

        Folder parentRepositoryFolder = null;
        File parentFolder = null;

        if (((ChildrenBrowserFragment) c.getFragment(ChildrenBrowserFragment.TAG)) != null)
        {
            parentRepositoryFolder = ((ChildrenBrowserFragment) c.getFragment(ChildrenBrowserFragment.TAG)).getParent();
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
            default:
                break;
        }
        capture.captureData();
        
        return capture;
    }
}
