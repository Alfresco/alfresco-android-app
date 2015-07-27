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
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesFragment;
import org.alfresco.mobile.android.application.fragments.operation.OperationsFragment;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskFragment;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskTypePickerFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DocumentPickerFragment.onPickDocumentFragment;
import org.alfresco.mobile.android.async.file.encryption.AccountProtectionEvent;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

/**
 * @author Jean Marie Pascal
 */
public class PrivateDialogActivity extends BaseActivity
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

        displayAsDialogActivity();
        setContentView(R.layout.app_left_panel);

        action = getIntent().getAction();
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

        if (ACTION_EDIT_NODE.equals(action))
        {
            Folder folder = (Folder) getIntent().getExtras().get(PrivateIntent.EXTRA_FOLDER);
            Node node = (Node) getIntent().getExtras().get(PrivateIntent.EXTRA_NODE);

            Fragment f = EditPropertiesFragment.with(this).parentFolder(folder).node(node).createFragment();
            FragmentDisplayer.with(this).back(false).load(f).animate(null).into(FragmentDisplayer.PANEL_LEFT);
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
    public onPickDocumentFragment getOnPickDocumentFragment()
    {
        if (action != null && ACTION_EDIT_NODE.equals(action))
        {
            return (onPickDocumentFragment) getFragmentManager().findFragmentByTag(EditPropertiesFragment.TAG);
        }
        else
        {
            return (onPickDocumentFragment) getFragmentManager().findFragmentByTag(CreateTaskFragment.TAG);
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
