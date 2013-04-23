/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments;

import java.lang.reflect.Constructor;

import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public abstract class BaseCursorLoader<VH> extends CursorAdapter
{
    private static final String TAG = BaseCursorLoader.class.getName();

    protected int layoutResourceId;

    protected String vhClassName;
    
    protected Context context;

    public BaseCursorLoader(Context context, Cursor c, int layoutId)
    {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
        this.layoutResourceId = layoutId;
        this.vhClassName = GenericViewHolder.class.getCanonicalName();
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(layoutResourceId, null);
        VH vh = create(vhClassName, view);
        updateControls(vh, cursor);
        view.setTag(vh);
        return view;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        VH vh = (VH) view.getTag();
        updateControls(vh, cursor);
    }

    private void updateControls(VH vh, Cursor cursor)
    {
        if (vh != null)
        {
            updateTopText(vh, cursor);
            updateBottomText(vh, cursor);
            updateIcon(vh, cursor);
        }
    }

    protected abstract void updateTopText(VH vh, Cursor cursor);

    protected abstract void updateBottomText(VH vh, Cursor cursor);

    protected abstract void updateIcon(VH vh, Cursor cursor);
    
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

}
