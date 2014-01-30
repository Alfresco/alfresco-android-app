/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.api.utils.DateUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.commons.utils.AndroidVersion;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.browser.onPickDocumentFragment;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.person.PersonSearchFragment;
import org.alfresco.mobile.android.application.fragments.person.onPickPersonFragment;
import org.alfresco.mobile.android.application.fragments.workflow.DatePickerFragment.onPickDateFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.workflow.process.start.StartProcessRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.ToggleButton;

public class CreateTaskFragment extends BaseFragment implements onPickPersonFragment, onPickDocumentFragment,
        onPickDateFragment
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

    private ToggleButton bM, bL, bH;

    private boolean isAdhoc = false;

    private EditText titleTask;

    private Button validation;

    private View emailNotification;

    private EditText approversEditText;

    private ImageButton removeApprover;

    private ImageButton addApprover;

    private StartProcessReceiver receiver;

    private Button assigneesButton;

    private Button attachmentsButton;

    private Button dueOn;

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

    public static CreateTaskFragment newInstance(ProcessDefinition processDefinition, Bundle b)
    {
        CreateTaskFragment bf = new CreateTaskFragment();
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

        if (getArguments() != null && getArguments().containsKey(IntentIntegrator.EXTRA_DOCUMENTS))
        {
            @SuppressWarnings("unchecked")
            List<Document> docs = (List<Document>) getArguments().get(IntentIntegrator.EXTRA_DOCUMENTS);
            for (Document document : docs)
            {
                items.put(document.getIdentifier(), document);
            }
            getArguments().remove(IntentIntegrator.EXTRA_DOCUMENTS);
        }

        setRetainInstance(true);
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        vRoot = inflater.inflate(R.layout.app_start_process, container, false);
        if (alfSession == null) { return vRoot; }

        // DESCRIPTION
        titleTask = (EditText) vRoot.findViewById(R.id.process_title);
        if (items != null && items.size() > 0 && titleTask.getText().length() == 0)
        {
            if (items.size() == 1)
            {
                List<Document> attachments = new ArrayList<Document>(items.values());
                titleTask.setText(String.format(getString(R.string.task_review_document),
                        MimeTypeManager.getName(attachments.get(0).getName())));
            }
            else
            {
                titleTask.setText(getString(R.string.task_review_documents));
            }
        }

        titleTask.addTextChangedListener(watcher);

        // DatePicker
        ImageButton ib = (ImageButton) vRoot.findViewById(R.id.action_process_due_on);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment.newInstance(0, TAG).show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });
        dueOn = (Button) vRoot.findViewById(R.id.process_due_on);
        if (dueAt != null)
        {
            dueOn.setText(DateFormat.getDateFormat(getActivity()).format(dueAt.getTime()));
        }

        Button b = (Button) vRoot.findViewById(R.id.process_due_on);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new DatePickerFragment().show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });
        b.setText(getString(R.string.tasks_due_no_date));

        // ASSIGNEES
        ib = (ImageButton) vRoot.findViewById(R.id.action_process_assignee);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startPersonPicker();
            }
        });
        assigneesButton = (Button) vRoot.findViewById(R.id.process_assignee);
        assigneesButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CreateTaskPickerFragment.newInstance(CreateTaskPickerFragment.MODE_PERSON).show(getFragmentManager(),
                        CreateTaskPickerFragment.TAG);
            }
        });
        assigneesButton.setText(MessageFormat.format(getString(R.string.task_assignees_plurals), assignees.size()));

        // APPROVERS
        if (WorkflowModel.FAMILY_PROCESS_ADHOC.contains(processDefinition.getKey()))
        {
            vRoot.findViewById(R.id.process_approvers_group).setVisibility(View.GONE);
            if (vRoot.findViewById(R.id.process_approvers_group_title) != null)
            {
                vRoot.findViewById(R.id.process_approvers_group_title).setVisibility(View.GONE);
            }
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
                startDocumentPicker();
            }
        });
        attachmentsButton = (Button) vRoot.findViewById(R.id.process_attachments);
        attachmentsButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CreateTaskPickerFragment.newInstance(CreateTaskPickerFragment.MODE_DOCUMENT).show(getFragmentManager(),
                        CreateTaskPickerFragment.TAG);
            }
        });
        attachmentsButton.setText(MessageFormat.format(getString(R.string.task_attachments_plurals), items.size()));

        // PRIORITY
        bM = (ToggleButton) vRoot.findViewById(R.id.action_priority_medium);
        bM.setOnTouchListener(priorityClickListener);
        bL = (ToggleButton) vRoot.findViewById(R.id.action_priority_low);
        bL.setOnTouchListener(priorityClickListener);
        bH = (ToggleButton) vRoot.findViewById(R.id.action_priority_high);
        bH.setOnTouchListener(priorityClickListener);
        updatePriority();

        // VALIDATION
        validation = UIUtils.initValidation(vRoot, R.string.done, true);
        validation.setEnabled(false);
        validation.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createProcess();
            }
        });

        // Email Notification
        if (AndroidVersion.isICSOrAbove())
        {
            emailNotification = (Switch) vRoot.findViewById(R.id.action_send_notification);
        }
        else
        {
            emailNotification = (CheckBox) vRoot.findViewById(R.id.action_send_notification);
        }

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
            UIUtils.displayTitle(getActivity(), R.string.task_create);
        }
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_START_PROCESS_COMPLETED);
        receiver = new StartProcessReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);

        super.onResume();

        dueOn = (Button) vRoot.findViewById(R.id.process_due_on);
        if (dueAt != null)
        {
            dueOn.setText(DateFormat.getDateFormat(getActivity()).format(dueAt.getTime()));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onDatePicked(int dateId, GregorianCalendar gregorianCalendar)
    {
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, 23);
        gregorianCalendar.set(Calendar.MINUTE, 59);
        gregorianCalendar.set(Calendar.SECOND, 59);
        gregorianCalendar.set(Calendar.MILLISECOND, 999);
        dueAt = gregorianCalendar;
        Button dueOn = (Button) vRoot.findViewById(R.id.process_due_on);
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

        // Items
        List<Document> attachments = new ArrayList<Document>(items.values());

        // Start an adhoc process with no items
        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new StartProcessRequest(processDefinition, people, variables, attachments).setNotificationTitle(
                titleTask.getText().toString()).setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));

        OperationWaitingDialogFragment.newInstance(StartProcessRequest.TYPE_ID, R.drawable.ic_action_inbox,
                getString(R.string.process_starting), null, null, 0).show(getActivity().getFragmentManager(),
                OperationWaitingDialogFragment.TAG);

        BatchOperationManager.getInstance(getActivity()).enqueue(group);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private boolean hasEmailNotification()
    {
        if (AndroidVersion.isICSOrAbove())
        {
            return ((Switch) emailNotification).isChecked();
        }
        else
        {
            return ((CheckBox) emailNotification).isChecked();
        }
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
                    bM.setChecked(false);
                    bM.setTextColor(Color.BLACK);
                    bH.setChecked(false);
                    bH.setTextColor(Color.BLACK);
                    break;
                case R.id.action_priority_medium:
                    priority = WorkflowModel.PRIORITY_MEDIUM;
                    bL.setChecked(false);
                    bL.setTextColor(Color.BLACK);
                    bH.setChecked(false);
                    bH.setTextColor(Color.BLACK);
                    break;
                case R.id.action_priority_high:
                    priority = WorkflowModel.PRIORITY_HIGH;
                    bL.setChecked(false);
                    bL.setTextColor(Color.BLACK);
                    bM.setChecked(false);
                    bM.setTextColor(Color.BLACK);
                    break;
                default:
                    break;
            }
            ToggleButton b = (ToggleButton) v;
            b.setChecked(true);
            b.setTextColor(Color.WHITE);
            return true;
        }
    };

    private void updatePriority()
    {
        switch (priority)
        {
            case WorkflowModel.PRIORITY_HIGH:
                bL.setChecked(false);
                bL.setTextColor(Color.BLACK);
                bM.setChecked(false);
                bM.setTextColor(Color.BLACK);
                bH.setChecked(true);
                bH.setTextColor(Color.WHITE);
                break;
            case WorkflowModel.PRIORITY_LOW:
                bM.setChecked(false);
                bM.setTextColor(Color.BLACK);
                bH.setChecked(false);
                bH.setTextColor(Color.BLACK);
                bL.setChecked(true);
                bL.setTextColor(Color.WHITE);
                break;
            case WorkflowModel.PRIORITY_MEDIUM:
                bL.setChecked(false);
                bL.setTextColor(Color.BLACK);
                bH.setChecked(false);
                bH.setTextColor(Color.BLACK);
                bM.setChecked(true);
                bM.setTextColor(Color.WHITE);
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

        approversEditText.setHint(String.format(
                MessageFormat.format(getString(R.string.process_approvers_plurals), assignees.size()), approvers));
    }

    private void updateAssignees()
    {
        assigneesButton.setText(MessageFormat.format(getString(R.string.task_assignees_plurals), assignees.size()));

        if (assignees.size() > 0 && titleTask.getText().length() > 0)
        {
            validation.setEnabled(true);
        }
        else
        {
            validation.setEnabled(false);
        }

        if (approvers > assignees.size())
        {
            approvers = assignees.size();
        }

        if (assignees.size() > 0 && !isAdhoc && approvers == 0)
        {
            approvers = 1;
        }
        if (!isAdhoc)
        {
            updateApprovers();
        }
    }

    private void updateDocuments()
    {
        attachmentsButton.setText(String.format(
                MessageFormat.format(getString(R.string.task_attachments_plurals), items.size()), items.size()));
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

                    titleTask.setError(getString(R.string.filename_error_character));
                    validation.setEnabled(false);
                }
                else
                {
                    titleTask.setError(null);
                    if (assignees.isEmpty())
                    {
                        validation.setEnabled(false);
                    }
                }
            }
            else
            {
                validation.setEnabled(false);
                titleTask.setError(null);
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
    }

    @Override
    public Map<String, Person> retrieveSelection()
    {
        return assignees;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DOCUMENT PICKER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onSelectDocument(List<Document> p)
    {
        if (p == null) { return; }
        items.clear();
        for (Node node : p)
        {
            items.put(node.getIdentifier(), (Document) node);
        }

        // Update documents
        updateDocuments();
        getActivity().getFragmentManager().popBackStackImmediate(CreateTaskDocumentPickerFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public Map<String, Document> retrieveDocumentSelection()
    {
        return new HashMap<String, Document>(items);
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

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void removeAssignee(Person item)
    {
        assignees.remove(item.getIdentifier());
        updateAssignees();
    }

    public void removeDocument(Document doc)
    {
        items.remove(doc.getIdentifier());
        updateDocuments();
    }

    public List<Person> getAssignees()
    {
        return new ArrayList<Person>(assignees.values());
    }

    public void startPersonPicker()
    {
        PersonSearchFragment frag = PersonSearchFragment.newInstance(ListingModeFragment.MODE_PICK, TAG, isAdhoc);
        FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                PersonSearchFragment.TAG, true);
    }

    public void startDocumentPicker()
    {
        CreateTaskDocumentPickerFragment frag = CreateTaskDocumentPickerFragment.newInstance();
        FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                CreateTaskDocumentPickerFragment.TAG, true);
    }

    public List<Node> getAttachments()
    {
        return new ArrayList<Node>(items.values());
    }
}
