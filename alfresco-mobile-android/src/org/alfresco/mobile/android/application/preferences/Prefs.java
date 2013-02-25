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

package org.alfresco.mobile.android.application.preferences;

import java.io.File;
import java.util.Vector;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.R.layout;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.EditText;

@SuppressWarnings("deprecation")
public class Prefs extends PreferenceActivity 
{
    public static final String HAS_ACCESSED_PAID_SERVICES = "HasAccessedPaidServices";
    public static final String REQUIRES_ENCRYPT = "RequiresEncrypt";
    public static final String ENCRYPTION_USER_INTERACTION = "EncryptionUserInteraction";
    public static final String PRIVATE_FOLDERS = "privatefolders";
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.layout.prefs);
        
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                                
        if (/*isDeviceRooted()  || */ prefs.getBoolean(HAS_ACCESSED_PAID_SERVICES, false) == false)
        {   
            Preference privateFoldersPref = preferenceScreen.findPreference(PRIVATE_FOLDERS);
            if (privateFoldersPref != null)
            {
                privateFoldersPref.setSelectable(false);
                privateFoldersPref.setEnabled(false);              
                prefs.edit().putBoolean(PRIVATE_FOLDERS, false).commit();
            }
        }
        
        Preference privateFoldersPref = preferenceScreen.findPreference(PRIVATE_FOLDERS);
        
        privateFoldersPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Prefs.this);
                final boolean checked = !prefs.getBoolean(PRIVATE_FOLDERS, false);
                
                final File folder = StorageManager.getPrivateFolder(Prefs.this, "", "", "");
                if (folder != null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Prefs.this);
                    builder.setTitle(Prefs.this.getString(R.string.data_protection));
                    builder.setMessage(Prefs.this.getString(checked ? R.string.content_being_decrypted : R.string.content_being_encrypted));
                    builder.setCancelable(false);
                    final AlertDialog progressAlert = builder.create();
                    
                    builder = new AlertDialog.Builder(Prefs.this);
                    builder.setTitle(Prefs.this.getString(R.string.data_protection));
                    builder.setMessage(Prefs.this.getString(checked ? R.string.unprotect_question : R.string.protect_question));
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            dialog.dismiss();
                            progressAlert.show();
                            
                            new Handler().postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Vector<String> folders = new Vector<String>();
                                    folders.add(StorageManager.DLDIR);
                                    folders.add(StorageManager.TEMPDIR);
                                    
                                    if (checked)
                                    {
                                        if (IOUtils.decryptFiles(Prefs.this, folder.getPath(), folders, true))
                                            prefs.edit().putBoolean(PRIVATE_FOLDERS, false).commit();
                                        else
                                            MessengerManager.showLongToast(Prefs.this, Prefs.this.getString(R.string.decryption_failed));
                                    }
                                    else
                                    {
                                        if (IOUtils.encryptFiles(Prefs.this, folder.getPath(), folders, true))
                                            prefs.edit().putBoolean(PRIVATE_FOLDERS, true).commit();
                                        else
                                            MessengerManager.showLongToast(Prefs.this, Prefs.this.getString(R.string.encryption_failed));
                                    }
                                    
                                    progressAlert.hide();
                                }
                                
                            }, 1000);
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            prefs.edit().putBoolean(PRIVATE_FOLDERS, checked).commit();
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else
                    MessengerManager.showLongToast(Prefs.this, getString(R.string.sdinaccessible));
                
                return false;
            }
        });
    }
    
    public static boolean isDeviceRooted() 
    {

      // get from build info
      String buildTags = android.os.Build.TAGS;
      if (buildTags != null && buildTags.contains("test-keys"))
      {
        return true;
      }

      // check if /system/app/Superuser.apk is present
      try 
      {
        File file = new File("/system/app/Superuser.apk");
        if (file.exists())
        {
           return true;
        }
      } 
      catch (Throwable e1)
      {
        // ignore
      }

      return false;
    }
}
