/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.comments;

import java.util.List;

import org.alfresco.mobile.android.api.model.Comment;
import org.alfresco.mobile.android.api.model.impl.CommentImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.person.PersonProfileFragment;
import org.alfresco.mobile.android.application.utils.TagHandlerList;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.manager.RenditionManager;
import org.alfresco.mobile.android.ui.utils.ViewHolder;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Provides access to comments and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class CommentAdapter extends BaseListAdapter<Comment, GenericViewHolder>
{
    private RenditionManager renditionManager;

    private Activity context;

    private TagHandlerList tagHandler;

    public CommentAdapter(Activity context, AlfrescoSession session, int textViewResourceId, List<Comment> listItems)
    {
        super(context, textViewResourceId, listItems);
        this.renditionManager = new RenditionManager(context, session);
        this.tagHandler = new TagHandlerList();
        this.vhClassName = GenericViewHolder.class.getCanonicalName();
        this.context = context;
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Comment item)
    {
        vh.topText.setText(createContentBottomText(getContext(), item));
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Comment item)
    {
        if (vh.content != null)
        {
            vh.content
                    .setText(Html.fromHtml(item.getContent().trim(), null, tagHandler), TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, final Comment item)
    {
        ((View) vh.icon.getParent()).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PersonProfileFragment.newInstance(item.getCreatedBy()).show(context.getFragmentManager(),
                        PersonProfileFragment.TAG);
            }
        });

        renditionManager.display(vh.icon, item.getCreatedBy(), R.drawable.ic_person);
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

final class GenericViewHolder extends ViewHolder
{
    public TextView topText;

    public TextView bottomText;

    public ImageView icon;

    public TextView content;

    public GenericViewHolder(View v)
    {
        super(v);
        icon = (ImageView) v.findViewById(R.id.icon);
        topText = (TextView) v.findViewById(R.id.toptext);
        bottomText = (TextView) v.findViewById(R.id.bottomtext);
        content = (TextView) v.findViewById(R.id.contentweb);
    }
}
