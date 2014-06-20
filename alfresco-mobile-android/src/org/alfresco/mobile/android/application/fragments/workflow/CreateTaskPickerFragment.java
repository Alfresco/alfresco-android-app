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
package org.alfresco.mobile.android.application.fragments.workflow;

import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.browser.NodeAdapter;
import org.alfresco.mobile.android.application.fragments.person.UserAdapter;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

public class CreateTaskPickerFragment extends AlfrescoFragment
{
    public static final String TAG = CreateTaskPickerFragment.class.getName();

    private static final String MODE = "pickerMode";

    public static final int MODE_PERSON = 1;

    public static final int MODE_DOCUMENT = 2;

    private List<Person> assignees;

    private View vRoot;

    private CreateTaskFragment createTaskFragment;

    private int mode = MODE_PERSON;

    private List<Node> docs;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateTaskPickerFragment()
    {
    }

    public static CreateTaskPickerFragment newInstance(int mode)
    {
        CreateTaskPickerFragment bf = new CreateTaskPickerFragment();
        Bundle b = new Bundle();
        b.putInt(MODE, mode);
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getArguments() == null || !getArguments().containsKey(MODE)) { return null; }
        mode = getArguments().getInt(MODE);

        // Retrieve parameters
        createTaskFragment = ((CreateTaskFragment) getFragmentManager().findFragmentByTag(CreateTaskFragment.TAG));

        if (mode == MODE_DOCUMENT)
        {
            docs = createTaskFragment.getAttachments();
        }
        else if (mode == MODE_PERSON)
        {
            assignees = createTaskFragment.getAssignees();
        }

        int titleId = -1;
        if (getDialog() != null)
        {
            titleId = R.string.task_assignees;
            if (mode == MODE_DOCUMENT)
            {
                titleId = R.string.task_attachments;
            }
            getDialog().setTitle(titleId);
        }

        setRetainInstance(true);
        setSession(SessionUtils.getSession(getActivity()));
        SessionUtils.checkSession(getActivity(), getSession());
        vRoot = inflater.inflate(R.layout.app_picker_list, container, false);
        if (getSession() == null) { return vRoot; }

        if (mode == MODE_PERSON)
        {
            ((ListView) vRoot.findViewById(R.id.listView)).setAdapter(new UserAdapter(this, R.layout.app_item_row,
                    assignees, true));
        }
        else if (mode == MODE_DOCUMENT)
        {
            ((ListView) vRoot.findViewById(R.id.listView)).setAdapter(new NodeAdapter(this,
                    R.layout.app_task_progress_row, docs, true));
        }

        // Button Selection
        Button b = (Button) vRoot.findViewById(R.id.action_select);
        titleId = R.string.task_assignees_selection;
        if (mode == MODE_DOCUMENT)
        {
            titleId = R.string.task_attachments_selection;
        }
        b.setText(titleId);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mode == MODE_DOCUMENT)
                {
                    createTaskFragment.startDocumentPicker();
                }
                else if (mode == MODE_PERSON)
                {
                    createTaskFragment.startPersonPicker();
                }
                dismiss();
            }
        });

        return vRoot;
    }

    public void removeAssignee(Person item)
    {
        assignees.remove(item.getIdentifier());
        createTaskFragment.removeAssignee(item);
    }

    public void removeDocument(Document item)
    {
        docs.remove(item.getIdentifier());
        createTaskFragment.removeDocument(item);
    }
}
