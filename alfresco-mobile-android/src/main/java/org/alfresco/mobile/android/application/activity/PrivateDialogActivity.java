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
package org.alfresco.mobile.android.application.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.help.HelpDialogFragment;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesFragment;
import org.alfresco.mobile.android.application.fragments.operation.OperationsFragment;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskFragment;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskTypePickerFragment;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.application.ui.form.picker.DocumentPickerFragment.onPickDocumentFragment;
import org.alfresco.mobile.android.async.file.encryption.AccountProtectionEvent;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;

import com.squareup.otto.Subscribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

/**
 * @author Jean Marie Pascal
 */
public class PrivateDialogActivity extends BaseAppCompatActivity
{
    private static final String TAG = PrivateDialogActivity.class.getName();

    public static final String EXTRA_FIELD_ID = "org.alfresco.mobile.android.intent.ACTION_PICK_NODE";

    public static final String ACTION_EDIT_NODE = "org.alfresco.mobile.android.intent.ACTION_EDIT_NODE";

    private String action;

    private String fieldId;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    /** Called when the activity is first created. */
    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        action = getIntent().getAction();

        if (PrivateIntent.ACTION_DISPLAY_HELP.equals(action))
        {
            setTheme(R.style.AlfrescoMaterialTheme);
        }
        else
        {
            displayAsDialogActivity();
            setTheme(R.style.AlfrescoMaterialTheme);
        }

        setContentView(R.layout.activitycompat_left_panel);

        // TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
        {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (PrivateIntent.ACTION_DISPLAY_SETTINGS.equals(action) && getFragment(GeneralPreferences.TAG) == null)
        {
            FragmentDisplayer.with(this).load(GeneralPreferences.with(this).createFragment()).back(false).animate(null)
                    .into(FragmentDisplayer.PANEL_LEFT);
            return;
        }

        if (PrivateIntent.ACTION_DISPLAY_OPERATIONS.equals(action))
        {
            FragmentDisplayer.with(this).load(new OperationsFragment()).back(false).animate(null)
                    .into(FragmentDisplayer.PANEL_LEFT);
            return;
        }

        if (ACTION_EDIT_NODE.equals(action))
        {
            if (getSupportActionBar() != null)
            {
                getSupportActionBar().hide();
            }

            Folder folder = (getIntent().hasExtra(PrivateIntent.EXTRA_FOLDER))
                    ? (Folder) getIntent().getExtras().get(PrivateIntent.EXTRA_FOLDER) : null;
            Node node = (getIntent().hasExtra(PrivateIntent.EXTRA_NODE))
                    ? (Node) getIntent().getExtras().get(PrivateIntent.EXTRA_NODE) : null;

            Fragment f = EditPropertiesFragment.with(this).parentFolder(folder).node(node).createFragment();
            FragmentDisplayer.with(this).back(false).load(f).animate(null).into(FragmentDisplayer.PANEL_LEFT);
            return;
        }

        if (PrivateIntent.ACTION_START_PROCESS.equals(action) && getFragment(CreateTaskTypePickerFragment.TAG) == null
                && getFragment(CreateTaskFragment.TAG) == null)
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
                docs.addAll(
                        (Collection<? extends Document>) getIntent().getExtras().get(PrivateIntent.EXTRA_DOCUMENTS));
                getIntent().removeExtra(PrivateIntent.EXTRA_DOCUMENTS);
            }

            Fragment f = docs.isEmpty() ? new CreateTaskTypePickerFragment()
                    : CreateTaskTypePickerFragment.newInstance(docs);
            FragmentDisplayer.with(this).load(f).back(false).animate(null).into(FragmentDisplayer.PANEL_LEFT);

            AnalyticsHelper.reportScreen(this, AnalyticsManager.SCREEN_TASK_CREATE_TYPE);
        }

        if (PrivateIntent.ACTION_DISPLAY_HELP.equals(action))
        {
            FragmentDisplayer.with(this).load(HelpDialogFragment.with(this).createFragment()).back(false).animate(null)
                    .into(FragmentDisplayer.PANEL_LEFT);
        }
    }

    @Override
    protected void onStart()
    {
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PassCodeActivity.REQUEST_CODE_PASSCODE && resultCode == RESULT_CANCELED)
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
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
    public onPickDocumentFragment getOnPickDocumentFragment()
    {
        if (action != null && ACTION_EDIT_NODE.equals(action))
        {
            return (onPickDocumentFragment) getSupportFragmentManager().findFragmentByTag(EditPropertiesFragment.TAG);
        }
        else
        {
            return (onPickDocumentFragment) getSupportFragmentManager().findFragmentByTag(CreateTaskFragment.TAG);
        }
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(String fieldId)
    {
        this.fieldId = fieldId;
    }

    public void doCancel(View v)
    {
        finish();
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
}
