/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.encryption;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.AbstractBaseLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class EncryptionLoader extends AbstractBaseLoader<LoaderResult<File>>
{
    /** Unique EncryptionLoader identifier. */
    public static final int ID = EncryptionLoader.class.hashCode();

    private static final String TAG = "EncryptionLoader";

    private File file;

    private boolean doEncrypt;

    private File copiedFile;

    public EncryptionLoader(Fragment fr, AlfrescoSession session, File file, boolean doEncrypt)
    {
        super(fr.getActivity());
        this.session = session;
        this.file = file;
        this.doEncrypt = doEncrypt;
    }

    public EncryptionLoader(Fragment fr, AlfrescoSession session, File file, boolean doEncrypt, File copiedFile)
    {
        super(fr.getActivity());
        this.session = session;
        this.file = file;
        this.doEncrypt = doEncrypt;
        this.copiedFile = copiedFile;
    }

    @Override
    public LoaderResult<File> loadInBackground()
    {
        LoaderResult<File> result = new LoaderResult<File>();
        File encryptedFile = null;
        try
        {
            if (copiedFile != null)
            {
                IOUtils.copyFile(copiedFile.getPath(), file.getPath());
                if (CipherUtils.encryptFile(getContext(), file.getPath(), true))
                {
                    encryptedFile = new File(file.getPath());
                }
            }
            else if (doEncrypt)
            {
                if (CipherUtils.encryptFile(getContext(), file.getPath(), true))
                {
                    encryptedFile = new File(file.getPath());
                }
            }
            else
            {
                if (CipherUtils.isEncrypted(getContext(), file.getPath(), true))
                {
                    if (CipherUtils.decryptFile(getContext(), file.getPath()))
                    {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                                .putString("RequiresEncrypt", file.getPath()).commit();
                        encryptedFile = file;
                    }
                }
                else
                {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                            .putString("RequiresEncrypt", file.getPath()).commit();
                    encryptedFile = file;
                }

            }
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(encryptedFile);

        return result;
    }
    
    public File getFile()
    {
        return file;
    }

    public boolean isDoEncrypt()
    {
        return doEncrypt;
    }

    public File getCopiedFile()
    {
        return copiedFile;
    }

}
