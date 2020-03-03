/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.security;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerHelper;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AccountsPreferences;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;

public class DataCleaner extends AsyncTask<String, Integer, Boolean>
{
    private static final String TAG = "DataCleaner";

    private List<File> listingFiles = new ArrayList<File>();

    private WeakReference<FragmentActivity> activityRef;

    public DataCleaner(FragmentActivity activity)
    {
        super();
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    protected Boolean doInBackground(String... params)
    {
        try
        {
            // Remove preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activityRef.get());
            Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
            SharedPreferences settings = activityRef.get().getSharedPreferences(AccountsPreferences.ACCOUNT_PREFS, 0);
            editor = settings.edit();
            editor.clear();
            editor.apply();
            SharedPreferences prefs = activityRef.get().getSharedPreferences(FileExplorerHelper.FILEEXPLORER_PREFS, 0);
            editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Remove All Accounts
            List<Account> accounts = AlfrescoAccountManager.getInstance(activityRef.get()).getAndroidAccounts();
            for (Account accountToDelete : accounts)
            {
                AccountManager.get(activityRef.get()).removeAccount(accountToDelete, null, null);
            }

            // Delete loaded accounts
            SessionManager.getInstance(activityRef.get()).shutdown();
            AlfrescoAccountManager.getInstance(activityRef.get()).shutdown();

            // Find folders
            File cache = activityRef.get().getCacheDir();
            File folder = activityRef.get().getExternalFilesDir(null);

            listingFiles.add(cache);
            listingFiles.add(folder);

            // Remove Files/folders
            for (File file : listingFiles)
            {
                if (file.exists())
                {
                    if (file.isDirectory())
                    {
                        recursiveDelete(file);
                    }
                    else
                    {
                        file.delete();
                    }
                }
            }
            return true;
        }
        catch (Exception fle)
        {
            Log.e(TAG, Log.getStackTraceString(fle));
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean statut)
    {
        if (statut && activityRef.get() != null)
        {
            AlfrescoNotificationManager.getInstance(activityRef.get()).showLongToast(
                    activityRef.get().getString(R.string.passcode_erase_data_complete));
            activityRef.get().setResult(FragmentActivity.RESULT_CANCELED);
            activityRef.get().finish();
        }
    }

    private boolean recursiveDelete(File file)
    {
        File[] files = file.listFiles();
        File childFile;
        if (files != null)
        {
            for (int x = 0; x < files.length; x++)
            {
                childFile = files[x];
                if (childFile.isDirectory())
                {
                    if (!recursiveDelete(childFile)) { return false; }
                }
                else
                {
                    if (!childFile.delete()) { return false; }
                }
            }
        }
        if (!file.delete()) { return false; }

        return true;
    }
}
