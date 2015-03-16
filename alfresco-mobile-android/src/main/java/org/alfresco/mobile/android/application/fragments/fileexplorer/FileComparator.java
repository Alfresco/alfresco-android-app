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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class FileComparator implements Comparator<File>, Serializable
{
    private static final long serialVersionUID = 1L;

    private boolean asc;

    public FileComparator(boolean asc)
    {
        super();
        this.asc = asc;
    }

    public int compare(File f1, File f2)
    {
        int value = 0;

        if (f1.isDirectory() && !f2.isDirectory())
        {
            // Directory before non-directory
            value = -1;
        }
        else if (!f1.isDirectory() && f2.isDirectory())
        {
            // Non-directory after directory
            value = 1;
        }
        else
        {
            // Alphabetic order
            value = f1.getName().compareToIgnoreCase(f2.getName());
        }

        return asc ? value : -value;

    }
}
