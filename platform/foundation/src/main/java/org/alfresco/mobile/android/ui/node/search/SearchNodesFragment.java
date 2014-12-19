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
package org.alfresco.mobile.android.ui.node.search;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.KeywordSearchOptions;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.node.search.SearchEvent;
import org.alfresco.mobile.android.async.node.search.SearchRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.os.Bundle;

import com.squareup.otto.Subscribe;

/**
 * @since 1.3
 * @author Jean Marie Pascal
 */
public class SearchNodesFragment extends BaseGridFragment
{

    public static final String TAG = SearchNodesFragment.class.getName();

    protected static final int MAX_RESULT_ITEMS = 30;

    protected static final String ARGUMENT_KEYWORDS = "keywords";

    protected static final String ARGUMENT_FULLTEXT = "fulltext";

    protected static final String ARGUMENT_EXACTMATCH = "exact";

    protected static final String ARGUMENT_PARENTFOLDER = "folder";

    protected static final String ARGUMENT_SEARCH_FOLDER = "searchFolderOnly";

    protected static final String ARGUMENT_INCLUDE_DESCENDANTS = "descendants";

    protected static final String ARGUMENT_STATEMENT = "statement";

    protected static final String AGUMENT_LANGUAGE = "language";

    protected String keywords = null;

    protected String statement = null;

    protected String language = null;

    protected Folder f = null;

    protected boolean isExact = false;

    protected boolean fullText = true;

    protected boolean includeDescendants = true;

    protected boolean searchFolderOnly = false;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public SearchNodesFragment()
    {
        emptyListMessageId = R.string.empty_search;
    }

    public static Bundle createBundleArgs(String query)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_KEYWORDS, query);
        return args;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), getString(R.string.search));
        super.onResume();
    }

    protected void onRetrieveParameters(Bundle bundle)
    {
        keywords = getArguments().getString(ARGUMENT_KEYWORDS);
        statement = getArguments().getString(ARGUMENT_STATEMENT);

        f = (Folder) getArguments().getSerializable(ARGUMENT_PARENTFOLDER);

        language = (getArguments().containsKey(AGUMENT_LANGUAGE)) ? getArguments().getString(AGUMENT_LANGUAGE) : null;
        isExact = (getArguments().containsKey(ARGUMENT_EXACTMATCH)) ? getArguments().getBoolean(ARGUMENT_EXACTMATCH)
                : isExact;
        fullText = (getArguments().containsKey(ARGUMENT_FULLTEXT)) ? getArguments().getBoolean(ARGUMENT_FULLTEXT)
                : fullText;
        includeDescendants = (getArguments().containsKey(ARGUMENT_INCLUDE_DESCENDANTS)) ? getArguments().getBoolean(
                ARGUMENT_INCLUDE_DESCENDANTS) : includeDescendants;
        searchFolderOnly = (getArguments().containsKey(ARGUMENT_SEARCH_FOLDER)) ? getArguments().getBoolean(
                ARGUMENT_SEARCH_FOLDER) : searchFolderOnly;
    }

    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        if (statement != null)
        {
            return new SearchRequest.Builder(statement, SearchLanguage.fromValue(language));
        }
        else if (keywords != null) { return new SearchRequest.Builder(keywords, new KeywordSearchOptions(f,
                includeDescendants, fullText, isExact)); }
        return null;
    }

    @Subscribe
    public void onResult(SearchEvent event)
    {
        displayData(event);
    }
}
