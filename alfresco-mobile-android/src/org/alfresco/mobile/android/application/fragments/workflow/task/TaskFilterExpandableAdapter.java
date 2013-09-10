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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;
import org.alfresco.mobile.android.ui.utils.ViewHolder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class TaskFilterExpandableAdapter extends BaseExpandableListAdapter
{
    LayoutInflater inflater;

    private Map<Integer, Integer> selectedItems = new HashMap<Integer, Integer>(4);

    public TaskFilterExpandableAdapter(Activity context, Map<Integer, Integer> selectedItems)
    {
        if (selectedItems != null)
        {
            this.selectedItems = selectedItems;
        }
        inflater = context.getLayoutInflater();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GROUP
    // ///////////////////////////////////////////////////////////////////////////
    public Object getGroup(int groupPosition)
    {
        return FAMILY.get(groupPosition);
    }

    public int getGroupCount()
    {
        return FAMILY.size();
    }

    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        Integer itemGroup = (Integer) getGroup(groupPosition);

        GenericViewHolder vh = null;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.sdk_list_row, null);
            vh = new GenericViewHolder(convertView);
            convertView.setTag(vh);
        }

        vh = (GenericViewHolder) convertView.getTag();
        vh.topText.setText(itemGroup);
        vh.choose.setImageResource(isExpanded ? R.drawable.expander_close_holo_light
                : R.drawable.expander_open_holo_light);

        int iconId = -1;
        switch (itemGroup)
        {
            case R.string.tasks_status:
                iconId = R.drawable.ic_validate;
                break;
            case R.string.tasks_due:
                iconId = R.drawable.ic_calendar_pick;
                break;
            case R.string.tasks_priority:
                iconId = R.drawable.ic_priority_medium;
                break;
            case R.string.tasks_assignee:
                iconId = R.drawable.ic_person;
                break;
            default:
                break;
        }
        vh.icon.setImageResource(iconId);

        if (selectedItems.containsKey(FAMILY.get(groupPosition)))
        {
            vh.bottomText.setText(selectedItems.get(FAMILY.get(groupPosition)));
        }
        else
        {
            vh.bottomText.setText("");
        }

        return convertView;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ITEM
    // ///////////////////////////////////////////////////////////////////////////
    public Object getChild(int groupPosition, int childPosition)
    {
        return FAMILY_LIST.get(FAMILY.get(groupPosition)).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent)
    {
        final Integer item = (Integer) getChild(groupPosition, childPosition);

        CheckBoxViewHolder vh = null;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.app_list_checkbox_row, null);
            vh = new CheckBoxViewHolder(convertView);
            convertView.setTag(vh);
        }

        vh = (CheckBoxViewHolder) convertView.getTag();
        vh.topText.setText(item);
        vh.bottomText.setVisibility(View.GONE);
        if (selectedItems.get(FAMILY.get(groupPosition)) == item)
        {
            vh.checkBox.setChecked(true);
        }
        else
        {
            vh.checkBox.setChecked(false);
        }

        return convertView;
    }

    public int getChildrenCount(int groupPosition)
    {
        return FAMILY_LIST.get(FAMILY.get(groupPosition)).size();
    }

    public boolean hasStableIds()
    {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public void select(View v, int groupPosition, int childPosition)
    {
        Integer selected = (Integer) getChild(groupPosition, childPosition);
        CheckBoxViewHolder vh = (CheckBoxViewHolder) v.getTag();
        if (!vh.checkBox.isChecked())
        {
            vh.checkBox.setChecked(true);
            // Check if other selected item of the same family
            selectedItems.put(FAMILY.get(groupPosition), selected);
        }
        else
        {
            vh.checkBox.setChecked(false);
            selectedItems.remove(FAMILY.get(groupPosition));
        }
        notifyDataSetChanged();
    }

    public Map<Integer, Integer> getSelectedItems()
    {
        return selectedItems;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TASKS FILTERS
    // ///////////////////////////////////////////////////////////////////////////
    private static final List<Integer> FAMILY_DUE_DATE = new ArrayList<Integer>(5)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.string.tasks_due_today);
            add(R.string.tasks_due_tomorrow);
            add(R.string.tasks_due_week);
            add(R.string.tasks_due_over);
            add(R.string.tasks_due_no_date);
        }
    };

    private static final List<Integer> FAMILY_TASKS = new ArrayList<Integer>(2)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.string.tasks_active);
            add(R.string.tasks_completed);
        }
    };

    private static final List<Integer> FAMILY_ASSIGNEE = new ArrayList<Integer>(2)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.string.tasks_assignee_me);
            add(R.string.tasks_assignee_unassigned);
            add(R.string.tasks_assignee_all);
        }
    };

    private static final List<Integer> FAMILY_PRIORITY = new ArrayList<Integer>(3)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.string.tasks_priority_low);
            add(R.string.tasks_priority_medium);
            add(R.string.tasks_priority_high);
        }
    };

    private static final List<Integer> FAMILY = new ArrayList<Integer>(4)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.string.tasks_status);
            add(R.string.tasks_due);
            add(R.string.tasks_priority);
            add(R.string.tasks_assignee);
        }
    };

    private static final Map<Integer, List<Integer>> FAMILY_LIST = new LinkedHashMap<Integer, List<Integer>>(4)
    {
        private static final long serialVersionUID = 1L;
        {
            put(R.string.tasks_status, FAMILY_TASKS);
            put(R.string.tasks_due, FAMILY_DUE_DATE);
            put(R.string.tasks_priority, FAMILY_PRIORITY);
            put(R.string.tasks_assignee, FAMILY_ASSIGNEE);
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL CLASS
    // ///////////////////////////////////////////////////////////////////////////
    private static class CheckBoxViewHolder extends ViewHolder
    {
        public TextView topText;

        public TextView bottomText;

        public CheckBox checkBox;

        public CheckBoxViewHolder(View v)
        {
            super(v);
            topText = (TextView) v.findViewById(R.id.toptext);
            bottomText = (TextView) v.findViewById(R.id.bottomtext);
            checkBox = (CheckBox) v.findViewById(R.id.checkbox);
        }
    }
}
