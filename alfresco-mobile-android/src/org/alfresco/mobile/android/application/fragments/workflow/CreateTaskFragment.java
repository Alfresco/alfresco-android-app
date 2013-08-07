/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.api.utils.DateUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.person.PersonSearchFragment;
import org.alfresco.mobile.android.application.fragments.person.onPickPersonFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.workflow.process.complete.StartProcessRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

public class CreateTaskFragment extends BaseFragment implements onPickPersonFragment
{
    public static final String TAG = CreateTaskFragment.class.getName();

    private static final String PARAM_PROCESS_DEFINITION = "processDefinition";

    private View vRoot;

    private ProcessDefinition processDefinition;

    private GregorianCalendar dueAt;

    private Map<String, Person> assignees = new HashMap<String, Person>(1);

    private Map<String, Document> items = new HashMap<String, Document>(1);

    private int approvers = 0;

    private int priority = WorkflowModel.PRIORITY_MEDIUM;

    private Button bM, bL, bH;

    private boolean isAdhoc = false;

    private EditText titleTask;

    private Button validation;

    private TextView errorMessage;

    private Switch emailNotification;

    private EditText approversEditText;

    private ImageButton removeApprover;

    private ImageButton addApprover;

    private StartProcessReceiver receiver;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateTaskFragment()
    {
    }

    public static CreateTaskFragment newInstance(ProcessDefinition processDefinition)
    {
        CreateTaskFragment bf = new CreateTaskFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_PROCESS_DEFINITION, processDefinition);
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Retrieve parameters
        if (getArguments() == null && !getArguments().containsKey(PARAM_PROCESS_DEFINITION)) { return null; }
        processDefinition = (ProcessDefinition) getArguments().getSerializable(PARAM_PROCESS_DEFINITION);

        setRetainInstance(true);
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        vRoot = inflater.inflate(R.layout.app_start_process, container, false);
        if (alfSession == null) { return vRoot; }

        // DESCRIPTION
        titleTask = (EditText) vRoot.findViewById(R.id.process_title);
        titleTask.addTextChangedListener(watcher);
        errorMessage = ((TextView) vRoot.findViewById(R.id.error_message));

