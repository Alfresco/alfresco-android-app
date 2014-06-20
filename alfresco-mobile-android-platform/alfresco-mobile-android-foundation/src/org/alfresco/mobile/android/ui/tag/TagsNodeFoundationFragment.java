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
package org.alfresco.mobile.android.ui.tag;

import java.util.ArrayList;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Tag;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.tag.TagsEvent;
import org.alfresco.mobile.android.async.tag.TagsOperationRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.squareup.otto.Subscribe;

public class TagsNodeFoundationFragment extends BaseGridFragment
{
    public static final String TAG = TagsNodeFoundationFragment.class.getName();

    protected Node node;

    public static final String ARGUMENT_NODE = "commentedNode";

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public TagsNodeFoundationFragment()
    {
        emptyListMessageId = R.string.empty_tag;
    }

    public static Bundle createBundleArgs(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    public static TagsNodeFoundationFragment newInstance(Node node)
    {
        TagsNodeFoundationFragment bf = new TagsNodeFoundationFragment();
        ListingContext lc = new ListingContext();
        lc.setMaxItems(50);
        Bundle b = createBundleArgs(lc, LOAD_VISIBLE);
        b.putSerializable(ARGUMENT_NODE, node);
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected void onRetrieveParameters(Bundle bundle)
    {
        node = (Node) bundle.getSerializable(ARGUMENT_NODE);
    }

    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new TagsOperationRequest.Builder(node).setListingContext(listingContext);
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new TagsFoundationAdapter(getActivity(), R.layout.sdk_grid_row, new ArrayList<Tag>(0));
    }

    @Subscribe
    public void onResult(TagsEvent event)
    {
        displayData(event);
    }
}
