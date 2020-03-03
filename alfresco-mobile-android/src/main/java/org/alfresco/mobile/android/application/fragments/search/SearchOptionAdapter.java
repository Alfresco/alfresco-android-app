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
package org.alfresco.mobile.android.application.fragments.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.providers.search.HistorySearch;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class SearchOptionAdapter extends ArrayAdapter<Integer>
{
    private Integer item;

    private AlfrescoAccount account;

    public SearchOptionAdapter(FragmentActivity context, int textViewResourceId, List<Integer> objects)
    {
        super(context, textViewResourceId, objects);
        this.account = SessionUtils.getAccount(context);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.app_path_shortcut, null);
        }
        item = getItem(position);
        if (item != null)
        {
            ((TextView) v.findViewById(R.id.bottomtext)).setText(ITEMS.get(item));
            v.findViewById(R.id.toptext).setVisibility(View.GONE);
            v.findViewById(R.id.icon).setVisibility(View.VISIBLE);
            ((ImageView) v.findViewById(R.id.icon)).setImageResource(ITEMS_ICONS.get(item));
        }
        return v;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.app_header_row, null);
        }
        item = getItem(position);
        if (item != null && v != null)
        {
            if (AlfrescoAccountManager.getInstance(getContext()).hasMultipleAccount())
            {
                ((TextView) v.findViewById(R.id.toptext)).setText(UIUtils.getAccountLabel(account));
                v.findViewById(R.id.toptext).setVisibility(View.VISIBLE);
            }
            else
            {
                v.findViewById(R.id.toptext).setVisibility(View.GONE);
            }

            ((TextView) v.findViewById(R.id.bottomtext)).setText(ITEMS.get(getItem(position)));
            v.findViewById(R.id.icon).setVisibility(View.GONE);
        }
        return v;
    }

    public static List<Integer> getSearchOptions(AlfrescoSession session, boolean searchInFolder)
    {
        if (session instanceof RepositorySession && !searchInFolder)
        {
            return SEARCH_OPTIONS;
        }
        else
        {
            return SEARCH_OPTIONS_CLOUD;
        }
    }

    private static final List<Integer> SEARCH_OPTIONS = new ArrayList<Integer>()
    {
        {
            add(HistorySearch.TYPE_DOCUMENT);
            add(HistorySearch.TYPE_FOLDER);
            add(HistorySearch.TYPE_PERSON);
            add(HistorySearch.TYPE_SITE);
        }
    };

    private static final List<Integer> SEARCH_OPTIONS_CLOUD = new ArrayList<Integer>()
    {
        {
            add(HistorySearch.TYPE_DOCUMENT);
            add(HistorySearch.TYPE_FOLDER);
        }
    };

    private static final Map<Integer, Integer> ITEMS = new HashMap<Integer, Integer>()
    {
        {
            put(HistorySearch.TYPE_DOCUMENT, R.string.search_documents);
            put(HistorySearch.TYPE_FOLDER, R.string.search_folders);
            put(HistorySearch.TYPE_PERSON, R.string.search_person);
            put(HistorySearch.TYPE_SITE, R.string.search_site);
        }
    };

    private static final Map<Integer, Integer> ITEMS_ICONS = new HashMap<Integer, Integer>()
    {
        {
            put(HistorySearch.TYPE_DOCUMENT, R.drawable.ic_doc_light);
            put(HistorySearch.TYPE_FOLDER, R.drawable.ic_repository_light);
            put(HistorySearch.TYPE_PERSON, R.drawable.ic_person_light);
            put(HistorySearch.TYPE_SITE, R.drawable.ic_site_light);
        }
    };

}