        // DatePicker
        ImageButton ib = (ImageButton) vRoot.findViewById(R.id.action_process_due_on);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new DatePickerFragment().show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });

        // ASSIGNEES
        ib = (ImageButton) vRoot.findViewById(R.id.action_process_assignee);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PersonSearchFragment frag = PersonSearchFragment.newInstance(ListingModeFragment.MODE_PICK, TAG,
                        isAdhoc);
                FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                        PersonSearchFragment.TAG, true);
            }
        });
        EditText edt = (EditText) vRoot.findViewById(R.id.process_assignee);
        edt.setHint(String.format(getResources().getQuantityString(R.plurals.process_assignees, assignees.size()),
                assignees.size()));

        // APPROVERS
        if (WorkflowModel.FAMILY_PROCESS_ADHOC.contains(processDefinition.getKey()))
        {
            vRoot.findViewById(R.id.process_approvers_group).setVisibility(View.GONE);
            isAdhoc = true;
        }
        else
        {
            approversEditText = (EditText) vRoot.findViewById(R.id.process_approvers);
            removeApprover = (ImageButton) vRoot.findViewById(R.id.action_remove_approvers);
            addApprover = (ImageButton) vRoot.findViewById(R.id.action_add_approvers);
            updateApprovers();

            removeApprover.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (approvers >= 2)
                    {
                        approvers--;
                    }
                    updateApprovers();
                }
            });
            addApprover.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (approvers < assignees.size())
                    {
                        approvers++;
                    }
                    updateApprovers();
                }
            });
        }

        // ATTACHMENTS
        ib = (ImageButton) vRoot.findViewById(R.id.action_process_attachments);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        // PRIORITY
        bM = (Button) vRoot.findViewById(R.id.action_priority_medium);
        bM.setOnTouchListener(priorityClickListener);
        bL = (Button) vRoot.findViewById(R.id.action_priority_low);
        bL.setOnTouchListener(priorityClickListener);
        bH = (Button) vRoot.findViewById(R.id.action_priority_high);
        bH.setOnTouchListener(priorityClickListener);
        updatePriority();

        // VALIDATION
        validation = (Button) vRoot.findViewById(R.id.action_create);
        validation.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createProcess();
            }
        });

        // Email Notification
        emailNotification = (Switch) vRoot.findViewById(R.id.action_send_notification);

        return vRoot;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        getActivity().invalidateOptionsMenu();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), R.string.process_start_title);
        }
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_START_PROCESS_COMPLETED);
        receiver = new StartProcessReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);

        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public void setDueAt(GregorianCalendar gregorianCalendar)
    {
        dueAt = gregorianCalendar;
        EditText dueOn = (EditText) vRoot.findViewById(R.id.process_due_on);
        dueOn.setText(DateFormat.getDateFormat(getActivity()).format(dueAt.getTime()));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VALIDATION
    // ///////////////////////////////////////////////////////////////////////////
    private void createProcess()
    {
        Map<String, Serializable> variables = new HashMap<String, Serializable>();
        if (dueAt != null)
        {
            variables.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, DateUtils.format(dueAt));
        }
        variables.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, priority);
        variables.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, titleTask.getText().toString().trim());
        variables.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, hasEmailNotification());
        if (!isAdhoc)
        {
            variables.put(WorkflowModel.PROP_REQUIRED_APPROVE_PERCENT, calculateApprovalPercent());
        }

        // Assignees
        List<Person> people = new ArrayList<Person>(assignees.values());
        
        //Items
        List<Document> attachments = new ArrayList<Document>(items.values());

        // Start an adhoc process with no items
        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new StartProcessRequest(processDefinition, people, variables, attachments).setNotificationTitle(
                titleTask.getText().toString()).setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));

        OperationWaitingDialogFragment.newInstance(StartProcessRequest.TYPE_ID, R.drawable.ic_action_inbox_light,
                getString(R.string.process_starting), null, null, 0).show(getActivity().getFragmentManager(),
                OperationWaitingDialogFragment.TAG);

        BatchOperationManager.getInstance(getActivity()).enqueue(group);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private boolean hasEmailNotification()
    {
        return emailNotification.isChecked();
    }

    private double calculateApprovalPercent()
    {
        double approvalValue = approvers;
        return (approvalValue / assignees.size()) * 100;
    }

    private OnTouchListener priorityClickListener = new OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (v.getId())
            {
                case R.id.action_priority_low:
                    priority = WorkflowModel.PRIORITY_LOW;
                    bM.setPressed(false);
                    bH.setPressed(false);
                    break;
                case R.id.action_priority_medium:
                    priority = WorkflowModel.PRIORITY_MEDIUM;
                    bL.setPressed(false);
                    bH.setPressed(false);
                    break;
                case R.id.action_priority_high:
                    priority = WorkflowModel.PRIORITY_HIGH;
                    bL.setPressed(false);
                    bM.setPressed(false);
                    break;
                default:
                    break;
            }
            Button b = (Button) v;
            b.setPressed(true);
            return true;
        }
    };

    private void updatePriority()
    {
        int id = R.id.action_priority_medium;
        switch (priority)
        {
            case WorkflowModel.PRIORITY_HIGH:
                id = R.id.action_priority_high;
                bL.setPressed(false);
                bM.setPressed(false);
                bH.setPressed(true);
                break;
            case WorkflowModel.PRIORITY_LOW:
                bM.setPressed(false);
                bH.setPressed(false);
                bL.setPressed(true);
                break;
            case WorkflowModel.PRIORITY_MEDIUM:
                bL.setPressed(false);
                bH.setPressed(false);
                bM.setPressed(true);
                break;
            default:
                break;
        }
    }

    private void updateApprovers()
    {
        if (approvers == assignees.size())
        {
            addApprover.setEnabled(false);
            removeApprover.setEnabled(true);
        }
        else if (approvers == 1)
        {
            removeApprover.setEnabled(false);
            addApprover.setEnabled(true);
        }
        else if (approvers == 0)
        {
            removeApprover.setEnabled(false);
            addApprover.setEnabled(false);
        }
        else
        {
            addApprover.setEnabled(true);
            removeApprover.setEnabled(true);
        }

        approversEditText.setHint(String.format(getResources()
                .getQuantityString(R.plurals.process_approvers, approvers), approvers, assignees.size()));
    }

    private void updateAssignees()
    {
        EditText edt = (EditText) vRoot.findViewById(R.id.process_assignee);
        edt.setHint(String.format(getResources().getQuantityString(R.plurals.process_assignees, assignees.size()),
                assignees.size()));

        if (assignees.size() > 0 && titleTask.getText().length() > 0)
        {
            validation.setEnabled(true);
        }
        else
        {
            validation.setEnabled(false);
        }

        if (assignees.size() > 0 && !isAdhoc && approvers == 0)
        {
            approvers = 1;
            updateApprovers();
        }
    }

    private TextWatcher watcher = new TextWatcher()
    {
        public void afterTextChanged(Editable s)
        {
            if (s.length() > 0)
            {
                validation.setEnabled(true);
                if (UIUtils.hasInvalidName(s.toString().trim()))
                {
                    ((View) errorMessage.getParent()).setVisibility(View.VISIBLE);
                    errorMessage.setText(R.string.filename_error_character);
                    validation.setEnabled(false);
                }
                else
                {
                    ((View) errorMessage.getParent()).setVisibility(View.GONE);
                    if (assignees.isEmpty())
                    {
                        validation.setEnabled(false);
                    }
                }
            }
            else
            {
                validation.setEnabled(false);
                ((View) errorMessage.getParent()).setVisibility(View.GONE);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // PERSON PICKER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onSelect(Map<String, Person> p)
    {
        if (p == null) { return; }
        assignees.putAll(p);

        // Update assignees
        updateAssignees();

        // Update approvers

    }

    @Override
    public Map<String, Person> retrieveSelection()
    {
        return assignees;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    public class StartProcessReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());

            if (getActivity() == null) { return; }

            if (intent.getExtras() != null)
            {
                if (intent.getAction().equals(IntentIntegrator.ACTION_START_PROCESS_COMPLETED))
                {
                    getActivity().finish();
                    return;
                }

            }
        }
    }
}
