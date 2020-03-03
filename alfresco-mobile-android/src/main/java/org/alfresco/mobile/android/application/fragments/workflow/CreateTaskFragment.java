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
package org.alfresco.mobile.android.application.fragments.workflow;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.api.session.impl.RepositorySessionImpl;
import org.alfresco.mobile.android.api.utils.DateUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.user.UserPickerCallback;
import org.alfresco.mobile.android.application.fragments.user.UserSearchFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment.onPickDateFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DocumentPickerFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DocumentPickerFragment.onPickDocumentFragment;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.workflow.process.start.StartProcessEvent;
import org.alfresco.mobile.android.async.workflow.process.start.StartProcessRequest;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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
import android.widget.ToggleButton;

import com.squareup.otto.Subscribe;

public class CreateTaskFragment extends AlfrescoFragment
        implements UserPickerCallback, onPickDocumentFragment, onPickDateFragment
{
    public static final String TAG = CreateTaskFragment.class.getName();

    private static final String ARGUMENT_PROCESS_DEFINITION = "processDefinition";

    private ProcessDefinition processDefinition;

    private GregorianCalendar dueAt;

    private Map<String, Person> assignees = new HashMap<String, Person>(1);

    private Map<String, Node> items = new HashMap<String, Node>(1);

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

    private Button assigneesButton;

    private Button attachmentsButton;

    private Button dueOn;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateTaskFragment()
    {
        screenName = AnalyticsManager.SCREEN_TASK_CREATE_FORM;
    }

    public static CreateTaskFragment newInstanceByTemplate(Bundle b)
    {
        CreateTaskFragment bf = new CreateTaskFragment();
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
        if (getArguments() == null || !getArguments().containsKey(ARGUMENT_PROCESS_DEFINITION)) { return null; }
        processDefinition = (ProcessDefinition) getArguments().getSerializable(ARGUMENT_PROCESS_DEFINITION);

        if (getArguments() != null && getArguments().containsKey(PrivateIntent.EXTRA_DOCUMENTS))
        {
            @SuppressWarnings("unchecked")
            List<Document> docs = (List<Document>) getArguments().get(PrivateIntent.EXTRA_DOCUMENTS);
            for (Node document : docs)
            {
                items.put(document.getIdentifier(), document);
            }
            getArguments().remove(PrivateIntent.EXTRA_DOCUMENTS);
        }

        setRetainInstance(true);
        checkSession();
        setRootView(inflater.inflate(R.layout.app_start_process, container, false));
        if (getSession() == null) { return getRootView(); }

        // DESCRIPTION
        titleTask = (EditText) viewById(R.id.process_title);
        if (items != null && items.size() > 0 && titleTask.getText().length() == 0)
        {
            if (items.size() == 1)
            {
                List<Node> attachments = new ArrayList<Node>(items.values());
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
        ImageButton ib = (ImageButton) viewById(R.id.action_process_due_on);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment.newInstance("0", TAG).show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });
        dueOn = (Button) viewById(R.id.process_due_on);
        if (dueAt != null)
        {
            dueOn.setText(DateFormat.getDateFormat(getActivity()).format(dueAt.getTime()));
        }
        else
        {
            dueOn.setText(getString(R.string.tasks_due_no_date));
        }

        Button dueOn = (Button) viewById(R.id.process_due_on);
        dueOn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment.newInstance("0", TAG).show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });

        // ASSIGNEES
        ib = (ImageButton) viewById(R.id.action_process_assignee);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startPersonPicker();
            }
        });
        assigneesButton = (Button) viewById(R.id.process_assignee);
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
            hide(R.id.process_approvers_group);
            hide(R.id.process_approvers_group_title);
            isAdhoc = true;
        }
        else
        {
            approversEditText = (EditText) viewById(R.id.process_approvers);
            removeApprover = (ImageButton) viewById(R.id.action_remove_approvers);
            addApprover = (ImageButton) viewById(R.id.action_add_approvers);
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
        ib = (ImageButton) viewById(R.id.action_process_attachments);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startDocumentPicker();
            }
        });
        attachmentsButton = (Button) viewById(R.id.process_attachments);
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
        bM = (ToggleButton) viewById(R.id.action_priority_medium);
        bM.setOnTouchListener(priorityClickListener);
        bL = (ToggleButton) viewById(R.id.action_priority_low);
        bL.setOnTouchListener(priorityClickListener);
        bH = (ToggleButton) viewById(R.id.action_priority_high);
        bH.setOnTouchListener(priorityClickListener);
        updatePriority();

        // VALIDATION
        validation = UIUtils.initValidation(getRootView(), R.string.done, true);
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
        emailNotification = viewById(R.id.action_send_notification);

        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        checkSession();
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
        super.onResume();

        dueOn = (Button) viewById(R.id.process_due_on);
        if (dueAt != null)
        {
            dueOn.setText(DateFormat.getDateFormat(getActivity()).format(dueAt.getTime()));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onDatePicked(String dateId, GregorianCalendar gregorianCalendar)
    {
        if (getSession() instanceof RepositorySessionImpl && ((RepositorySessionImpl) getSession()).hasPublicAPI())
        {
            gregorianCalendar.set(Calendar.HOUR_OF_DAY, 00);
            gregorianCalendar.set(Calendar.MINUTE, 00);
            gregorianCalendar.set(Calendar.SECOND, 00);
            gregorianCalendar.set(Calendar.MILLISECOND, 000);
        }
        else
        {
            gregorianCalendar.set(Calendar.HOUR_OF_DAY, 23);
            gregorianCalendar.set(Calendar.MINUTE, 59);
            gregorianCalendar.set(Calendar.SECOND, 59);
            gregorianCalendar.set(Calendar.MILLISECOND, 999);
        }
        dueAt = gregorianCalendar;
        Button dueOn = (Button) viewById(R.id.process_due_on);
        dueOn.setText(DateFormat.getDateFormat(getActivity()).format(dueAt.getTime()));
    }

    @Override
    public void onDateClear(String dateId)
    {
        dueAt = null;
        dueOn.setText(getString(R.string.tasks_due_no_date));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VALIDATION
    // ///////////////////////////////////////////////////////////////////////////
    private void createProcess()
    {
        Map<String, Serializable> variables = new HashMap<String, Serializable>();

        if (dueAt != null)
        {
            if (getSession() instanceof RepositorySessionImpl && ((RepositorySessionImpl) getSession()).hasPublicAPI())
            {
                variables.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, DateUtils.formatISO(dueAt));
            }
            else
            {
                variables.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, DateUtils.format(dueAt));
            }
        }
        variables.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, priority);
        variables.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, titleTask.getText().toString().trim());
        variables.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, hasEmailNotification());
        if (!isAdhoc)
        {
            variables.put(WorkflowModel.PROP_REQUIRED_APPROVE_PERCENT, calculateApprovalPercent());
        }

        // Assignees
        List<Person> persons = new ArrayList<Person>(assignees.values());

        // Items
        List<Node> attachments = new ArrayList<>(items.values());

        // Start process
        String operationId = Operator.with(getActivity(), getAccount())
                .load(new StartProcessRequest.Builder(processDefinition, persons, variables, attachments));

        // Display waiting dialog
        OperationWaitingDialogFragment
                .newInstance(StartProcessRequest.TYPE_ID, R.drawable.ic_task_light,
                        getString(R.string.process_starting), null, null, 0, operationId)
                .show(getActivity().getSupportFragmentManager(), OperationWaitingDialogFragment.TAG);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private boolean hasEmailNotification()
    {
        return ((Switch) emailNotification).isChecked();
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
    public void onPersonSelected(Map<String, Person> p)
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
    public void onNodeSelected(String fieldId, Map<String, Node> p)
    {
        if (p == null) { return; }
        items.clear();
        for (Entry<String, Node> entry : p.entrySet())
        {
            items.put(entry.getKey(), entry.getValue());
        }

        // Update documents
        updateDocuments();
        getActivity().getSupportFragmentManager().popBackStackImmediate(DocumentPickerFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onNodeClear(String fieldId)
    {
        items.clear();
    }

    @Override
    public Map<String, Node> getNodeSelected(String fieldId)
    {
        return new HashMap<String, Node>(items);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onProcessStarted(StartProcessEvent event)
    {
        getActivity().finish();
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
        UserSearchFragment.with(getActivity()).fragmentTag(TAG).singleChoice(isAdhoc)
                .mode(ListingModeFragment.MODE_PICK).display();
    }

    public void startDocumentPicker()
    {
        DocumentPickerFragment.with(getActivity()).display();
    }

    public List<Node> getAttachments()
    {
        return new ArrayList<Node>(items.values());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            this.extraConfiguration = new Bundle();
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder processDefinition(ProcessDefinition processDefinition)
        {
            extraConfiguration.putSerializable(ARGUMENT_PROCESS_DEFINITION, processDefinition);
            return this;
        }

        public Builder documents(ArrayList<? extends Parcelable> documents)
        {
            extraConfiguration.putParcelableArrayList(PrivateIntent.EXTRA_DOCUMENTS, documents);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE FRAGMENT
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}
