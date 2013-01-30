package org.alfresco.mobile.android.application.preferences;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.R.layout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

@SuppressWarnings("deprecation")
public class Prefs extends PreferenceActivity 
{
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.layout.prefs);
        
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        Preference pref = preferenceScreen.findPreference("allowuntrusted");
        pref.setSelectable(false);
        pref.setEnabled(false);              
        
        pref = preferenceScreen.findPreference("web");
        pref.setSelectable(false);
        pref.setEnabled(false);   
        
        pref = preferenceScreen.findPreference("rate");
        pref.setSelectable(false);
        pref.setEnabled(false);   
        
        pref = preferenceScreen.findPreference("like");
        pref.setSelectable(false);
        pref.setEnabled(false);      
        
        if (isDeviceRooted()  ||  prefs.getBoolean("HasAccessedPaidServices", false) == false)
        {   
            Preference privateFoldersPref = preferenceScreen.findPreference("privatefolders");
            if (privateFoldersPref != null)
            {
                privateFoldersPref.setSelectable(false);
                privateFoldersPref.setEnabled(false);              
                prefs.edit().putBoolean("privatefolders", false).commit();
            }
            
            Preference pinPref = preferenceScreen.findPreference("pin");
            if (pinPref != null)
            {
                pinPref.setSelectable(false);
                pinPref.setEnabled(false);              
            }
        }
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
