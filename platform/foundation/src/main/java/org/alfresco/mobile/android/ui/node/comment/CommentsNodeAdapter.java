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

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.api.model.Comment;
import org.alfresco.mobile.android.api.model.impl.CommentImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.widget.TextView;

/**
 * Provides access to comments and displays them as a view based on
 * CommentViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class CommentsNodeAdapter extends BaseListAdapter<Comment, CommentViewHolder>
{
    protected RenditionManager renditionManager;

    protected WeakReference<Activity> activityRef;

    private TagHandlerList tagHandler;

    public CommentsNodeAdapter(Activity activity, AlfrescoSession session, int textViewResourceId,
            List<Comment> listItems)
    {
        super(activity, textViewResourceId, listItems);
        this.tagHandler = new TagHandlerList();
        this.vhClassName = CommentViewHolder.class.getCanonicalName();
        this.activityRef = new WeakReference<Activity>(activity);
    }

    @Override
    protected void updateTopText(CommentViewHolder vh, Comment item)
    {
        vh.topText.setText(createContentBottomText(getContext(), item));
        AccessibilityUtils.addContentDescription(
                vh.topText,
                String.format(getContext().getString(R.string.metadata_created_by),
                        createContentBottomText(getContext(), item)));
    }

    @Override
    protected void updateBottomText(CommentViewHolder vh, Comment item)
    {
        if (vh.content != null)
        {
            if (AccessibilityUtils.isEnabled(getContext()))
            {
                vh.content.setTextIsSelectable(false);
                AccessibilityUtils.addContentDescription(vh.content,
                        Html.fromHtml(item.getContent().trim(), null, tagHandler).toString());
            }
            vh.content
                    .setText(Html.fromHtml(item.getContent().trim(), null, tagHandler), TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    protected void updateIcon(CommentViewHolder vh, final Comment item)
    {
        RenditionManager.with(activityRef.get()).loadAvatar(item.getCreatedBy()).placeHolder(R.drawable.ic_avatar).into(vh.icon);
        AccessibilityUtils.addContentDescription(vh.icon, String.format(getContext().getString(R.string.contact_card),
                ((CommentImpl) item).getCreatedByPerson().getFullName()));

    }

    private String createContentBottomText(Context context, Comment item)
    {
        String s = ((CommentImpl) item).getCreatedByPerson().getFullName();

        if (item.getCreatedAt() != null)
        {
            s += " - " + formatDate(context, item.getCreatedAt().getTime());
        }
        return s;
    }
}
