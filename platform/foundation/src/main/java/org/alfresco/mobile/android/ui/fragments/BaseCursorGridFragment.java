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
package org.alfresco.mobile.android.ui.fragments;

import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@TargetApi(11)
/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public abstract class BaseCursorGridFragment extends CommonGridFragment implements LoaderCallbacks<Cursor>
{
    public static final String TAG = BaseCursorGridFragment.class.getName();

    protected int loaderId;

    // /////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ////////////////////////////////////////////////////////////
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        if (displayAsList)
        {
            gv.setColumnWidth(getDPI(getResources().getDisplayMetrics(), 2000));
        }

        if (cursor.getCount() == 0)
        {
            prepareEmptyView(ev, (ImageView) ev.findViewById(R.id.empty_picture),
                    (TextView) ev.findViewById(R.id.empty_text),
                    (TextView) ev.findViewById(R.id.empty_text_description));
            displayEmptyView();
        }
        else
        {
            if (adapter == null)
            {
                adapter = onAdapterCreation();
                gv.setAdapter(adapter);
            }
            ((CursorAdapter) adapter).changeCursor(onChangeCursor(cursor));
        }
        setListShown(true);
        if (cursor.getCount() == 0)
        {
            ev.setVisibility(View.VISIBLE);
        }

        AccessibilityUtils.sendAccessibilityEvent(getActivity());
        refreshHelper.setRefreshComplete();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        if (adapter != null)
        {
            ((CursorAdapter) adapter).changeCursor(null);
        }
    }

    protected Cursor onChangeCursor(Cursor cursor)
    {
        return cursor;
    }

    // //////////////////////////////////////////////////////////////////////
    // REFRESH
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void refresh()
    {
        onPrepareRefresh();
        isFullLoad = Boolean.FALSE;
        hasmore = Boolean.FALSE;
        skipCount = 0;
        adapter = null;
        if (getArguments() == null) { return; }
        getLoaderManager().restartLoader(loaderId, getArguments(), this);
        getLoaderManager().getLoader(loaderId).forceLoad();
    }

    protected void refreshSilently()
    {
        isFullLoad = Boolean.FALSE;
        hasmore = Boolean.FALSE;
        skipCount = 0;
        adapter = null;
        if (getArguments() == null) { return; }
        getLoaderManager().restartLoader(loaderId, getArguments(), this);
        getLoaderManager().getLoader(loaderId).forceLoad();
    }
}
