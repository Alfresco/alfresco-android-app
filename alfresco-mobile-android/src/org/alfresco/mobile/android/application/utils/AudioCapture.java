/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.alfresco.mobile.android.application.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.mobile.android.api.model.Folder;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;


public class AudioCapture extends DeviceCapture
{	
    private static final long serialVersionUID = 1L;

    
    public AudioCapture(Activity parent, Folder folder) 
	{
		super (parent, folder);
	}

	@Override
	public boolean hasDevice()
	{
		return (parentActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE));
	}
	
	@Override
	public boolean captureData() 
	{
		if (hasDevice())
		{
			try 
			{
				Intent intent = new Intent (MediaStore.Audio.Media.RECORD_SOUND_ACTION);
				parentActivity.startActivityForResult (intent, getRequestCode() );
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		else
			return false;
	}

	@Override
	protected void payloadCaptured (int requestCode, int resultCode, Intent data)
	{
		Uri savedUri = data.getData();
		
		try 
		{
			String filePath 	= 	getAudioFilePathFromUri (savedUri);
			String newFilePath 	= 	Environment.getExternalStorageDirectory().getPath() +
									"/AndroidSoundCapture." + 
									filePath.substring(filePath.lastIndexOf(".")+1);
									
	        copyFile (filePath, newFilePath);
	        
	        parentActivity.getContentResolver().delete (savedUri, null, null);  
	        (new File(filePath)).delete();
	        
	        payload = new File (newFilePath);
	    } 
		catch (IOException e)
	    {
			e.printStackTrace();
	    }
	}

	private String getAudioFilePathFromUri (Uri uri)
	{
		Cursor cursor = parentActivity.getContentResolver().query (uri, null, null, null, null);
		cursor.moveToFirst();
		int index = cursor.getColumnIndex (MediaStore.Audio.AudioColumns.DATA);
		return cursor.getString (index);
	}
	
	private void copyFile (String fileName, String newFileName) throws IOException
	{
	    InputStream in = new FileInputStream (fileName);
	    OutputStream out = new FileOutputStream (newFileName);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0)
	    {
	        out.write (buf, 0, len);
	    }
	    
	    in.close();
	    out.close();
	}
}
