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
package org.alfresco.mobile.android.application.fragments.node.comment;

import java.util.List;

import org.alfresco.mobile.android.api.model.Comment;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.fragments.user.UserProfileFragment;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.ui.holder.TwoLinesCaptionViewHolder;
import org.alfresco.mobile.android.ui.node.comment.CommentsNodeAdapter;

import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Provides access to comments and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class CommentAdapter extends CommentsNodeAdapter
{
    public CommentAdapter(FragmentActivity context, AlfrescoSession session, int textViewResourceId,
            List<Comment> listItems)
    {
        super(context, session, textViewResourceId, listItems);
        this.renditionManager = RenditionManagerImpl.getInstance(getContext());
    }

    @Override
    protected void updateIcon(TwoLinesCaptionViewHolder vh, final Comment item)
    {
        super.updateIcon(vh, item);
        ((View) vh.icon).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserProfileFragment.with(activityRef.get()).personId(item.getCreatedBy()).displayAsDialog();
            }
        });
    }
}
