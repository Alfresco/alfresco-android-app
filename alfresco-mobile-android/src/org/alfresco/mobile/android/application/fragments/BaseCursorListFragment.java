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

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.operations.OperationCursorAdapter;
import org.alfresco.mobile.android.application.integration.OperationContentProvider;
import org.alfresco.mobile.android.application.integration.OperationSchema;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseCursorListFragment extends DialogFragment implements LoaderCallbacks<Cursor>
{
    /** Principal ListView of the fragment */
    protected ListView lv;

    /** Principal progress indicator displaying during loading of listview */
    protected ProgressBar pb;

    /** View displaying if no result inside the listView */
    protected View ev;

    protected CursorAdapter adapter;

    protected int emptyListMessageId = R.string.emtpy;

    protected int layoutId = R.layout.sdk_list;

    protected int title = R.string.app_name;

    // /////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }
        View v = inflater.inflate(layoutId, container, false);
        init(v, emptyListMessageId);
        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(layoutId, null);
        init(v, emptyListMessageId);
        return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v).create();
    }

    // /////////////////////////////////////////////////////////////
    // UTILS
    // ////////////////////////////////////////////////////////////
    protected void init(View v, int estring)
    {
        pb = (ProgressBar) v.findViewById(R.id.progressbar);
        lv = (ListView) v.findViewById(R.id.listView);
        ev = v.findViewById(R.id.empty);
        TextView evt = (TextView) v.findViewById(R.id.empty_text);
        evt.setText(estring);

        if (adapter != null)
        {
            if (adapter.getCount() == 0)
            {
                lv.setEmptyView(ev);
            }
            else
            {
                lv.setAdapter(adapter);
            }

        }

        lv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id)
            {
                BaseCursorListFragment.this.onListItemClick((ListView) l, v, position, id);
            }
        });

        lv.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id)
            {
                return BaseCursorListFragment.this.onItemLongClick((ListView) l, v, position, id);
            }
        });
    }

    protected void setListShown(Boolean shown)
    {
        if (shown)
        {
            lv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }
        else
        {
            ev.setVisibility(View.GONE);
            lv.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);
        }
    }

    protected void setEmptyShown(Boolean shown)
    {
        if (shown)
        {
            lv.setEmptyView(ev);
            lv.setVisibility(View.GONE);
            ev.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }
        else
        {
            ev.setVisibility(View.GONE);
            lv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }
    }

    public void refreshListView()
    {
        lv.setAdapter(adapter);
    }

    public void onListItemClick(ListView l, View v, int position, long id)
    {

    }

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        return false;
    }

    // /////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ////////////////////////////////////////////////////////////
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        adapter.changeCursor(cursor);
        if (cursor.getCount() == 0)
        {
            setEmptyShown(true);
        }
        else
        {
            setEmptyShown(false);
        }
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        adapter.changeCursor(null);
    }

}
