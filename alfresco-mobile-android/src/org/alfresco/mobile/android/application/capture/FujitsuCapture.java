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
package org.alfresco.mobile.android.application.capture;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import  org.alfresco.mobile.android.application.capture.CarryResult;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;


public class FujitsuCapture extends DeviceCapture
{
    public static final String TAG = "FujitsuCapture";

    private static final long serialVersionUID = 1L;

    private CarryResult cResult = null;
    
    private LinkedList<String> m_cFileNameList = null;
    
        
    public FujitsuCapture(Activity parent, Folder folder)
    {
        this(parent, folder, null);
    }

    public FujitsuCapture(Activity parent, Folder folder, File parentFolder)
    {
        super(parent, folder, parentFolder);
        mimeType = "application/pdf";
    }

    @Override
    public boolean hasDevice()
    {
        try 
        {
            Uri uri = Uri.parse("scansnap:///Scan&OutMode=2&CallBack=alfrescoFujitsuScanCallback:");
            
            Intent in = new Intent();
            in.setData(uri);
            
            final PackageManager mgr = parentActivity.getPackageManager();
            List<ResolveInfo> list = mgr.queryIntentActivities(in, PackageManager.MATCH_DEFAULT_ONLY);
               
            return list.size() > 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean captureData()
    {
        if (hasDevice())
        {
            String strUrlTxt = "scansnap:///Scan&OutMode=2&OutPath=" + parentFolder.getPath() + "&CallBack=alfrescoFujitsuScanCallback:";
            try 
            {
                Uri uri = Uri.parse(strUrlTxt);
                Intent in = new Intent();
                in.setData(uri);
                parentActivity.startActivity(in);
            }
            catch(ActivityNotFoundException  e)
            {   
                //specified application can not be found
                Log.d(TAG, Log.getStackTraceString(e));
                return false;
            }
            catch(NullPointerException  e)
            {                                           
                Log.d(TAG, Log.getStackTraceString(e));
                return false;
            }
            
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected boolean payloadCaptured(int requestCode, int resultCode, Intent callBackIntent)
    {
        File folder = parentFolder;
        if (folder != null)
        {
            m_cFileNameList = new LinkedList<String>();
            
            cResult = CarryResult.getInstance();
            cResult.clear();
    
            int bParseRet = CarryResult.CARRY_RESULT_SUCCESS;
            
            /*
             *  Data linkage mode is 3(bundle)
             */
            if(callBackIntent != null && callBackIntent.getExtras() != null)
            {
                bParseRet = parseBundleData(callBackIntent);
            }
            /*
             *  Data linkage mode is 2(url scheme)
             */
            else if(callBackIntent != null && callBackIntent.getData() != null)
            {                
                Uri uri = callBackIntent.getData();
                bParseRet = parseUri(uri);
            }
        
            if(bParseRet != CarryResult.CARRY_RESULT_SUCCESS)
            {
                //error occurred when parsing url, show dialog
                if(bParseRet == CarryResult.CARRY_PARSE_ERROR)
                {
                    AlfrescoNotificationManager.getInstance(context).showLongToast(context.getString(R.string.cannot_capture));
                }
                //error occurred when scan files, show dialog
                if(bParseRet == CarryResult.CARRY_SCAN_ERROR)
                {
                    AlfrescoNotificationManager.getInstance(context).showLongToast(context.getString(R.string.cannot_capture));
                }
                
                return false;
            }
            else
            {                
                payload = new File(folder.getPath(), m_cFileNameList.get(0));
                return true;
            }
        }
        else
        {
            AlfrescoNotificationManager.getInstance(parentActivity).showLongToast(parentActivity.getString(R.string.sdinaccessible));
            return false;
        }
    }
    
    /**
     * when data linkage mode is 2(url scheme),parse data from url
     * @param target url to parse   
     * @return  error code
     */
    private int parseUri(Uri carryUri)
    {   
        int sidesBegin,sidesEnd,fileCnt=1;
        String authorityData; 
        Map<String, String> urlParseMap = null;
                
        //authority value of url
        authorityData = carryUri.getEncodedSchemeSpecificPart().trim();         
        
        //find PFU header="PFUFILELISTFORMAT"
        if(authorityData != null && authorityData.startsWith("//"+CarryResult.PFUFILELISTFORMAT))
        {          
            sidesBegin = authorityData.indexOf('&', 0);   
            sidesEnd = authorityData.length()-1;
            
            if(sidesBegin > 0 && sidesEnd > sidesBegin && authorityData.substring(sidesBegin+1).equals(CarryResult.CARRY_ERROR))
            {
                //start with"&Error",scan error
                return CarryResult.CARRY_SCAN_ERROR;                            
            }
            else
            {
                authorityData = authorityData.substring(sidesBegin+1);
                
                //split the url by '&', generate a "key=value" formed string array
                String strParam[] = authorityData.split("&");                   
                
                urlParseMap = new HashMap<String, String>();
                String strUrlKey = null;
                String strUrlValue = null;
                int nUrlValue;
                if(strParam.length > 0)
                {
                    int paramCnt;
                    for(paramCnt = 0; paramCnt < strParam.length; paramCnt++)
                    {
                        //check '=', if not exist ,parse error
                        if(strParam[paramCnt].indexOf('=') > 0)
                        {                            
                            strUrlKey = strParam[paramCnt].substring(0,strParam[paramCnt].indexOf('=')).trim();
                            strUrlValue = strParam[paramCnt].substring(strParam[paramCnt].indexOf('=')+1).trim();

                            if(!strUrlKey.equals("") && urlParseMap.get(strUrlKey) == null)
                            {
                                urlParseMap.put(strUrlKey,strUrlValue);
                            }
                            else
                            {
                                return CarryResult.CARRY_PARSE_ERROR;
                            }
                        }
                        else 
                        {
                            return CarryResult.CARRY_PARSE_ERROR;
                        }   
                    }
                }
                
                try
                {    
                    /*
                     * get data linkage mode
                     */
                    Iterator iter = urlParseMap.entrySet().iterator();
                    strUrlValue =null;
                    
                    while (iter.hasNext())
                    {            
                        Map.Entry entry = (Map.Entry) iter.next();
                    
                        if(entry.getKey().toString().equalsIgnoreCase(CarryResult.CARRY_OUTMODE))
                        {
                            strUrlValue = urlParseMap.get(entry.getKey().toString());           
                            break;
                        }
                    }   
                                    
                    if(strUrlValue == null)
                    {
                        return CarryResult.CARRY_PARSE_ERROR;
                    }
                    else
                    {
                        if(strUrlValue.equals(""))
                        {
                            return CarryResult.CARRY_PARSE_ERROR;
                        }
                        else 
                        {
                            nUrlValue = Integer.parseInt(strUrlValue);
                            if(nUrlValue == CarryResult.CARRY_OUTMODE_URL_PATH) 
                            {
                                cResult.setOutMode(nUrlValue);
                            }
                            else
                            {
                                return CarryResult.CARRY_PARSE_ERROR;
                            }
                        }   
                    }
                    
                    /*
                     * get file fomat
                     */
                    iter = urlParseMap.entrySet().iterator();
                    strUrlValue =null;
                    
                    while (iter.hasNext()) 
                    {            
                        Map.Entry entry = (Map.Entry) iter.next();
                    
                        if(entry.getKey().toString().equalsIgnoreCase(CarryResult.CARRY_FORMAT))
                        {
                            strUrlValue = urlParseMap.get(entry.getKey().toString());               
                            break;
                        }
                    }
                    
                    if(strUrlValue == null)
                    {
                        return CarryResult.CARRY_PARSE_ERROR;
                    }
                    else
                    {
                        if(strUrlValue.equals(""))
                        {
                            return CarryResult.CARRY_PARSE_ERROR;
                        }
                        else
                        {
                            nUrlValue = Integer.parseInt(strUrlValue);
                        
                            if(nUrlValue == CarryResult.CARRY_FILE_TYPE_JPG || nUrlValue == CarryResult.CARRY_FILE_TYPE_PDF_MULTI_MODE)
                            {
                                cResult.setScanFileType(nUrlValue);
                            }
                            else
                            {
                                return CarryResult.CARRY_PARSE_ERROR;
                            }
                        }
                        
                    }
                    
                    /*
                     * get file count
                     */
                    iter = urlParseMap.entrySet().iterator();
                    strUrlValue =null;
                    
                    while (iter.hasNext())
                    {            
                        Map.Entry entry = (Map.Entry) iter.next();
                    
                        if(entry.getKey().toString().equalsIgnoreCase(CarryResult.CARRY_FILE_COUNT))
                        {
                            strUrlValue = urlParseMap.get(entry.getKey().toString());           
                            break;
                        }   
                    }
                    
                    //If "FileCount" does not exist,set default value=1
                    if(strUrlValue == null || strUrlValue.equals(""))
                    {
                        cResult.setFileCnt(1);
                    }
                    else
                    {
                        nUrlValue = Integer.parseInt(strUrlValue);
                        cResult.setFileCnt(nUrlValue);
                    }
                    
                    /*
                     * get file names according to "FileCount"
                     */
                    LinkedList<String> tempList = new LinkedList<String>();             
                    while(fileCnt <= cResult.getFileCnt())
                    {
                        iter = urlParseMap.entrySet().iterator();
                        strUrlValue =null;
                        
                        while (iter.hasNext()) 
                        {            
                            Map.Entry entry = (Map.Entry) iter.next();
                        
                            if(entry.getKey().toString().equalsIgnoreCase(CarryResult.CARRY_FILE+String.valueOf(fileCnt)))
                            {
                                strUrlValue = urlParseMap.get(entry.getKey().toString());       
                                break;
                            }   
                        }
                        
                        if(strUrlValue == null)
                        {
                            tempList = null;
                            return CarryResult.CARRY_PARSE_ERROR;
                        }   
                        else
                        {
                            if(strUrlValue.equals(""))
                            {
                                tempList = null;
                                return CarryResult.CARRY_PARSE_ERROR;
                            }
                            else 
                            {
                                tempList.addFirst(strUrlValue.substring(strUrlValue.lastIndexOf('/')+1));
                            }
                        }
                        
                        fileCnt++;
                    }
                    
                    //add file name to FileList to display
                    m_cFileNameList.addAll(0,tempList);                 
                    tempList = null;
                    
                }
                catch (Exception e) 
                {
                    return CarryResult.CARRY_PARSE_ERROR;
                }
            }
        }
        else
        { 
            return CarryResult.CARRY_PARSE_ERROR;
        }
        
        return CarryResult.CARRY_RESULT_SUCCESS;
    }
    
    
    /**
     * when data linkage mode is 3(bundle),parse date from bundle
     * @param callBackIntent
     * @return
     */
    private int parseBundleData(Intent callBackIntent)
    {    
        //get PFU header (true means callback from SSCA)
        if(!callBackIntent.getBooleanExtra(CarryResult.PFUFILELISTFORMAT,false))
        {
            return CarryResult.CARRY_PARSE_ERROR;
        }
        
        //get scan result (true means error occurred while reading files)
        cResult.setScanResult(callBackIntent.getBooleanExtra(CarryResult.CARRY_ERROR,false));           
        
        if(!cResult.getScanResult())
        {   
            //get file format, default value is 0
            int nFormat = callBackIntent.getIntExtra(CarryResult.CARRY_FORMAT, 0);
            if(nFormat != CarryResult.CARRY_FILE_TYPE_JPG && nFormat != CarryResult.CARRY_FILE_TYPE_PDF_MULTI_MODE)
                return CarryResult.CARRY_PARSE_ERROR;
            else
                cResult.setScanFileType(nFormat);
            
            //get file count, default value is 1
            cResult.setFileCnt(callBackIntent.getIntExtra(CarryResult.CARRY_FILE_COUNT, 1));                
            
            //get file name array
            if(callBackIntent.getStringArrayListExtra(CarryResult.CARRY_FILE) != null){
                ArrayList<String> tempList = callBackIntent.getStringArrayListExtra(CarryResult.CARRY_FILE);
                
                if(tempList.size() != cResult.getFileCnt())
                {
                    return CarryResult.CARRY_PARSE_ERROR;
                }
                
                int nCnt;
                ArrayList<String> fileSequenceList = new ArrayList<String>();
                for(nCnt=tempList.size();nCnt > 0;nCnt--)
                    fileSequenceList.add(tempList.get(nCnt-1).substring(tempList.get(nCnt-1).lastIndexOf('/')+1));
                
                m_cFileNameList.addAll(0,fileSequenceList);
            }
            else
                return CarryResult.CARRY_PARSE_ERROR;
            
        }
        else
        {
            return CarryResult.CARRY_SCAN_ERROR;
        }
        
        return CarryResult.CARRY_RESULT_SUCCESS;
    }
}