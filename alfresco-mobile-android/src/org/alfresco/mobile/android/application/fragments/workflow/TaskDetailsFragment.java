package org.alfresco.mobile.android.application.fragments.workflow;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.api.utils.WorkflowUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.person.PersonSearchFragment;
import org.alfresco.mobile.android.application.fragments.person.onPickPersonFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.workflow.process.complete.StartProcessRequest;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.delegate.ReassignTaskRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class TaskDetailsFragment extends BaseFragment implements OnTabChangeListener, onPickPersonFragment
{

    public static final String TAG = TaskDetailsFragment.class.getName();

    private static final String TAB_SELECTED = "tabSelected";

    private static final String ARGUMENT_TASK = "paramTask";

    private View vRoot;

    private Task currentTask;

    private TabHost mTabHost;

    protected Integer tabSelected = null;

    protected Integer tabSelection = null;

    private TaskDetailsFragmentReceiver receiver;

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

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);

        container.setVisibility(View.VISIBLE);
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        vRoot = inflater.inflate(R.layout.app_task_tabpanes, container, false);

        if (alfSession == null) { return vRoot; }

        currentTask = (Task) getArguments().get(ARGUMENT_TASK);
        if (currentTask == null) { return null; }

        // TAB
        if (savedInstanceState != null)
        {
            tabSelection = savedInstanceState.getInt(TAB_SELECTED);
            savedInstanceState.remove(TAB_SELECTED);
        }

        // Header
        TextView tv = (TextView) vRoot.findViewById(R.id.title);
        tv.setText(currentTask.getDescription());
        tv = (TextView) vRoot.findViewById(R.id.details);
        createBottomHeader(currentTask, tv);

        // TabHost
        mTabHost = (TabHost) vRoot.findViewById(android.R.id.tabhost);
        setupTabs();

        return vRoot;
    }

    private void createBottomHeader(Task currentTask, TextView tv)
    {
        StringBuilder builder = new StringBuilder();
        switch (currentTask.getPriority())
        {
            case 1:
                builder.append(getString(R.string.workflow_priority_high));
                break;
            case 2:
                builder.append(getString(R.string.workflow_priority_medium));
                break;
            case 3:
                builder.append(getString(R.string.workflow_priority_low));
                break;
            default:
                break;
        }
        builder.append("   -   ");
        builder.append(currentTask.getName());
        if (currentTask.getDueAt() != null)
        {
            builder.append("   -   ");
            if (currentTask.getDueAt().before(new GregorianCalendar()))
            {
                builder.append("<b>");
                builder.append("<font color='#9F000F'>");
                builder.append(Formatter.formatToRelativeDate(getActivity(), currentTask.getDueAt().getTime()));
                builder.append("</font>");
                builder.append("</b>");
            }
            else
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM");
                builder.append(formatter.format(currentTask.getDueAt().getTime()));
            }
        }
        tv.setText(Html.fromHtml(builder.toString()), TextView.BufferType.SPANNABLE);
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
    // TAB MENU
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (tabSelected != null)
        {
            outState.putInt(TAB_SELECTED, tabSelected);
        }
    }

    private static final String TAB_ACTIONS = "Actions";

    private static final String TAB_ITEMS = "Items";

    private static final String TAB_TASKS = "Persons";

    private static final String TAB_WORKFLOW = "Workflow";

    private void setupTabs()
    {
        if (mTabHost == null) { return; }

        mTabHost.setup();
        mTabHost.setOnTabChangedListener(this);

        int stringId = R.string.task_actions;
        if (currentTask.getEndedAt() != null)
        {
            stringId = R.string.variables;
        }
        mTabHost.addTab(newTab(TAB_ACTIONS, stringId, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_ITEMS, R.string.task_items, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_TASKS, R.string.tasks, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_WORKFLOW, R.string.task_workflow, android.R.id.tabcontent));

        if (tabSelection != null)
        {
            if (tabSelection == 0) { return; }
            mTabHost.setCurrentTab(tabSelection);
        }
    }

    private TabSpec newTab(String tag, int labelId, int tabContentId)
    {
        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setContent(tabContentId);
        tabSpec.setIndicator(this.getText(labelId));
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId)
    {
        if (TAB_ACTIONS.equals(tabId))
        {
            tabSelected = 0;
            addTaskAction(currentTask);
        }
        else if (TAB_TASKS.equals(tabId))
        {
            tabSelected = 2;
            addTasks(currentTask);
        }
        else if (TAB_ITEMS.equals(tabId))
        {
            tabSelected = 1;
            addItems(currentTask);
        }
        else if (TAB_WORKFLOW.equals(tabId))
        {
            tabSelected = 3;
            addWorkflow(currentTask);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TAB MENU ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void addWorkflow(Task t)
    {
        BaseFragment frag = ProcessDetailsFragment.newInstance(t);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, android.R.id.tabcontent, ProcessDetailsFragment.TAG,
                false);
    }

    private void addItems(Task currentTask)
    {
        BaseFragment frag = ItemsFragment.newInstance(currentTask);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, android.R.id.tabcontent, ItemsFragment.TAG, false);
    }

    private void addTasks(Task currentTask)
    {
        BaseFragment frag = ProcessTasksFragment.newInstance(currentTask.getProcessIdentifier());
        frag.setSession(alfSession);
        FragmentDisplayer
                .replaceFragment(getActivity(), frag, android.R.id.tabcontent, ProcessTasksFragment.TAG, false);
    }

    private void addTaskAction(Task currentTask)
    {
        BaseFragment frag = TaskActionFragment.newInstance(currentTask);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, android.R.id.tabcontent, TaskActionFragment.TAG, false);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu)
    {
        MenuItem mi;

        // If task is ended, no action associated
        if (currentTask.getEndedAt() != null) { return; }

        String processDefinitionKey = WorkflowUtils.getKeyFromProcessDefinitionId(currentTask
                .getProcessDefinitionIdentifier());

        // unclaim : I unassign myself (generally created by a pooled process)
        if (currentTask.getAssigneeIdentifier() != null
                && WorkflowModel.FAMILY_PROCESS_POOLED_REVIEW.contains(processDefinitionKey))
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_TASK_UNCLAIM, Menu.FIRST + MenuActionItem.MENU_TASK_UNCLAIM,
                    R.string.task_unclaim);
            mi.setIcon(R.drawable.ic_reassign);
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
            mi.setIcon(R.drawable.ic_reassign);
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
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    public class TaskDetailsFragmentReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());

            if (getActivity() == null) { return; }

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
