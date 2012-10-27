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
package org.alfresco.mobile.android.application.fragments.comments;

import org.alfresco.mobile.android.api.asynchronous.CommentCreateLoader;
import org.alfresco.mobile.android.api.model.Comment;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.CommentService;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.comment.CommentFragment;
import org.alfresco.mobile.android.ui.comment.actions.CommentCreateLoaderCallback;
import org.alfresco.mobile.android.ui.comment.listener.OnCommentCreateListener;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class CommentsFragment extends CommentFragment
{

    public static final String TAG = "CommentsFragment";

    private EditText commentText;
    private Button bAdd;

    public CommentsFragment()
    {
    }

    public static CommentsFragment newInstance(Node n)
    {
        CommentsFragment bf = new CommentsFragment();
        ListingContext lc = new ListingContext();
        lc.setMaxItems(15);
        lc.setSortProperty(CommentService.SORT_PROPERTY_CREATED_AT);
        lc.setIsSortAscending(true);
        Bundle b = createBundleArgs(lc, LOAD_MANUAL);
        b.putAll(createBundleArgs(n));
        bf.setArguments(b);
        return bf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.sdk_comments, container, false);

        init(v, R.string.empty_comment);

        commentText = (EditText) v.findViewById(R.id.comment_value);
        bAdd = (Button) v.findViewById(R.id.send_comment);

        bAdd.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (commentText.getText().length() > 0)
                {
                    CommentCreateLoaderCallback c = new CommentCreateLoaderCallback(alfSession, getActivity(), node,
                            commentText.getText().toString());
                    c.setOnCommentCreateListener(createListener);
                    getLoaderManager().restartLoader(CommentCreateLoader.ID, null, c);
                }
                else
                {
                    MessengerManager.showToast(getActivity(), R.string.empty_comment);
                }
            }
        });

        return v;
    }
    
    private OnCommentCreateListener createListener = new OnCommentCreateListener()
    {
        @Override
        public void beforeCommentCreation(String content)
        {
            commentText.setEnabled(false);
            bAdd.setEnabled(false);
        }

        @Override
        public void afterCommentCreation(Comment c)
        {
            commentText.setEnabled(true);
            commentText.setText("");
            bAdd.setEnabled(true);
            reload(bundle, loaderId, callback);
            getLoaderManager().destroyLoader(CommentCreateLoader.ID);
        }

        @Override
        public void onExeceptionDuringCreation(Exception e)
        {
            MessengerManager.showLongToast(getActivity(), e.getMessage());
            bAdd.setEnabled(true);
            reload(bundle, loaderId, callback);
            getLoaderManager().destroyLoader(CommentCreateLoader.ID);
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart()
    {
        //FragmentUtils.setTitleFragmentPlace(getActivity(), "Comments : " + node.getName());
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }
}
