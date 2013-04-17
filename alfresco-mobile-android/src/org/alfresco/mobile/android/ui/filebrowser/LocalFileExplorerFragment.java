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
package org.alfresco.mobile.android.ui.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.filebrowser.FolderAdapter.FolderBookmark;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;
import org.alfresco.mobile.android.application.manager.StorageManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Spinner;
import android.widget.TextView;


public abstract class LocalFileExplorerFragment extends BaseListFragment implements LoaderCallbacks<List<File>>, OnClickListener
{

    public static final String TAG = "LocalFileExplorerFragment";

    public static final String ARGUMENT_FOLDER = "folder";

    public static final String ARGUMENT_FOLDERPATH = "folderPath";

    public static final String MODE = "mode";
    
    public static final int MODE_LISTING = 1;

    public static final int MODE_PICK_FILE = 2;
    
    public static final int MODE_IMPORT = 3;

    protected List<File> selectedItems = new ArrayList<File>(1);

    private int titleId;

    enum fileLocation { INITIAL_FOLDER, DOWNLOAD_FOLDER, SDCARD_ROOT };
    
    private fileLocation currentLocation = fileLocation.INITIAL_FOLDER;
    
    private File userLocation = null;
    
    private File downloadLocation = null;
 
    private File sdCardLocation = null;
 
    private File cameraLocation;
    
    private TextView pathText = null;

    private HorizontalScrollView pathTextScroller = null;
        
    private boolean inMultiSelect = false;
    
    
    public LocalFileExplorerFragment()
    {
        loaderId = LocalFileExplorerLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_child;
    }

    public static Bundle createBundleArgs(File folder)
    {
        return createBundleArgs(folder, null);
    }

    public static Bundle createBundleArgs(String folderPath)
    {
        return createBundleArgs(null, folderPath);
    }

