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
package org.alfresco.mobile.android.ui.workflow.task;

import org.alfresco.mobile.android.ui.template.ListingTemplate;

public interface TasksTemplate extends ListingTemplate
{
    // ////////////////////////////////////////////////////////////////
    // FILTER : STATUS
    // ////////////////////////////////////////////////////////////////
    String FILTER_KEY_STATUS = "status";

    /** All tasks are returned. */
    String FILTER_STATUS_ANY = "any";

    /** Only active (not completed) tasks are returned. */
    String FILTER_STATUS_ACTIVE = "active";

    /** Only completed tasks are returned. */
    String FILTER_STATUS_COMPLETE = "complete";

    // ////////////////////////////////////////////////////////////////
    // FILTER : DUE DATE
    // ////////////////////////////////////////////////////////////////
    String FILTER_KEY_DUE = "due";

    /** Only tasks which are due today. */
    String FILTER_DUE_TODAY = "today";

    /** Only tasks which are due tomorrow. */
    String FILTER_DUE_TOMORROW = "tomorrow";

    /** Only tasks which are due in the next 7 days. */
    String FILTER_DUE_7DAYS = "week";

    /** Only tasks which are overdue. */
    String FILTER_DUE_OVERDUE = "overdue";

    /** Only tasks with no due date. */
    String FILTER_DUE_NODATE = "none";

    // ////////////////////////////////////////////////////////////////
    // FILTER : PRIORITY
    // ////////////////////////////////////////////////////////////////
    String FILTER_KEY_PRIORITY = "priority";

    /** Only tasks with a low priority. */
    String FILTER_PRIORITY_LOW = "low";

    /** Only tasks with a medium priority. */
    String FILTER_PRIORITY_MEDIUM = "medium";

    /** Only tasks with a high priority. */
    String FILTER_PRIORITY_HIGH = "high";

    // ////////////////////////////////////////////////////////////////
    // FILTER : ASSIGNEE
    // ////////////////////////////////////////////////////////////////
    String FILTER_KEY_ASSIGNEE = "assignee";

    /** Only tasks explicitly assign to the current user. */
    String FILTER_ASSIGNEE_ME = "me";

    /** Only unassigned tasks current user can claim (member of the group) */
    String FILTER_ASSIGNEE_UNASSIGNED = "unassigned";

    /**
     * tasks assigned to the current user and unassigned task current user can
     * claim (member of the group)
     */
    String FILTER_ASSIGNEE_ALL = "all";

    /** tasks not assigned to the current user */
    String FILTER_NO_ASSIGNEE = "none";

    // ////////////////////////////////////////////////////////////////
    // FILTER : INITIATOR
    // ////////////////////////////////////////////////////////////////
    String FILTER_KEY_INITIATOR = "initiator";

    /** Only tasks explicitly assign to the current user. */
    String FILTER_INITIATOR_ME = "me";

    /** tasks assigned to anybody. */
    String FILTER_INITIATOR_ANY = "any";

}
