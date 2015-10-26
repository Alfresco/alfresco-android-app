/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.activity;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.sync.SyncMigrationFragment;
import org.alfresco.mobile.android.sync.SyncContentManager;

import android.os.Bundle;

/**
 * @author Jean Marie Pascal
 */
public class InfoActivity extends BaseAppCompatActivity
{
    private static final String TAG = InfoActivity.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        displayAsDialogActivity(0.5f, 0.65f);

        setContentView(R.layout.app_main_single);

        if (getFragment(SyncMigrationFragment.TAG) == null)
        {
            FragmentDisplayer.load(SyncMigrationFragment.with(this).back(false)).animate(null)
                    .into(FragmentDisplayer.PANEL_LEFT);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (getFragment(SyncMigrationFragment.TAG) != null)
        {
            SyncContentManager.saveStateInfo(this);
            finish();
        }
        else
        {
            super.onBackPressed();
        }
    }

}
