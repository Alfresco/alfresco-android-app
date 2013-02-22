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

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.documentfolder.actions.CreateFolderDialogFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class AddFolderDialogFragment extends CreateFolderDialogFragment
{

    public AddFolderDialogFragment()
    {
    }

    public static AddFolderDialogFragment newInstance(Folder folder)
    {
        AddFolderDialogFragment adf = new AddFolderDialogFragment();
        adf.setArguments(createBundle(folder));
        adf.setRetainInstance(true);
        return adf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
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
