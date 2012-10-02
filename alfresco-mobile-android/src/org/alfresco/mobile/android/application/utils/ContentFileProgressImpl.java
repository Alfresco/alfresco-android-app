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

import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;



/**
 * ContentFile represents an abstract way to share file between the client
 * remote api and server.
 * 
 * @author Luke Jagger
 */
public class ContentFileProgressImpl extends ContentFileImpl
{
    private static final long serialVersionUID = 1L;
   
    private int amountCopied = 0;
    private int segment = 0;
    private int currentSegment = 0;
    private String newFilename = null;
    
    
    public ContentFileProgressImpl()
    {
        super();
    }

    /**
     * Init a contentFile based on local file.
     * 
     * @param f : file inside a device filesystem.
     */
    public ContentFileProgressImpl(File f)
    {
        super(f);
        
        segment = (int) (f.length() / 10);
    }

    /**
     * Init a contentFile based on local file and redefine default mimetype and
     * filename associated.
     * 
     * @param f : File inside a device filesystem
     * @param filename : New name of the file
     * @param mimetype : mimetype associated to the file.
     */
    public ContentFileProgressImpl(File f, String filename, String mimetype)
    {
        super(f, filename, mimetype);
        
        segment = (int) (f.length() / 10);
    }
    
    
    @Override
    public void fileReadCallback(int nBytes)
    {
        amountCopied += nBytes;
        
        if (amountCopied / segment > currentSegment)
        {
            ++currentSegment;
            ProgressNotification.updateProgress (getFileName());
        }
    }

    @Override
    public void fileWriteCallback(int nBytes)
    {
        ProgressNotification.updateProgress (getFileName());
    }
    
    public void setFilename (String name)
    {
        newFilename = name;
    }
    
    @Override
    public String getFileName()
    {
        if (newFilename != null)
            return newFilename;
        else
            return super.getFileName();
    }
}
