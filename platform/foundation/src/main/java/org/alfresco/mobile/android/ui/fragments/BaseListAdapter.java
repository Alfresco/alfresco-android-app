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
package org.alfresco.mobile.android.ui.fragments;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class BaseListAdapter<T, VH> extends ArrayAdapter<T>
{

    private static final String TAG = "BaseListAdapter";

    // ///////////////////////////////////////////////
    // ADAPTER
    // ///////////////////////////////////////////////
    public static final String DISPLAY_ICON = "org.alfresco.mobile.ui.adapter.display.icon";

    public static final int DISPLAY_ICON_NONE = 0;

    public static final int DISPLAY_ICON_DEFAULT = 1;

    public static final int DISPLAY_ICON_CREATOR = 2;

    public static final String DISPLAY_DATE = "org.alfresco.mobile.ui.adapter.display.date";

    public static final int DISPLAY_DATE_NONE = 0;

    public static final int DISPLAY_DATE_RELATIVE = 1;

    public static final int DISPLAY_DATE_DATETIME = 2;

    public static final int DISPLAY_DATE_DATE = 3;

    public static final int DISPLAY_DATE_TIME = 4;

    // ///////////////////////////////////////////////
    // MEMBERS
    // ///////////////////////////////////////////////
    protected int textViewResourceId;

    protected String vhClassName;

    protected int dateFormatType = DISPLAY_DATE_RELATIVE;

    protected int iconItemType = DISPLAY_ICON_DEFAULT;

    public BaseListAdapter(Context context, int textViewResourceId, List<T> objects)
    {
        super(context, textViewResourceId, objects);
        this.textViewResourceId = textViewResourceId;
        this.vhClassName = GenericViewHolder.class.getCanonicalName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = recycleOrCreateView(getContext(), convertView, textViewResourceId);
        VH vh = (VH) v.getTag();
        T item = getItem(position);
        updateControls(vh, item);
        return v;
    }

    protected void updateControls(VH vh, T item)
    {
        if (item != null && vh != null)
        {
            updateTopText(vh, item);
            updateBottomText(vh, item);
            updateIcon(vh, item);
        }
    }

    protected abstract void updateTopText(VH vh, T item2);

    protected abstract void updateBottomText(VH vh, T item2);

    protected abstract void updateIcon(VH vh, T item2);

    protected View recycleOrCreateView(Context c, View v, int layoutId)
    {
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(layoutId, null);
            VH vh = create(vhClassName, v);
            v.setTag(vh);
        }
        return v;
    }

    protected View createView(Context c, View v, int layoutId)
    {
        LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(layoutId, null);
        VH vh = create(vhClassName, v);
        v.setTag(vh);
        return v;
    }

    @SuppressWarnings("unchecked")
    protected VH create(String className, View v)
    {
        VH s = null;
        try
        {
            Class<?> c = Class.forName(className);
            Constructor<?> t = c.getDeclaredConstructor(View.class);
            s = (VH) t.newInstance(v);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return s;
    }

    public void setFragmentSettings(Bundle fragmentSettings)
    {
        if (fragmentSettings != null)
        {
            if (fragmentSettings.containsKey(DISPLAY_DATE))
            {
                dateFormatType = (Integer) fragmentSettings.get(DISPLAY_DATE);
            }
            if (fragmentSettings.containsKey(DISPLAY_ICON))
            {
                iconItemType = (Integer) fragmentSettings.get(DISPLAY_ICON);
            }
        }
    }

    public String formatDate(Context c, Date date)
    {
        switch (dateFormatType)
        {
            case DISPLAY_DATE_RELATIVE:
                return Formatter.formatToRelativeDate(getContext(), date);
            case DISPLAY_DATE_NONE:
                return "";
            case DISPLAY_DATE_DATE:
                return DateFormat.getLongDateFormat(c).format(date);
            case DISPLAY_DATE_DATETIME:
                return date.toLocaleString();
            case DISPLAY_DATE_TIME:
                return DateFormat.getTimeFormat(c).format(date);
            default:
                break;
        }
        return "";
    }
}
