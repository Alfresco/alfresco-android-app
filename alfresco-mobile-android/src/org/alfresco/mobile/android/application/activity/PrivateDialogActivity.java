/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderPickerCallback;
import org.alfresco.mobile.android.application.fragments.operations.OperationsFragment;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskDocumentPickerFragment;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskFragment;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskTypePickerFragment;
import org.alfresco.mobile.android.async.file.encryption.AccountProtectionEvent;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.ui.fragments.WaitingDialogFragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

/**
 * @author Jean Marie Pascal
 */
public class PrivateDialogActivity extends BaseActivity
{
    private static final String TAG = PrivateDialogActivity.class.getName();

    private boolean doubleBackToExitPressedOnce = false;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    /** Called when the activity is first created. */
    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        displayAsDialogActivity();
        setContentView(R.layout.app_left_panel);

        String action = getIntent().getAction();
        if (PrivateIntent.ACTION_DISPLAY_SETTINGS.equals(action))
        {
            GeneralPreferences.with(this).back(false).display();
            return;
        }

        if (PrivateIntent.ACTION_DISPLAY_OPERATIONS.equals(action))
        {
            FragmentDisplayer.with(this).load(new OperationsFragment()).back(false).animate(null)
                    .into(FragmentDisplayer.PANEL_LEFT);
            return;
        }

        if (PrivateIntent.ACTION_START_PROCESS.equals(action) && getFragment(CreateTaskTypePickerFragment.TAG) == null)
        {
            List<Document> docs = new ArrayList<Document>();
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(PrivateIntent.EXTRA_DOCUMENT))
            {
                docs.add((Document) getIntent().getExtras().get(PrivateIntent.EXTRA_DOCUMENT));
                getIntent().removeExtra(PrivateIntent.EXTRA_DOCUMENT);
            }
            else if (getIntent().getExtras() != null
                    && getIntent().getExtras().containsKey(PrivateIntent.EXTRA_DOCUMENTS))
            {
                docs.addAll((Collection<? extends Document>) getIntent().getExtras().get(PrivateIntent.EXTRA_DOCUMENTS));
                getIntent().removeExtra(PrivateIntent.EXTRA_DOCUMENTS);
            }

            Fragment f = docs.isEmpty() ? new CreateTaskTypePickerFragment() : CreateTaskTypePickerFragment
                    .newInstance(docs);
            FragmentDisplayer.with(this).load(f).back(false).animate(null).into(FragmentDisplayer.PANEL_LEFT);
            return;
        }
    }

    @Override
    protected void onStart()
    {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        if (receiver != null)
        {
            broadcastManager.unregisterReceiver(receiver);
        }
        super.onStop();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                if (getIntent() != null && PrivateIntent.ACTION_PICK_FILE.equals(getIntent().getAction()))
                {
                    finish();
                }
                else
                {
                    Intent i = new Intent(this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public DocumentFolderPickerCallback getOnPickDocumentFragment()
    {
        return (DocumentFolderPickerCallback) getFragmentManager().findFragmentByTag(CreateTaskFragment.TAG);
    }

    public void doCancel(View v)
    {
        getFragmentManager().popBackStackImmediate(CreateTaskDocumentPickerFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAccountProtectionEvent(AccountProtectionEvent event)
    {
        removeWaitingDialog();
        if (getFragment(GeneralPreferences.TAG) != null)
        {
            ((GeneralPreferences) getFragment(GeneralPreferences.TAG)).refreshDataProtection();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // NAVIGATION
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onBackPressed()
    {
        if (getFragment(WaitingDialogFragment.TAG) != null)
        {
            if (doubleBackToExitPressedOnce)
            {
                ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
        else
        {
            super.onBackPressed();
        }
    }
}