    public static Bundle createBundleArgs(File folder, String path)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_FOLDER, folder);
        args.putSerializable(ARGUMENT_FOLDERPATH, path);
        return args;
    }
    
    @Override
    public void onStart()
    {
        retrieveTitle();
        if (getDialog() != null)
        {
            getDialog().setTitle(titleId);
        }
        else
        {
            getActivity().getActionBar().show();
            getActivity().setTitle(titleId);
        }
        super.onStart();
    }
    
    private void retrieveTitle(){
        Bundle b = getArguments();
        if (b.getInt(MODE) == MODE_LISTING)
        {
            titleId = R.string.menu_downloads;
        }
        else if (b.getInt(MODE) == MODE_PICK_FILE)
        {
            titleId = R.string.upload_pick_document;
        }
    }
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    { 
        View toolButton = null;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.app_folderbookmark_list, null);
        View toolsGroup = v.findViewById(R.id.tools_group);
        
        //Set to a decent size for a file browser, using minimum Android 'NORMAL' screen size as smallest possible.
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        v.setMinimumHeight(size.y);
        v.setMinimumWidth(size.x);
        
        pathTextScroller  = (HorizontalScrollView)v.findViewById(R.id.pathTextScrollView);
        pathTextScroller.setVisibility(View.VISIBLE);
        pathText = (TextView)v.findViewById(R.id.pathText);
        
        if (toolsGroup != null)
        {
            toolsGroup.setVisibility(View.VISIBLE);
            
            toolButton = toolsGroup.findViewById(R.id.toolbutton1);
            ((ImageView)toolButton).setImageResource(android.R.drawable.ic_menu_revert);
            toolButton.setVisibility(View.VISIBLE);
            toolButton.setOnClickListener(this);
            
            Context c = getActivity();
            File folder = StorageManager.getDownloadFolder(c, SessionUtils.getAccount(c).getUrl(), SessionUtils.getAccount(c).getUsername());
            if (folder != null)
            {
                //Location buttons that require the presence of the SD card...
                //Download button
                /*toolButton = toolsGroup.findViewById(R.id.toolbutton2);
                ((ImageView)toolButton).setImageResource(R.drawable.ic_download_dark);
                toolButton.setVisibility(View.VISIBLE);
                toolButton.setOnClickListener(this);
                //SD button
                toolButton = toolsGroup.findViewById(R.id.toolbutton3);
                ((ImageView)toolButton).setImageResource(R.drawable.ic_repository_dark);
                toolButton.setVisibility(View.VISIBLE);
                toolButton.setOnClickListener(this);
                */
                
                //Now we know SD is available, get relevant paths.
                currentLocation = fileLocation.INITIAL_FOLDER;
                userLocation = folder;
                downloadLocation = folder;
                sdCardLocation = Environment.getExternalStorageDirectory();
                cameraLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                resetPathText();
                
                List<FolderBookmark> folders = new ArrayList<FolderBookmark>();
                
                FolderAdapter.FolderBookmark downloadFolder = new FolderBookmark();
                downloadFolder.icon = R.drawable.ic_download_dark;
                downloadFolder.name = "Download folder";
                downloadFolder.location = downloadLocation.getPath();
                folders.add(downloadFolder);
                
                if (cameraLocation != null)
                {
                    FolderAdapter.FolderBookmark DCIMFolder = new FolderBookmark();
                    DCIMFolder.icon = R.drawable.ic_repository_dark;
                    DCIMFolder.name = "Camera folder";
                    DCIMFolder.location = cameraLocation.getPath();
                    folders.add(DCIMFolder);
                }
                
                FolderAdapter.FolderBookmark sdFolder = new FolderBookmark();
                sdFolder.icon = R.drawable.ic_repository_dark;
                sdFolder.name = "SD card";
                sdFolder.location = sdCardLocation.getPath();
                folders.add(sdFolder);
                
                FolderAdapter folderAdapter = new FolderAdapter(getActivity(), R.layout.app_account_list_row, folders);
                
                Spinner s = (Spinner) toolsGroup.findViewById(R.id.folderspinner);
                s.setVisibility(View.VISIBLE);
                s.setAdapter(folderAdapter);
                s.setSelection(0);
                s.setOnItemSelectedListener(new OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> a0, View v, int a2, long a3)
                    {
                        Bundle args = getArguments();
                        String itemDesc = ((TextView)v.findViewById(R.id.toptext)).getText().toString();
                        
                        if (getString(R.string.sdcard_desc).compareTo(itemDesc) == 0)
                        {   
                            args.putSerializable(ARGUMENT_FOLDER, sdCardLocation);
                            args.putSerializable(ARGUMENT_FOLDERPATH, sdCardLocation.getPath());
                            userLocation = sdCardLocation;
                            resetPathText();
                            refresh();
                        }
                        else
                        if (getString(R.string.download_folder_desc).compareTo(itemDesc) == 0)
                        {        
                            args.putSerializable(ARGUMENT_FOLDER, downloadLocation);
                            args.putSerializable(ARGUMENT_FOLDERPATH, downloadLocation.getPath());
                            userLocation = downloadLocation;
                            resetPathText();
                            refresh();
                        }
                        else
                        if (getString(R.string.camera_folder_desc).compareTo(itemDesc) == 0)
                        {                
                            args.putSerializable(ARGUMENT_FOLDER, cameraLocation);
                            args.putSerializable(ARGUMENT_FOLDERPATH, cameraLocation.getPath());
                            userLocation = cameraLocation;
                            resetPathText();
                            refresh();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0)
                    {
                    }
                } );
            }
        }
        
        init(v, emptyListMessageId);
        
        //Override list item click
        lv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id)
            {
                LinearLayout entry = (LinearLayout)v;
                
                TextView filenameView = (TextView) entry.findViewById(R.id.toptext);
                CharSequence filename = filenameView.getText();
                File current = new File (userLocation.getPath() + "/" + filename);
                
                if (current.isDirectory())
                {
                    Bundle args = getArguments();
                    args.putSerializable(ARGUMENT_FOLDER, current);
                    args.putSerializable(ARGUMENT_FOLDERPATH, current.getPath());
                    userLocation = current;
                    resetPathText();
                    refresh();
                }
                else
                {
                    //Fulfill base class behavior
                    savePosition();
                    onListItemClick((ListView) l, v, position, id);
                }
            }
        });

             
        v.setOnKeyListener( new OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    if (inMultiSelect)
                    {
                        inMultiSelect = false;
                        return true;
                    }
                }
                return false;
            }
        } );
                
        setRetainInstance(true);
        if (initLoader)
        {
            continueLoading(loaderId, callback);
        }
        
        retrieveTitle();
        return new AlertDialog.Builder(getActivity()).setTitle(titleId).setView(v).create();
    }
    
    String getRelativePath()
    {
        String path = userLocation.getPath();
        String newPath = null;
        
        if (path.startsWith(downloadLocation.getPath()))
        {
            newPath = StorageManager.DLDIR + path.substring(downloadLocation.getPath().length());
            return newPath;
        }
        else
        if (path.startsWith(sdCardLocation.getPath()))
        {
            newPath = path.substring(sdCardLocation.getPath().length());
            
            return "SD" + newPath;
        }
        else
            return path;
    }

    void resetPathText ()
    {
        pathText.setText(getRelativePath());
        
        pathTextScroller.post(new Runnable()
                                {            
                                    @Override
                                    public void run() 
                                    {
                                        pathTextScroller.fullScroll(View.FOCUS_RIGHT);              
                                    }
                                });
    }
    
    @Override
    public void onClick(View v)
    {
        Bundle args = getArguments();
        
        switch (v.getId())
        {
            case R.id.toolbutton1:  //Up folder button
                
                //SD card or Downloads are top level folders, don't allow delving into system folders.
//                if (userLocation.getPath().compareTo(sdCardLocation.getPath()) != 0 &&
//                    userLocation.getPath().compareTo(downloadLocation.getPath()) != 0)
                {
                    File upFolder = userLocation.getParentFile();
                    
                    if (upFolder != null)
                    {
                        args.putSerializable(ARGUMENT_FOLDER, upFolder);
                        args.putSerializable(ARGUMENT_FOLDERPATH, upFolder.getPath());
                        userLocation = upFolder;
                        resetPathText();
                        refresh();
                    }
                }
                break;
                
            case R.id.toolbutton2:  //Download folder button 
                
                args.putSerializable(ARGUMENT_FOLDER, downloadLocation);
                args.putSerializable(ARGUMENT_FOLDERPATH, downloadLocation.getPath());
                userLocation = downloadLocation;
                resetPathText();
                refresh();
                break;
                
            case R.id.toolbutton3:  //SD Card button
                
                args.putSerializable(ARGUMENT_FOLDER, sdCardLocation);
                args.putSerializable(ARGUMENT_FOLDERPATH, sdCardLocation.getPath());
                userLocation = sdCardLocation;
                resetPathText();
                refresh();
                break;
                
            case R.id.toolbutton4:  break;
            case R.id.toolbutton5:  break;
            default:                break;
        }
    }
    
    protected int getMode()
    {
        Bundle b = getArguments();
        return b.getInt(MODE);
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle ba)
    {
        if (!hasmore && lv != null)
        {
            setListShown(false);
        }

        // Case Init & case Reload
        bundle = (ba == null) ? getArguments() : ba;

        ListingContext lc = null, lcorigin = null;
        File f = null;
        String path = null;

        if (bundle != null)
        {
            f = (File) bundle.getSerializable(ARGUMENT_FOLDER);
            path = bundle.getString(ARGUMENT_FOLDERPATH);
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
        }
        calculateSkipCount(lc);

        Loader<List<File>> loader = null;
        if (path != null)
        {
            title = path.substring(path.lastIndexOf("/") + 1, path.length());
            loader = new LocalFileExplorerLoader(getActivity(), new File(path));
        }
        else if (f != null)
        {
            title = f.getName();
            loader = new LocalFileExplorerLoader(getActivity(), f);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> results)
    {

        if (adapter == null)
        {
            adapter = new LocalFileExplorerAdapter(getActivity(), R.layout.sdk_list_row, new ArrayList<File>(0),
                    selectedItems);
        }

        PagingResult<File> pagingResultFiles = new PagingResultImpl<File>(results, false, results.size());
        displayPagingData(pagingResultFiles, loaderId, callback);
    }

    @Override
    public void onLoaderReset(Loader<List<File>> arg0)
    {
        // TODO Auto-generated method stub
    }

    public void refresh()
    {
        refresh(loaderId, callback);
    }
}
