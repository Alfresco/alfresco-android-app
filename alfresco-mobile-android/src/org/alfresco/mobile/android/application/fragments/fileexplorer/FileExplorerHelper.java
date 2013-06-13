package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;

import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.MediaStore;

public final class FileExplorerHelper
{

    private FileExplorerHelper()
    {
    }

    private static final String FILEEXPLORER_PREFS = "org.alfresco.mobile.android.fileexplorer.preferences";

    private static final String FILEEXPLORER_DEFAULT = "org.alfresco.mobile.android.fileexplorer.preferences.default";

    public static void setSelection(Activity activity)
    {
        int itemPosition = 1;
        SharedPreferences prefs = activity.getSharedPreferences(FILEEXPLORER_PREFS, 0);
        prefs.edit().putInt(FILEEXPLORER_DEFAULT, itemPosition).commit();
    }

    public static void setSelection(Activity activity, File location)
    {
        int itemPosition = 1;
        if (location.equals(StorageManager.getDownloadFolder(activity, ((BaseActivity) activity).getCurrentAccount())))
        {
            itemPosition = 1;
        }
        else if (location.equals(Environment.getExternalStorageDirectory()))
        {
            itemPosition = 3;
        }

        SharedPreferences prefs = activity.getSharedPreferences(FILEEXPLORER_PREFS, 0);
        prefs.edit().putInt(FILEEXPLORER_DEFAULT, itemPosition).commit();
    }

    public static void setSelection(Activity activity, int mediatype)
    {
        int itemPosition = 1;
        switch (mediatype)
        {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                itemPosition = 8;
                break;

            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                itemPosition = 7;
                break;

            default:
                break;
        }

        SharedPreferences prefs = activity.getSharedPreferences(FILEEXPLORER_PREFS, 0);
        prefs.edit().putInt(FILEEXPLORER_DEFAULT, itemPosition).commit();
    }

    public static void displayNavigationMode(final Activity activity, int mode)
    {
        displayNavigationMode(activity, mode, true, 1);
    }

    public static void displayNavigationMode(final Activity activity, final int mode, final boolean backStack, int menuId)
    {
        activity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ShortCutFolderMenuAdapter adapter = new ShortCutFolderMenuAdapter(activity);

        OnNavigationListener mOnNavigationListener = new OnNavigationListener()
        {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId)
            {
                SharedPreferences prefs = activity.getSharedPreferences(FILEEXPLORER_PREFS, 0);
                int currentSelection = prefs.getInt(FILEEXPLORER_DEFAULT, 1);

                if (!backStack && itemPosition == currentSelection) { return true; }

                File currentLocation = null;
                int mediatype = -1;
                boolean thirdPartyApp = false;
                switch (itemPosition)
                {
                    case 1:
                        currentLocation = StorageManager.getDownloadFolder(activity,
                                ((BaseActivity) activity).getCurrentAccount());
                        break;
                    case 3:
                        currentLocation = Environment.getExternalStorageDirectory();
                        break;
                    case 5:
                        mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
                        break;
                    case 6:
                        mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
                        break;
                    case 7:
                        mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                        break;
                    case 8:
                        mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                        break;
                    case 10:
                        thirdPartyApp = true;
                        break;
                    default:
                        break;
                }

                if (!backStack)
                {
                    activity.getFragmentManager().popBackStack();
                }

                if (thirdPartyApp)
                {
                    if (activity instanceof PublicDispatcherActivity)
                    {
                        activity.setResult(PublicIntent.REQUESTCODE_FILEPICKER, new Intent(
                                IntentIntegrator.ACTION_PICK_FILE));
                        activity.finish();
                    }
                    return true;
                }
                else if (currentLocation != null)
                {
                    BaseFragment frag = FileExplorerFragment.newInstance(currentLocation, mode, true, itemPosition);
                    FragmentDisplayer.replaceFragment(activity, frag, DisplayUtils.getLeftFragmentId(activity),
                            FileExplorerFragment.TAG, false);
                }
                else if (mediatype >= 0)
                {
                    LibraryFragment frag = LibraryFragment.newInstance(mediatype, mode, true, itemPosition);
                    FragmentDisplayer.replaceFragment(activity, frag, DisplayUtils.getLeftFragmentId(activity),
                            LibraryFragment.TAG, false);
                }
                prefs.edit().putInt(FILEEXPLORER_DEFAULT, itemPosition).commit();

                return true;
            }

        };
        activity.getActionBar().setListNavigationCallbacks(adapter, mOnNavigationListener);
        SharedPreferences prefs = activity.getSharedPreferences(FILEEXPLORER_PREFS, 0);
        int currentSelection = menuId;
        //int currentSelection = prefs.getInt(FILEEXPLORER_DEFAULT, 1);
        activity.getActionBar().setSelectedNavigationItem(currentSelection);
    }
}
