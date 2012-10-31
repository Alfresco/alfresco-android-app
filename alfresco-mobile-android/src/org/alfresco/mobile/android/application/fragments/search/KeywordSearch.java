/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.search;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.search.SearchFragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class KeywordSearch extends SearchFragment
{

    public static final String TAG = "SearchFragment";

    public static KeywordSearch newInstance(String keywords)
    {
        KeywordSearch ssf = new KeywordSearch();
        ssf.setArguments(createBundleArgs(keywords));
        return ssf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        setActivateThumbnail(false);
        View v = inflater.inflate(R.layout.sdkapp_search, container, false);

        init(v, R.string.empty_child);

        final EditText searchForm = (EditText) v.findViewById(R.id.search_query);
        searchForm.setImeActionLabel(getString(R.string.action_search), KeyEvent.KEYCODE_ENTER);

        searchForm.setOnKeyListener(new OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    if (searchForm.getText().length() > 0)
                    {
                        search(searchForm.getText().toString(), true, false);
                    }
                    else
                    {
                        MessengerManager.showLongToast(getActivity(), getString(R.string.search_form_hint));
                    }
                    return true;
                }
                return false;
            }
        });
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        Node n = (Node) l.getItemAtPosition(position);
        if (n.isDocument())
        {
            // Show properties
            ((MainActivity) getActivity()).addPropertiesFragment(n);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        getActivity().setTitle(R.string.action_search);
    }

    public void onPause()
    {
        getActivity().invalidateOptionsMenu();
        super.onPause();
    }

}
