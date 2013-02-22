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
package org.alfresco.mobile.android.application.fragments.browser;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.browser.local.LocalFileBrowserFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.intent.PublicIntent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class UploadChooseDialogFragment extends DialogFragment
{
    public static final String TAG = "UploadChooseDialogFragment";
    
    private Account currentAccount;

    private String fragmentTag;


    public static UploadChooseDialogFragment newInstance(Account currentAccount)
    {
        UploadChooseDialogFragment fragment = new UploadChooseDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("account", currentAccount);
        args.putSerializable("fragmentTag", ChildrenBrowserFragment.TAG);
        fragment.setArguments(args);
        return fragment;
    }
    
    public static UploadChooseDialogFragment newInstance(Account currentAccount, String fragmentTag)
    {
        UploadChooseDialogFragment fragment = new UploadChooseDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("account", currentAccount);
        args.putSerializable("fragmentTag", fragmentTag);
        fragment.setArguments(args);
        return fragment;
    }


    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        currentAccount = (Account) getArguments().get("account");
        fragmentTag = (String) getArguments().get("fragmentTag");


        return new AlertDialog.Builder(getActivity()).setTitle(R.string.upload_from)
                .setPositiveButton(R.string.upload_download_folder, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        File f = StorageManager.getDownloadFolder(getActivity(), currentAccount.getUrl(),
                                currentAccount.getUsername());

                        BaseFragment frag = LocalFileBrowserFragment.newInstance(f,
                                LocalFileBrowserFragment.MODE_PICK_FILE, fragmentTag);
                        frag.setSession(SessionUtils.getSession(getActivity()));
                        frag.show(getFragmentManager(), LocalFileBrowserFragment.TAG);
                    }
                }).setNegativeButton(R.string.upload_third_application, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        ActionManager.actionPickFile(getFragmentManager()
                                .findFragmentByTag(fragmentTag),
                                IntentIntegrator.REQUESTCODE_FILEPICKER);
                    }
                }).create();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PublicIntent.REQUESTCODE_DECRYPTED)
        {
            try
            {
                String filename = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("RequiresEncrypt", "");
                if (filename != null && filename.length() > 0)
                {
                    if (CipherUtils.encryptFile(getActivity(), filename, true) == false)
                        MessengerManager.showLongToast(getActivity(), getString(R.string.encryption_failed));
                    else
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("RequiresEncrypt", "").commit();
                }
            }
            catch (Exception e)
            {
                MessengerManager.showLongToast(getActivity(), getString(R.string.encryption_failed));
                e.printStackTrace();
            }
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }

}
