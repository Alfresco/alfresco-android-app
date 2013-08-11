package org.alfresco.mobile.android.application.fragments.workflow;

import org.alfresco.mobile.android.api.model.ListingFilter;
import org.alfresco.mobile.android.api.services.WorkflowService;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;

public final class TasksHelper
{

    public  static final String TASK_FILTER_PREFS = "org.alfresco.mobile.android.tasks.preferences";

    private static final String TASK_FILTER_DEFAULT = "org.alfresco.mobile.android.tasks.preferences.filter.default";

    
    private TasksHelper()
    {
    }

    public static void displayNavigationMode(final Activity activity, final boolean backStack, int menuId)
    {
        activity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        TasksShortCutAdapter adapter = new TasksShortCutAdapter(activity);

        OnNavigationListener mOnNavigationListener = new OnNavigationListener()
        {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId)
            {
                SharedPreferences prefs = activity.getSharedPreferences(TASK_FILTER_PREFS, 0);
                int currentSelection = prefs.getInt(TASK_FILTER_DEFAULT, 1);

                if (!backStack && itemPosition == currentSelection) { return true; }
                
                ListingFilter f = new ListingFilter();
                switch (itemPosition)
                {
                    case 0:
                        f.addFilter(WorkflowService.FILTER_STATUS, WorkflowService.FILTER_STATUS_ACTIVE);
                        break;
                    case 1:
                        f.addFilter(WorkflowService.FILTER_STATUS, WorkflowService.FILTER_STATUS_COMPLETE);
                        break;
                    case 2:
                        f.addFilter(WorkflowService.FILTER_PRIORITY, WorkflowService.FILTER_PRIORITY_HIGH);
                        break;
                    case 3:
                        f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_TODAY);
                        break;
                    case 4:
                        f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_OVERDUE);
                    case 5:
                        f.addFilter(WorkflowService.FILTER_ASSIGNEE, SessionUtils.getSession(activity)
                                .getPersonIdentifier());
                        break;
                    case 6:
                        Fragment taskFragment = TaskFilterFragment.newInstance();
                        FragmentDisplayer.replaceFragment(activity, taskFragment,
                                DisplayUtils.getLeftFragmentId(activity), TaskFilterFragment.TAG, true);
                        return true;
                    default:
                        break;
                }
                
                if (!backStack)
                {
                    activity.getFragmentManager().popBackStack();
                }

                Fragment taskFragment = TasksFragment.newInstance(f, itemPosition);
                FragmentDisplayer.replaceFragment(activity, taskFragment, DisplayUtils.getLeftFragmentId(activity),
                        TasksFragment.TAG, false);
                
                prefs.edit().putInt(TASK_FILTER_DEFAULT, itemPosition).commit();

                
                return true;
            }
        };
        activity.getActionBar().setListNavigationCallbacks(adapter, mOnNavigationListener);
        int currentSelection = menuId;
        activity.getActionBar().setSelectedNavigationItem(currentSelection);
    }
}
