/*******************************************************************************
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.browser.ProgressNodeAdapter;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

@TargetApi(11)
public abstract class BaseGridFragment extends BaseFragment
{

    /** Principal ListView of the fragment */
    protected GridView gv;

    /** Principal progress indicator displaying during loading of listview */
    protected ProgressBar pb;

    /** View displaying if no result inside the listView */
    protected View ev;

    /** Max Items for a single call */
    protected int maxItems;

    /** Skip count paramater during loading */
    protected int skipCount;

    /**
     * Indicator to retain position of first item currently display during
     * scrolling
     */
    protected int selectedPosition;

    /** Indicator to retain if everything has been loaded */
    protected boolean isFullLoad = Boolean.FALSE;

    protected View footer;

    protected ArrayAdapter<?> adapter;

    protected String title;

    public static final String TAG = "BaseEndlessList";

    protected Boolean hasmore = Boolean.FALSE;

    public static final String LOAD_STATE = "loadState";

    public static final int LOAD_NONE = 0;

    public static final int LOAD_AUTO = 1;

    public static final int LOAD_MANUAL = 2;

    public static final int LOAD_VISIBLE = 3;

    protected int loadState = LOAD_AUTO;

    protected Bundle bundle;

    protected int loaderId;

    protected LoaderCallbacks<?> callback;

    protected int emptyListMessageId;

    private boolean isLockVisibleLoader = Boolean.FALSE;

    public static final String ARGUMENT_GRID = "gridView";

    protected boolean initLoader = true;

    protected boolean checkSession = true;

    public static Bundle createBundleArgs(ListingContext lc, int loadState)
    {
        Bundle args = new Bundle();
        args.putSerializable(LOAD_STATE, loadState);
        args.putSerializable(ARGUMENT_GRID, lc);
        return args;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }
        View v = inflater.inflate(R.layout.sdk_grid, container, false);

        init(v, emptyListMessageId);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        checkSession(checkSession);
        if (initLoader)
        {
            continueLoading(loaderId, callback);
        }
    }

    protected void checkSession(boolean activate)
    {
        if (activate && alfSession == null)
        {
            MessengerManager.showToast(getActivity(), R.string.empty_session);
            setListShown(true);
            gv.setEmptyView(ev);
            return;
        }
    }

    public String getTitle()
    {
        return title;
    }

    /**
     * Control whether the list is being displayed.
     * 
     * @param shown : If true, the list view is shown; if false, the progress
     *            indicator. The initial value is true.
     */
    protected void setListShown(Boolean shown)
    {
        if (shown)
        {
            gv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }
        else
        {
            ev.setVisibility(View.GONE);
            gv.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);
        }
    }

    protected final void savePosition()
    {
        if (gv != null)
        {
            selectedPosition = gv.getFirstVisiblePosition();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        savePosition();
        super.onSaveInstanceState(outState);
    }

    /**
     * Affect a clickListener to the principal ListView.
     */
    public void setOnItemClickListener(OnItemClickListener clickListener)
    {
        gv.setOnItemClickListener(clickListener);
    }

    protected void init(View v, int estring)
    {
        pb = (ProgressBar) v.findViewById(R.id.progressbar);
        gv = (GridView) v.findViewById(R.id.gridview);
        ev = v.findViewById(R.id.empty);
        TextView evt = (TextView) v.findViewById(R.id.empty_text);
        evt.setText(estring);

        if (adapter != null)
        {
            if (adapter.getCount() == 0)
            {
                gv.setEmptyView(ev);
            }
            else
            {
                gv.setAdapter(adapter);
                gv.setSelection(selectedPosition);
            }

        }

        gv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id)
            {
                savePosition();
                BaseGridFragment.this.onListItemClick((GridView) l, v, position, id);
            }
        });

        gv.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id)
            {
                return BaseGridFragment.this.onItemLongClick((GridView) l, v, position, id);
            }
        });

        gv.setOnScrollListener(new OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                savePosition();
                if (firstVisibleItem + visibleItemCount == totalItemCount && loadState == LOAD_VISIBLE
                        && !isLockVisibleLoader)
                {
                    loadMore();
                    isLockVisibleLoader = Boolean.TRUE;
                }
            }
        });
    }

    public void onListItemClick(GridView l, View v, int position, long id)
    {
        // todo
    }

    public boolean onItemLongClick(GridView l, View v, int position, long id)
    {
        return false;
    }

    protected void continueLoading(int loaderId, LoaderCallbacks<?> callback)
    {
        if (!isFullLoad && loadState == LOAD_AUTO)
        {
            getLoaderManager().initLoader(loaderId, getArguments(), callback);
            if (getLoaderManager().getLoader(loaderId) != null)
            {
                getLoaderManager().getLoader(loaderId).forceLoad();
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void calculateSkipCount(ListingContext lc)
    {
        if (lc != null)
        {
            skipCount = lc.getSkipCount();
            maxItems = lc.getMaxItems();
            if (hasmore)
            {
                skipCount = (adapter != null) ? (((ArrayAdapter<Object>) adapter)).getCount() : lc.getSkipCount()
                        + lc.getMaxItems();
            }
            lc.setSkipCount(skipCount);
        }
    }

    protected void reload(Bundle b, int loaderId, LoaderCallbacks<?> callback)
    {
        isFullLoad = Boolean.FALSE;
        hasmore = Boolean.FALSE;
        skipCount = 0;
        if (adapter != null)
        {
            adapter.clear();
        }
        adapter = null;

        if (getLoaderManager().getLoader(loaderId) == null)
        {
            getLoaderManager().initLoader(loaderId, b, callback);
        }
        else
        {
            getLoaderManager().restartLoader(loaderId, b, callback);
        }
        getLoaderManager().getLoader(loaderId).forceLoad();
    }

    protected void refresh(int loaderId, LoaderCallbacks<?> callback)
    {
        isFullLoad = Boolean.FALSE;
        hasmore = Boolean.FALSE;
        skipCount = 0;
        adapter = null;
        if (getArguments() == null) { return; }
        getLoaderManager().restartLoader(loaderId, getArguments(), callback);
        getLoaderManager().getLoader(loaderId).forceLoad();
    }

    protected boolean checkException(LoaderResult<?> result)
    {
        return (result.getException() != null);
    }

    /**
     * Override this method to handle an exception coming back from the server.
     * 
     * @param e : exception raised by the client API.
     */
    public void onLoaderException(Exception e)
    {
        MessengerManager.showToast(getActivity(), e.getMessage());
        setListShown(true);
    }

    @SuppressWarnings("unchecked")
    protected void displayPagingData(PagingResult<?> data, int loaderId, LoaderCallbacks<?> callback)
    {
        if (!isFullLoad)
        {
            if ((data == null || data.getTotalItems() == 0 || data.getList().isEmpty()) && !hasmore)
            {
                gv.setEmptyView(ev);
                isFullLoad = Boolean.TRUE;
                if (adapter != null)
                {
                    gv.setAdapter(null);
                }
                // Log.d("BaseListFragment", "ITEMS : Empty !");
            }
            else
            {
                if (!isDataPresent(data))
                {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
                    {
                        ((ArrayAdapter<Object>) adapter).addAll(data.getList());
                    }
                    else
                    {
                        for (Object item : data.getList())
                        {
                            ((ArrayAdapter<Object>) adapter).add(item);
                        }
                    }
                    hasmore = data.hasMoreItems();
                    // Log.d("BrowserFragment", hasmore + " - Total Items : " +
                    // data.getTotalItems() + " Results : "
                    // + data.getList().size() + " Adapter " +
                    // ((ArrayAdapter<Object>) adapter).getCount());
                    if (doesLoadMore())
                    {
                        loadMore();
                    }
                    gv.setAdapter(adapter);
                }
            }
            setListShown(true);
        }
        if (selectedPosition != 0)
        {
            gv.setSelection(selectedPosition);
        }
    }

    protected void displayEmptyView()
    {
        gv.setEmptyView(ev);
        isFullLoad = Boolean.TRUE;
        if (adapter != null)
        {
            gv.setAdapter(null);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isDataPresent(PagingResult<?> data)
    {
        ArrayAdapter<Object> arrayAdapter = ((ArrayAdapter<Object>) adapter);
        if (arrayAdapter.isEmpty())
        {
            return false;
        }
        else
        {
            return !(data.getList() != null && !data.getList().contains(
                    arrayAdapter.getItem(arrayAdapter.getCount() - 1)));
        }
    }

    public void refreshListView()
    {
        if (adapter != null && adapter instanceof ProgressNodeAdapter)
        {
            ((ProgressNodeAdapter) adapter).setContext(getActivity());
        }
        gv.setAdapter(adapter);
    }

    private boolean doesLoadMore()
    {
        boolean loadMore = Boolean.FALSE;

        switch (loadState)
        {
            case LOAD_MANUAL:
                isFullLoad = Boolean.FALSE;
                if (!hasmore)
                {
                    isFullLoad = Boolean.TRUE;
                }
                break;
            case LOAD_AUTO:
                if (hasmore)
                {
                    loadMore = Boolean.TRUE;
                    isFullLoad = Boolean.FALSE;
                }
                else
                {
                    loadMore = Boolean.FALSE;
                    isFullLoad = Boolean.TRUE;
                }
                break;
            case LOAD_NONE:
                isFullLoad = Boolean.TRUE;
                break;
            case LOAD_VISIBLE:
                isFullLoad = Boolean.FALSE;
                if (!hasmore)
                {
                    isFullLoad = Boolean.TRUE;
                }
                else
                {
                    isLockVisibleLoader = Boolean.FALSE;
                }
                break;
            default:
                break;
        }
        return loadMore;
    }

    protected void loadMore()
    {
        getLoaderManager().restartLoader(loaderId, bundle, callback);
        if (getLoaderManager().getLoader(loaderId) != null)
        {
            getLoaderManager().getLoader(loaderId).forceLoad();
        }
    }

    protected boolean checkSession()
    {
        if (alfSession == null)
        {
            MessengerManager.showToast(getActivity(), R.string.empty_session);
            setListShown(true);
            gv.setEmptyView(ev);
            return true;
        }
        return false;
    }

    protected static ListingContext copyListing(ListingContext lco)
    {
        if (lco == null) { return null; }
        ListingContext lci = new ListingContext();
        lci.setIsSortAscending(lco.isSortAscending());
        lci.setMaxItems(lco.getMaxItems());
        lci.setSkipCount(lco.getSkipCount());
        lci.setSortProperty(lco.getSortProperty());
        return lci;
    }

    public void setColumnWidth(int value)
    {
        gv.setColumnWidth(value);
    }
}
