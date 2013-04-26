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
package org.alfresco.mobile.android.application.fragments.editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.StorageManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.webkit.WebView.FindListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;


public class TextEditorFragment extends DialogFragment implements OnClickListener, LoaderCallbacks<LoaderResult<String>>, OnKeyListener, OnEditorActionListener
{
    public static final String TAG = "TextEditorFragment";
   
    protected List<File> selectedItems = new ArrayList<File>(1);

    private static final String PARAM_FILE = "file";
    
    private String title;

    private int loaderId = 0;
    
    private File currentFile = null;

    private boolean tempFile = false;
    
    private View v;
    
    boolean changed = false;
    
    
    public TextEditorFragment()
    {
        loaderId = TextEditorLoader.ID;
    }
    
    public static TextEditorFragment editFile(String filePath)
    {
        File myFile = new File(filePath);
        return editFile (myFile);
    }
    
    public static TextEditorFragment editFile(File myFile)
    {
        if (myFile.length() < Integer.MAX_VALUE)
        {
            TextEditorFragment fragment = new TextEditorFragment();
            Bundle b = new Bundle();
            b.putSerializable(PARAM_FILE, myFile);
            fragment.setArguments(b);
            return fragment;
        }
        else
            return null;
    }
    
    @Override
    public void onStart()
    {
        retrieveTitle();
        if (getDialog() != null)
        {
            getDialog().setTitle(title);
        }
        else
        {
            getActivity().getActionBar().show();
            getActivity().setTitle(title);
        }
        
        loaderId = TextEditorLoader.ID + ((File)getArguments().getSerializable(PARAM_FILE)).getPath().hashCode();
        Loader<Object> loader = getActivity().getLoaderManager().getLoader(loaderId);
        if (loader == null)
        {
            getActivity().getLoaderManager().initLoader(loaderId, getArguments(), this);
            getActivity().getLoaderManager().getLoader(loaderId).forceLoad();
        }
        else
        {
            getActivity().getLoaderManager().initLoader(loaderId, getArguments(), this);
        }
        
        super.onStart();
    }
    
    private void retrieveTitle()
    {
        Bundle b = getArguments();
        
        currentFile = (File) getArguments().getSerializable(PARAM_FILE);
        
        if (StorageManager.isTempFile(getActivity(), currentFile))
        {
            title = getString(R.string.new_file);
            tempFile  = true;
        }
        else
            title = currentFile.getName();
    }
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    { 
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        v = inflater.inflate(R.layout.app_text_editor, null);
        
        //Set to a decent size for a file browser, using minimum Android 'NORMAL' screen size as smallest possible.
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        v.setMinimumHeight(size.y);
        v.setMinimumWidth(size.x);
        
        setRetainInstance(true);
        
        retrieveTitle();
        
        EditText view = (EditText) v.findViewById(R.id.texteditor);
        view.setOnKeyListener(this);
        view.setOnEditorActionListener(this);
        
        return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v).create();
    }
    
    @Override
    public void onClick(View clickView)
    {
        Bundle args = getArguments();
        
        switch (clickView.getId())
        {
            case R.id.savebutton: 
                
                TextView view = (TextView) v.findViewById(R.id.texteditor);
                OutputStream sourceFile;
                try
                {
                    sourceFile = new FileOutputStream(currentFile);
                    sourceFile.write(view.getText().toString().getBytes());
                    sourceFile.close();
                }
                catch (FileNotFoundException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                break;
                
             default:
                 break;
        }
    }
    
    @Override
    public Loader<LoaderResult<String>> onCreateLoader(int id, Bundle ba)
    {
        // Case Init & case Reload
        Bundle bundle = (ba == null) ? getArguments() : ba;

        if (bundle != null)
        {
            currentFile = (File) bundle.getSerializable(PARAM_FILE);
        }
        
        Loader<LoaderResult<String>> loader = null;
        
        loader = new TextEditorLoader(this, currentFile);
        
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<String>> loader, LoaderResult<String> results)
    {
        TextView view = (TextView) v.findViewById(R.id.texteditor);
        
        try
        {
            view.setText(results.getData());
        }
        catch (Exception e)
        {
            Toast.makeText(getActivity(), getString(R.string.textfile_populate_failed), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<String>> arg0)
    {
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
        //TODO set changed = true appropriately
        
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
    {
        //TODO set changed = true appropriately
                
        return false;
    }
}
