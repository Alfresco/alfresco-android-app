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
package org.alfresco.mobile.android.ui.node.comment;

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.api.model.Comment;
import org.alfresco.mobile.android.api.model.impl.CommentImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesCaptionViewHolder;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.widget.TextView;

/**
 * Provides access to comments and displays them as a view based on
 * CommentViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class CommentsNodeAdapter extends BaseListAdapter<Comment, TwoLinesCaptionViewHolder>
{
    protected RenditionManager renditionManager;

    protected WeakReference<FragmentActivity> activityRef;

    private TagHandlerList tagHandler;

    public CommentsNodeAdapter(FragmentActivity activity, AlfrescoSession session, int textViewResourceId,
            List<Comment> listItems)
    {
        super(activity, textViewResourceId, listItems);
        this.tagHandler = new TagHandlerList();
        this.vhClassName = TwoLinesCaptionViewHolder.class.getCanonicalName();
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    protected void updateTopText(TwoLinesCaptionViewHolder vh, Comment item)
    {
        vh.topText.setText(((CommentImpl) item).getCreatedByPerson().getFullName());
        if (item.getCreatedAt() != null)
        {
            vh.topTextRight.setText(formatDate(getContext(), item.getCreatedAt().getTime()));
        }
        AccessibilityUtils.addContentDescription(
                vh.topText,
                String.format(getContext().getString(R.string.metadata_created_by),
                        createContentBottomText(getContext(), item)));
    }

    @Override
    protected void updateBottomText(TwoLinesCaptionViewHolder vh, Comment item)
    {
        if (vh.bottomText != null)
        {
            String value = (item.getContent() == null || item.getContent().isEmpty()) ? "" : item.getContent().trim();
            if (AccessibilityUtils.isEnabled(getContext()))
            {
                vh.bottomText.setTextIsSelectable(false);
                AccessibilityUtils.addContentDescription(vh.bottomText, Html.fromHtml(value, null, tagHandler)
                        .toString());
            }
            vh.bottomText.setText(Html.fromHtml(value, null, tagHandler), TextView.BufferType.SPANNABLE);
            HolderUtils.makeMultiLine(vh.bottomText, 25);
            vh.bottomText.setTextIsSelectable(true);
        }
    }

    @Override
    protected void updateIcon(TwoLinesCaptionViewHolder vh, final Comment item)
    {
        RenditionManager.with(activityRef.get()).loadAvatar(item.getCreatedBy()).placeHolder(R.drawable.ic_person_light)
                .into(vh.icon);
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
