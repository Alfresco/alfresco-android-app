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
package org.alfresco.mobile.android.application.fragments.workflow.task;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.api.model.impl.ProcessImpl;
import org.alfresco.mobile.android.api.model.impl.TaskImpl;
import org.alfresco.mobile.android.api.services.impl.publicapi.PublicAPIWorkflowServiceImpl;
import org.alfresco.mobile.android.api.utils.WorkflowUtils;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.person.PersonProfileFragment;
import org.alfresco.mobile.android.application.fragments.person.PersonSearchFragment;
import org.alfresco.mobile.android.application.fragments.person.onPickPersonFragment;
import org.alfresco.mobile.android.application.fragments.workflow.ItemsLoader;
import org.alfresco.mobile.android.application.fragments.workflow.ProcessDiagramFragment;
import org.alfresco.mobile.android.application.fragments.workflow.process.ProcessTasksFragment;
import org.alfresco.mobile.android.application.fragments.workflow.process.ProcessesAdapter;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.workflow.process.start.StartProcessRequest;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.complete.CompleteTaskRequest;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.delegate.ReassignTaskRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskDetailsFragment extends BaseFragment implements onPickPersonFragment,
        LoaderCallbacks<LoaderResult<PagingResult<Document>>>
{

    public static final String TAG = TaskDetailsFragment.class.getName();

    private static final String ARGUMENT_TASK = "paramTask";

    private static final String ARGUMENT_PROCESS = "TaskProcess";

    private static final String ACTION_ATTACHMENTS_COMPLETED = "Attachments";

    private View vRoot;

    private Task currentTask;

    private Process currentProcess;

    private TaskDetailsFragmentReceiver receiver;

    private EditText comment;

    private boolean isReviewTask = false;

    private Person initiator;

    private RenditionManager renditionManager;

    private List<Document> items;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public TaskDetailsFragment()
    {
    }

    public static TaskDetailsFragment newInstance(Task task)
    {
        TaskDetailsFragment bf = new TaskDetailsFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARGUMENT_TASK, task);
        bf.setArguments(b);
        return bf;
    }

    public static TaskDetailsFragment newInstance(Process process)
    {
        TaskDetailsFragment bf = new TaskDetailsFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARGUMENT_PROCESS, process);
        bf.setArguments(b);
        return bf;
    };

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);

        container.setVisibility(View.VISIBLE);
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        vRoot = inflater.inflate(R.layout.app_task_details, container, false);

        if (alfSession == null) { return vRoot; }

        currentTask = (Task) getArguments().get(ARGUMENT_TASK);
        currentProcess = (Process) getArguments().get(ARGUMENT_PROCESS);
        if (currentTask == null && currentProcess == null) { return null; }

        // Init variable depending on object
        initVariables();

        // Rendition Manager
        renditionManager = ApplicationManager.getInstance(getActivity()).getRenditionManager(getActivity());

        // Header
        TextView tv = (TextView) vRoot.findViewById(R.id.title);
        tv.setText(description);

        // Other parts
        initHeader();
        initCompleteForm(inflater);
        initInitiator();

        if (items == null)
        {
            getActivity().getLoaderManager().restartLoader(ItemsLoader.ID, null, this);
        }
        else
        {
            diplayAttachment();
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
            UIUtils.displayTitle(getActivity(), R.string.details);
        }
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_TASK_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_TASK_DELEGATE_COMPLETED);
        intentFilter.addAction(ACTION_ATTACHMENTS_COMPLETED);
        receiver = new TaskDetailsFragmentReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop()
    {
        getActivity().invalidateOptionsMenu();
        super.onStop();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INIT
    // ///////////////////////////////////////////////////////////////////////////
    private String description;

    private String type;

    private int priority;

    private GregorianCalendar endedAt;

    private GregorianCalendar dueAt;

    private String processId;

    private String processDefinitionId;

    private void initVariables()
    {
        if (currentTask != null)
        {
            description = currentTask.getDescription();
            priority = currentTask.getPriority();
            endedAt = currentTask.getEndedAt();
            if (((TaskImpl) currentTask).getData().containsKey(OnPremiseConstant.WORKFLOWINSTANCE_VALUE))
            {
                Process p = (Process) ((TaskImpl) currentTask).getData().get(OnPremiseConstant.WORKFLOWINSTANCE_VALUE);
                initiator = (Person) ((ProcessImpl) p).getData().get(OnPremiseConstant.INITIATOR_VALUE);
            }
            type = currentTask.getName();
            dueAt = currentTask.getDueAt();
            processId = currentTask.getProcessIdentifier();
            processDefinitionId = currentTask.getProcessDefinitionIdentifier();
        }
        else if (currentProcess != null)
        {
            description = currentProcess.getDescription() != null ? currentProcess.getDescription()
                    : getString(R.string.process_no_description);
            priority = currentProcess.getPriority();
            endedAt = currentProcess.getEndedAt();
            initiator = (Person) ((ProcessImpl) currentProcess).getData().get(OnPremiseConstant.INITIATOR_VALUE);
            type = ProcessesAdapter.getName(getActivity(), currentProcess.getKey());
            dueAt = ((ProcessImpl) currentProcess).getDueAt();
            processId = currentProcess.getIdentifier();
            processDefinitionId = currentProcess.getDefinitionIdentifier();
        }
    }

    public void initCompleteForm(LayoutInflater inflater)
    {
        if (currentProcess != null)
        {
            vRoot.findViewById(R.id.complete_group).setVisibility(View.GONE);
            return;
        }

        if (currentTask != null && endedAt == null)
        {

            View validation = vRoot.findViewById(R.id.action_approve);
            View reject = vRoot.findViewById(R.id.action_reject);
            comment = (EditText) vRoot.findViewById(R.id.task_comment);

            if (WorkflowModel.TASK_REVIEW.equals(currentTask.getKey())
                    || WorkflowModel.TASK_ACTIVITI_REVIEW.equals(currentTask.getKey()))
            {
                isReviewTask = true;
                reject.setVisibility(View.VISIBLE);
            }
            else
            {
                reject.setVisibility(View.GONE);
                if (validation instanceof Button)
                {
                    ((Button) validation).setText(R.string.done);
                }
            }

            validation.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    completeTask(currentTask, isReviewTask, true);
                }
            });

            reject.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    completeTask(currentTask, isReviewTask, false);
                }
            });
        }
        else
        {
            vRoot.findViewById(R.id.complete_group).setVisibility(View.GONE);

        }
    }

    private void initInitiator()
    {
        // Display Initiator
        if (initiator != null)
        {
            // ImageView preview = (ImageView)
            // vRoot.findViewById(R.id.task_initiator_icon);
            // int iconId = R.drawable.ic_person;
            // renditionManager.display((ImageView) preview,
            // initiator.getIdentifier(), iconId);

            LinearLayout layout = (LinearLayout) vRoot.findViewById(R.id.task_initiator_group);
            layout.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    PersonProfileFragment.newInstance(initiator.getIdentifier()).show(getFragmentManager(),
                            PersonProfileFragment.TAG);
                }
            });

            TextView tv = (TextView) vRoot.findViewById(R.id.task_initiator);
            tv.setText(initiator.getFullName());
        }
        else
        {
            vRoot.findViewById(R.id.task_initiator_group).setVisibility(View.GONE);
            vRoot.findViewById(R.id.task_initiator_icon).setVisibility(View.GONE);
        }
    }

    private void initHeader()
    {
        // PRIORITY
        ImageView icon = (ImageView) vRoot.findViewById(R.id.task_priority_icon);
        TextView textValue = (TextView) vRoot.findViewById(R.id.task_priority);

        icon.setImageDrawable(getResources().getDrawable(TasksAdapter.getPriorityIconId(priority)));
        int labelId = R.string.workflow_priority_medium;
        switch (priority)
        {
            case WorkflowModel.PRIORITY_HIGH:
                labelId = R.string.workflow_priority_high;
                break;
            case WorkflowModel.PRIORITY_MEDIUM:
                labelId = R.string.workflow_priority_medium;
                break;
            case WorkflowModel.PRIORITY_LOW:
                labelId = R.string.workflow_priority_low;
                break;
            default:
                break;
        }
        textValue.setText(labelId);

        // TASK TYPE
        textValue = (TextView) vRoot.findViewById(R.id.task_type);
        textValue.setText(type);

        // DUE DATE
        StringBuilder builder = new StringBuilder();
        if (dueAt != null)
        {
            textValue = (TextView) vRoot.findViewById(R.id.task_due_date);
            if (dueAt.before(new GregorianCalendar()))
            {
                builder.append("<b>");
                builder.append("<font color='#9F000F'>");
                builder.append(DateFormat.getLongDateFormat(getActivity()).format(dueAt.getTime()));
                builder.append("</font>");
                builder.append("</b>");
            }
            else
            {
                builder.append(DateFormat.getLongDateFormat(getActivity()).format(dueAt.getTime()));
            }
            textValue.setText(builder.toString());
            textValue.setText(Html.fromHtml(builder.toString()), TextView.BufferType.SPANNABLE);
        }
        else
        {
            vRoot.findViewById(R.id.task_due_date_group).setVisibility(View.GONE);
            vRoot.findViewById(R.id.task_due_date_icon).setVisibility(View.GONE);
        }
    }

    private void diplayAttachment()
    {
        vRoot.findViewById(R.id.attachments_waiting).setVisibility(View.GONE);
        LinearLayout ll = (LinearLayout) vRoot.findViewById(R.id.attachments);
        if (items == null || items.isEmpty())
        {
            ImageView iv = new ImageView(getActivity());
            iv.setScaleType(ScaleType.FIT_CENTER);
            iv.setImageResource(R.drawable.mime_empty_doc);
            ll.addView(iv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            TextView tv = new TextView(getActivity());
            tv.setText(R.string.process_no_attachments);
            tv.setGravity(Gravity.CENTER);
            ll.addView(tv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            ll.setGravity(Gravity.CENTER);
            return;
        }

        LayoutInflater li = LayoutInflater.from(getActivity());
        View vr = null;
        TextView tv = null;
        for (Node node : items)
        {
            vr = li.inflate(R.layout.app_task_item_row, ll, false);
            tv = (TextView) vr.findViewById(R.id.toptext);
            tv.setText(node.getName());
            tv = (TextView) vr.findViewById(R.id.bottomtext);
            tv.setText(createContentBottomText(getActivity(), node));
            ll.addView(vr, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            ImageView iv = (ImageView) vr.findViewById(R.id.icon);
            renditionManager.display(iv, node, MimeTypeManager.getIcon(node.getName(), true), ScaleType.FIT_CENTER);
            vr.setTag(node);
            vr.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Node item = (Node) v.getTag();
                    ((MainActivity) getActivity()).addPropertiesFragment(item.getIdentifier(), true);
                }
            });
        }
    }

    private String createContentBottomText(Context context, Node node)
    {
        String s = "";

        if (node.getCreatedAt() != null)
        {
            s = Formatter.formatToRelativeDate(context, node.getCreatedAt().getTime());
            if (node.isDocument())
            {
                Document doc = (Document) node;
                s += " - " + Formatter.formatFileSize(context, doc.getContentStreamLength());
            }
        }
        return s;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void completeTask(Task task, boolean isReviewTask, boolean isApprove)
    {
        // Prepare Variables
        Map<String, Serializable> variables = new HashMap<String, Serializable>(3);
        if (isReviewTask)
        {
            String outcome = (isApprove) ? WorkflowModel.TRANSITION_APPROVE : WorkflowModel.TRANSITION_REJECT;
            if (!(alfSession.getServiceRegistry().getWorkflowService() instanceof PublicAPIWorkflowServiceImpl))
            {
                outcome = (task.getProcessDefinitionIdentifier().startsWith(WorkflowModel.KEY_PREFIX_ACTIVITI)) ? outcome
                        : outcome.toLowerCase();
            }
            variables.put(WorkflowModel.PROP_REVIEW_OUTCOME, outcome);
        }

        if (comment.getText().length() > 0)
        {
            variables.put(WorkflowModel.PROP_COMMENT, comment.getText().toString());
        }

        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new CompleteTaskRequest(task, variables).setNotificationTitle(task.getName())
                .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));

        OperationWaitingDialogFragment.newInstance(CompleteTaskRequest.TYPE_ID, R.drawable.ic_validate,
                getString(R.string.task_completing), null, null, 0).show(getActivity().getFragmentManager(),
                OperationWaitingDialogFragment.TAG);

        BatchOperationManager.getInstance(getActivity()).enqueue(group);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu)
    {
        MenuItem mi;

        String processDefinitionKey = WorkflowUtils.getKeyFromProcessDefinitionId(processDefinitionId);

        if (endedAt == null && processDefinitionKey.startsWith(WorkflowModel.KEY_PREFIX_ACTIVITI))
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_PROCESS_DETAILS, Menu.FIRST
                    + MenuActionItem.MENU_PROCESS_DETAILS, R.string.process_diagram);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_PROCESS_HISTORY, Menu.FIRST + MenuActionItem.MENU_PROCESS_HISTORY,
                R.string.tasks_history);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (currentTask == null || endedAt != null) { return; }

        // unclaim : I unassign myself (generally created by a pooled process)
        if (currentTask.getAssigneeIdentifier() != null
                && WorkflowModel.FAMILY_PROCESS_POOLED_REVIEW.contains(processDefinitionKey))
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_TASK_UNCLAIM, Menu.FIRST + MenuActionItem.MENU_TASK_UNCLAIM,
                    R.string.task_unclaim);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        // reassign : I have a task and I decide I dont want to be responsible
        // anymore of this task so I reassign to a specific person
        else if (currentTask.getAssigneeIdentifier() != null)
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_TASK_REASSIGN, Menu.FIRST + MenuActionItem.MENU_TASK_REASSIGN,
                    R.string.task_reassign);
            mi.setIcon(R.drawable.ic_reassign);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        // claim : I assign to me an unassigned task (created by a pooled
        // process)
        else if (currentTask.getAssigneeIdentifier() == null
                && WorkflowModel.FAMILY_PROCESS_POOLED_REVIEW.contains(processDefinitionKey))
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_TASK_CLAIM, Menu.FIRST + MenuActionItem.MENU_TASK_CLAIM,
                    R.string.task_claim);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public void reassign()
    {
        PersonSearchFragment.newInstance(ListingModeFragment.MODE_PICK, TAG, true).show(
                getActivity().getFragmentManager(), PersonSearchFragment.TAG);
    }

    public void claim()
    {
        // Start claim
        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new ReassignTaskRequest(currentTask, alfSession.getPersonIdentifier(), true)
                .setNotificationTitle(currentTask.getName()).setNotificationVisibility(
                        OperationRequest.VISIBILITY_DIALOG));

        OperationWaitingDialogFragment.newInstance(StartProcessRequest.TYPE_ID, R.drawable.ic_reassign,
                getString(R.string.task_reassign), null, null, 0).show(getActivity().getFragmentManager(),
                OperationWaitingDialogFragment.TAG);

        BatchOperationManager.getInstance(getActivity()).enqueue(group);
    }

    public void unclaim()
    {
        // Start unclaim
        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new ReassignTaskRequest(currentTask, alfSession.getPersonIdentifier(), false)
                .setNotificationTitle(currentTask.getName()).setNotificationVisibility(
                        OperationRequest.VISIBILITY_DIALOG));

        OperationWaitingDialogFragment.newInstance(StartProcessRequest.TYPE_ID, R.drawable.ic_reassign,
                getString(R.string.task_reassign), null, null, 0).show(getActivity().getFragmentManager(),
                OperationWaitingDialogFragment.TAG);

        BatchOperationManager.getInstance(getActivity()).enqueue(group);
    }

    public void showProcessDiagram()
    {
        BaseFragment frag = ProcessDiagramFragment.newInstance(processId);
        frag.setSession(alfSession);
        frag.show(getFragmentManager(), ProcessDiagramFragment.TAG);
    }

    public void displayHistory()
    {
        BaseFragment frag = ProcessTasksFragment.newInstance(processId);
        frag.setSession(alfSession);
        frag.show(getActivity().getFragmentManager(), ProcessTasksFragment.TAG);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REASSIGN
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onSelect(Map<String, Person> mapPerson)
    {
        Person delegatePerson = null;
        for (Entry<String, Person> assignee : mapPerson.entrySet())
        {
            delegatePerson = assignee.getValue();
            break;
        }

        // Start reassign
        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new ReassignTaskRequest(currentTask, delegatePerson).setNotificationTitle(currentTask.getName())
                .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));

        OperationWaitingDialogFragment.newInstance(StartProcessRequest.TYPE_ID, R.drawable.ic_reassign,
                getString(R.string.task_reassign), null, null, 0).show(getActivity().getFragmentManager(),
                OperationWaitingDialogFragment.TAG);

        BatchOperationManager.getInstance(getActivity()).enqueue(group);
    }

    @Override
    public Map<String, Person> retrieveSelection()
    {
        return new HashMap<String, Person>(1);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult<Document>>> onCreateLoader(int id, Bundle ba)
    {

        if (currentTask != null)
        {
            return new ItemsLoader(getActivity(), alfSession, currentTask);
        }
        else if (currentProcess != null)
        {
            return new ItemsLoader(getActivity(), alfSession, currentProcess);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Document>>> arg0,
            LoaderResult<PagingResult<Document>> results)
    {
        if (results.hasException())
        {

        }
        else
        {
            if (items != null)
            {
                items.clear();
            }
            items = results.getData().getList();
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ACTION_ATTACHMENTS_COMPLETED));
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult<Document>>> arg0)
    {
        // TODO Auto-generated method stub
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    public class TaskDetailsFragmentReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());

            if (getActivity() == null) { return; }

            if (ACTION_ATTACHMENTS_COMPLETED.equals(intent.getAction()))
            {
                diplayAttachment();
                return;
            }

            if (intent.getExtras() != null)
            {
                TaskDetailsFragment detailsFragment = (TaskDetailsFragment) getFragmentManager().findFragmentByTag(
                        TaskDetailsFragment.TAG);

                Bundle b = intent.getExtras().getParcelable(IntentIntegrator.EXTRA_DATA);
                if (b == null) { return; }
                Task _task = (Task) b.getSerializable(IntentIntegrator.EXTRA_TASK);
                Task task = (Task) detailsFragment.getArguments().get(TaskDetailsFragment.ARGUMENT_TASK);
                if (task == null || _task == null) { return; }

                if ((intent.getAction().equals(IntentIntegrator.ACTION_TASK_COMPLETED) || intent.getAction().equals(
                        IntentIntegrator.ACTION_TASK_DELEGATE_COMPLETED))
                        && _task.getIdentifier().equals(task.getIdentifier()))
                {
                    if (DisplayUtils.hasCentralPane(getActivity()))
                    {
                        FragmentDisplayer.removeFragment(getActivity(), TaskDetailsFragment.TAG);
                    }
                    else
                    {
                        getFragmentManager().popBackStack(TaskDetailsFragment.TAG,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                    return;
                }

            }
        }
    }
}
