package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class FileExplorerMenuFragment extends BaseFragment
{
    public static final String TAG = FileExplorerMenuFragment.class.getName();

    private View rootView = null;

    private View currentSelectedButton = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public static BaseFragment newInstance()
    {
        return new FileExplorerMenuFragment();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.app_fileexplorer_menu, container, false);
        initClickListener(rootView);

        Button b = (Button) rootView.findViewById(R.id.shortcut_alfresco_downloads);
        b.performClick();

        return rootView;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void initClickListener(View rootView)
    {
        for (Integer buttonId : FILEEXPLORER_SHORTCUTS)
        {
            rootView.findViewById(buttonId).setOnClickListener(menuClickListener);
        }
    }

    private static final List<Integer> FILEEXPLORER_SHORTCUTS = new ArrayList<Integer>(7)
    {
        private static final long serialVersionUID = 1L;

        {
            add(R.id.shortcut_alfresco_downloads);

            add(R.id.shortcut_local_sdcard);
            add(R.id.shortcut_local_downloads);
            
            add(R.id.shortcut_library_office);
            add(R.id.shortcut_library_audios);
            add(R.id.shortcut_library_videos);
            add(R.id.shortcut_library_images);
        }
    };

    private OnClickListener menuClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            File currentLocation = null;
            int mediatype = -1;
            switch (v.getId())
            {
                case R.id.shortcut_alfresco_downloads:
                    currentLocation = StorageManager.getDownloadFolder(getActivity(),
                            ((BaseActivity) getActivity()).getCurrentAccount());
                    break;
                /*case R.id.shortcut_alfresco_sync:
                    currentLocation = StorageManager.getDownloadFolder(getActivity(),
                            ((BaseActivity) getActivity()).getCurrentAccount());
                    break;*/
                case R.id.shortcut_local_sdcard:
                    currentLocation = Environment.getExternalStorageDirectory();
                    break;
                case R.id.shortcut_local_downloads:
                    currentLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    break;
                case R.id.shortcut_library_office:
                    mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
                    break;
                case R.id.shortcut_library_audios:
                    mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
                    break;
                case R.id.shortcut_library_videos:
                    mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                    break;
                case R.id.shortcut_library_images:
                    mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                    break;
                default:
                    break;
            }

            if (currentSelectedButton != null)
            {
                UIUtils.setBackground(currentSelectedButton, FileExplorerMenuFragment.this.getResources()
                        .getDrawable(R.drawable.btn_default_holo_light_underline));
            }

            if (currentLocation != null)
            {
                ((MainActivity) getActivity()).addLocalFileNavigationFragment(currentLocation);
            }
            else if (mediatype >= 0)
            {
                ((MainActivity) getActivity()).addLocalFileNavigationFragment(mediatype);
            }

            UIUtils.setBackground(
                    v,
                    FileExplorerMenuFragment.this.getResources().getDrawable(
                            R.drawable.btn_default_focused_holo_light));
            currentSelectedButton = v;
        }
    };

}
