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
package org.alfresco.mobile.android.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;

import org.alfresco.mobile.android.api.asynchronous.CloudSessionLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.StorageManager;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Environment;

public class LoginLoaderCallback implements LoaderCallbacks<LoaderResult<AlfrescoSession>>
{
    //TODO REMOVE ALL BEFORE RELEASE!!
    public static final String ALFRESCO_CLOUD_URL = "http://my.alfresco.com";
    public static final String BASE_URL = "org.alfresco.mobile.binding.internal.baseurl";
    public static final String USER = "org.alfresco.mobile.credential.user";
    public static final String PASSWORD = "org.alfresco.mobile.credential.password";
    
    public static final String CLOUD_CONFIG_PATH = Environment.getExternalStorageDirectory().getPath() + "/alfresco-mobile/cloud-config.properties";
    public static final boolean ENABLE_CONFIG_FILE = true;
    
    private Activity activity;
    private String url;
    private String username;
    private String password;
    private Bundle bundle;
   // private ProgressDialog mProgressDialog;

    public LoginLoaderCallback(Activity activity, String url, String username, String password)
    {
       this.activity = activity;
       this.url = url;
       this.username = username;
       this.password = password;
    }
    
    public LoginLoaderCallback(Activity activity, String url, String username, String password, Bundle bundle)
    {
       this.activity = activity;
       this.url = url;
       this.username = username;
       this.password = password;
       this.bundle = bundle;
    }

    @Override
    public Loader<LoaderResult<AlfrescoSession>> onCreateLoader(final int id, Bundle args)
    {
        //Default settings for Alfresco Application
        HashMap<String, Serializable> settings = new HashMap<String, Serializable>(2);
        settings.put(SessionParameter.CONNECT_TIMEOUT, "10000");
        settings.put(SessionParameter.READ_TIMEOUT, "60000");
        settings.put(AlfrescoSession.EXTRACT_METADATA, true);
        settings.put(AlfrescoSession.CREATE_THUMBNAIL, true);
        settings.put(AlfrescoSession.CACHE_FOLDER, StorageManager.getCacheDir(activity, "AlfrescoMobile"));
        
        //TODO REMOVE ALL BEFORE RELEASE!!
        //Specific for Test Instance server
        if (url.startsWith(ALFRESCO_CLOUD_URL)){
            
            //TODO Remove it when public
            url = "http://devapis.alfresco.com";
            
            // Check Properties available inside the device
            if (ENABLE_CONFIG_FILE){
                File f = new File(CLOUD_CONFIG_PATH);
                if (f.exists() && ENABLE_CONFIG_FILE)
                {
                    Properties prop = new Properties();
                    try
                    {
                        // load a properties file
                        prop.load(new FileInputStream(f));
                        url = prop.getProperty("url");
                    }
                    catch (IOException ex)
                    {
                        throw new AlfrescoServiceException(ErrorCodeRegistry.PARSING_GENERIC, "Error with config files");
                    }
                }  
            }
            
            settings.put(BASE_URL, url);
            settings.put(USER, username);
            settings.put(PASSWORD, password);
            return new CloudSessionLoader(activity, null, settings);
        } else {
            return new SessionLoader(activity, url, username, password, settings);
        }
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<AlfrescoSession>> arg0, LoaderResult<AlfrescoSession> results)
    {
        
        if (!results.hasException()){
            SessionUtils.setsession(activity, results.getData());
            Intent i = new Intent(activity, MainActivity.class);
            i.setAction(IntentIntegrator.ACTION_LOAD_SESSION_FINISH);
            activity.startActivity(i);
        } else {
            MessengerManager.showToast(activity, "ERROR : Session not loaded");
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<AlfrescoSession>> arg0)
    {
        // TODO Auto-generated method stub

    }
    
}
