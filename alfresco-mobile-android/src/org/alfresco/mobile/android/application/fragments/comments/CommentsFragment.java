/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.comments;

import java.util.ArrayList;

import org.alfresco.mobile.android.api.asynchronous.CommentCreateLoader;
import org.alfresco.mobile.android.api.asynchronous.CommentsLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Comment;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.services.CommentService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.comment.CommentFragment;
import org.alfresco.mobile.android.ui.comment.actions.CommentCreateLoaderCallback;
import org.alfresco.mobile.android.ui.comment.listener.OnCommentCreateListener;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.Loader;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class CommentsFragment extends CommentFragment
{
    public static final String TAG = CommentsFragment.class.getName();

    private static final int MAX_COMMENT = 15;

    private EditText commentText;

    private ImageButton bAdd;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public CommentsFragment()
    {
        loaderId = CommentsLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_comment;
    }

    public static Bundle createBundleArgs(Node node)
    {
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_NODE, node);
        return args;
    }

    public static CommentsFragment newInstance(Node n)
    {
        CommentsFragment bf = new CommentsFragment();
        ListingContext lc = new ListingContext();
        lc.setMaxItems(MAX_COMMENT);
        lc.setSortProperty(CommentService.SORT_PROPERTY_CREATED_AT);
        lc.setIsSortAscending(true);
        Bundle b = createBundleArgs(lc, LOAD_MANUAL);
        b.putAll(createBundleArgs(n));
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);

        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume()
    {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.document_comments_header));
        }

        if (!alfSession.getServiceRegistry().getDocumentFolderService().getPermissions(node).canEdit())
        {
            commentText.setVisibility(View.GONE);
            bAdd.setVisibility(View.GONE);
        }

        super.onResume();
    }

    @Override
    public void onPause()
    {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);
        View v = inflater.inflate(R.layout.app_comments, container, false);

        init(v, R.string.empty_comment);

        commentText = (EditText) v.findViewById(R.id.comment_value);
        bAdd = (ImageButton) v.findViewById(R.id.send_comment);
        bAdd.setEnabled(false);

        bAdd.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addcomment();
            }
        });

        commentText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                activateSend();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        commentText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        commentText.setOnEditorActionListener(new OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (event != null && (event.getAction() == KeyEvent.ACTION_DOWN)
                        && ((actionId == EditorInfo.IME_ACTION_SEND) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))
                {
                    addcomment();
                    return true;
                }
                return false;
            }
        });

        lv.setDivider(null);
        lv.setSelector(android.R.color.transparent);
        lv.setCacheColorHint(android.R.color.transparent);
        
        
        
        return v;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult<Comment>>> onCreateLoader(int id, Bundle ba)
    {
        if (!hasmore)
        {
            setListShown(false);
        }

        // Case Init & case Reload
        bundle = (ba == null) ? getArguments() : ba;

        ListingContext lc = null, lcorigin = null;

        if (bundle != null)
        {
            node = bundle.getParcelable(ARGUMENT_NODE);
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
        }
        calculateSkipCount(lc);
        CommentsLoader loader = new CommentsLoader(getActivity(), alfSession, node);
        loader.setListingContext(lc);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Comment>>> arg0,
            LoaderResult<PagingResult<Comment>> results)
    {
        if (adapter == null)
        {
            adapter = new CommentAdapter(getActivity(), alfSession, R.layout.sdk_list_comment_row,
                    new ArrayList<Comment>(0));
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult<Comment>>> arg0)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void activateSend()
    {
        if (commentText.getText().length() > 0)
        {
            bAdd.setEnabled(true);
        }
        else
        {
            bAdd.setEnabled(false);
        }
    }

    private void addcomment()
    {
        if (commentText.getText().length() > 0)
        {
            CommentCreateLoaderCallback c = new CommentCreateLoaderCallback(alfSession, getActivity(), node,
                    commentText.getText().toString());
            c.setOnCommentCreateListener(createListener);
            getLoaderManager().restartLoader(CommentCreateLoader.ID, null, c);
            getLoaderManager().getLoader(CommentCreateLoader.ID).forceLoad();
        }
        else
        {
            MessengerManager.showToast(getActivity(), R.string.empty_comment);
        }
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
            Log.e(TAG, Log.getStackTraceString(e));
            MessengerManager.showLongToast(getActivity(), getActivity().getString(R.string.error_general));
            bAdd.setEnabled(true);
            reload(bundle, loaderId, callback);
            getLoaderManager().destroyLoader(CommentCreateLoader.ID);
            activateSend();
            commentText.setEnabled(true);
        }
    };
}
