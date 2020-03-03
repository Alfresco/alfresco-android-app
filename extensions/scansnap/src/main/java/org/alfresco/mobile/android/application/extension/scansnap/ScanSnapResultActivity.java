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
package org.alfresco.mobile.android.application.extension.scansnap;

import org.alfresco.mobile.android.platform.extensions.ScanSnapManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

/**
 * This activity is responsible to retrieve information from ScanSnap Connect
 * Application and create the internal Intent to send to Alfresco Mobile
 * Application.
 * 
 * @author Jean Marie Pascal
 */
public class ScanSnapResultActivity extends FragmentActivity
{
    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.activity_list_item);

        String action = getIntent().getAction();

        // Fujitsu ScanSnap Integration
        if ((Intent.ACTION_VIEW.equals(action)) && ScanSnapManager.getInstance(this) != null)
        {
            // Prepare the new Intent
            Intent i = new Intent();
            i.setAction(PrivateIntent.ACTION_SCAN_RESULT);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Retrieve Information from the Intent
            if (getIntent().getStringArrayListExtra(ScanSnapManager.CARRY_FILE) != null)
            {
                i.putStringArrayListExtra(PrivateIntent.EXTRA_FILE_PATH,
                        getIntent().getStringArrayListExtra(ScanSnapManager.CARRY_FILE));
            }

            // end this activity. It's just a passthrough
            finish();
            startActivity(i);
        }
    }
}
