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
package org.alfresco.mobile.android.ui.node.comment;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.node.comment.CommentsEvent;
import org.alfresco.mobile.android.async.node.comment.CommentsRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;

import android.os.Bundle;

import com.squareup.otto.Subscribe;

public class CommentsNodeFragment extends BaseGridFragment
{
    public static final String TAG = CommentsNodeFragment.class.getName();

    protected static final String ARGUMENT_NODE = "commentedNode";

    protected Node node;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public CommentsNodeFragment()
    {
        emptyListMessageId = R.string.empty_actvity;
    }

    public static CommentsNodeFragment newInstance(Node node)
    {
        CommentsNodeFragment bf = new CommentsNodeFragment();
        Bundle settings = new Bundle();
        settings.putParcelable(ARGUMENT_NODE, node);
        settings.putInt(BaseListAdapter.DISPLAY_ICON, BaseListAdapter.DISPLAY_ICON_CREATOR);
        bf.setArguments(settings);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // REQUEST
    // //////////////////////////////////////////////////////////////////////
    protected void onRetrieveParameters(Bundle bundle)
    {
        node = bundle.getParcelable(ARGUMENT_NODE);
    }

    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new CommentsRequest.Builder(node).setListingContext(listingContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(CommentsEvent event)
    {
        displayData(event);
    }
}
