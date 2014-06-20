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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.mobile.android.application.security.EncryptionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * @author jpascal
 */
public class FileExplorerLoader extends AsyncTaskLoader<List<File>>
{
    private File folder;

    private Context context;

    public static final int ID = FileExplorerLoader.class.hashCode();

    /**
     * Get all children from a the specified folder.
     * 
     * @param context
     * @param repoSession
     * @param folder
     * @param lcontext
     */
    public FileExplorerLoader(Context context, File folder)
    {
        super(context);
        this.context = context;
        this.folder = folder;
    }

    @Override
    public List<File> loadInBackground()
    {
        try
        {
            ArrayList<File> fileList = new ArrayList<File>();
            if (folder == null) { return fileList; }

            if (folder.isDirectory())
            {
                File[] childs = folder.listFiles();
                if (childs != null)
                {
                    for (File child : childs)
                    {
                        if (!child.isHidden() && !child.getName().startsWith(".")
                                && !EncryptionUtils.isEncrypted(child.getName()))
                        {
                            fileList.add(child);
                        }
                    }
                }

                Collections.sort(fileList, new FileComparator(true));

                return fileList;
            }
        }
        catch (Exception e)
        {
            MessengerManager.showLongToast(context, e.getMessage());
        }

        return null;
    }

    public File getFolder()
    {
        return folder;
    }
}
