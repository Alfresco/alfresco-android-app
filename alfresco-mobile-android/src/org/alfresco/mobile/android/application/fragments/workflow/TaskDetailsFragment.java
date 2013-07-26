package org.alfresco.mobile.android.application.fragments.workflow;

import org.alfresco.mobile.android.api.model.workflow.Task;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TaskDetailsFragment extends BaseFragment
{

    public static final String TAG = TaskDetailsFragment.class.getName();

    private static final String PARAM_TASK = "paramTask";

    private View vRoot;

    private Task currentTask;

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
        b.putSerializable(PARAM_TASK, task);
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
        vRoot = inflater.inflate(R.layout.app_task_onepanel, container, false);

        if (alfSession == null) { return vRoot; }

        currentTask = (Task) getArguments().get(PARAM_TASK);
        if (currentTask == null) { return null; }
        
        // Header
        TextView tv = (TextView) vRoot.findViewById(R.id.title);
        tv.setText(currentTask.getDescription());
        tv = (TextView) vRoot.findViewById(R.id.details);
        tv.setText(currentTask.getName());

        return vRoot;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
    }
}
