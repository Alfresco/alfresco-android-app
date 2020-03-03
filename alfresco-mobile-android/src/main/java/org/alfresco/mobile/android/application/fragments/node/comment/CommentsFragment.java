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

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Comment;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.CommentService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurableActionHelper;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.comment.CommentsEvent;
import org.alfresco.mobile.android.async.node.comment.CreateCommentEvent;
import org.alfresco.mobile.android.async.node.comment.CreateCommentRequest;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.ui.node.comment.CommentsNodeFragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.squareup.otto.Subscribe;

public class CommentsFragment extends CommentsNodeFragment
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
        emptyListMessageId = R.string.empty_comment;
        loadState = LOAD_VISIBLE;
        reportAtCreation = false;
    }

    protected static CommentsFragment newInstanceByTemplate(Bundle b)
    {
        CommentsFragment cbf = new CommentsFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);
        setRootView(inflater.inflate(R.layout.app_comments, container, false));

        init(getRootView(), R.string.empty_comment);

        commentText = (EditText) viewById(R.id.comment_value);
        bAdd = (ImageButton) viewById(R.id.send_comment);
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
                        && ((actionId == EditorInfo.IME_ACTION_SEND)))
                {
                    addcomment();
                    return true;
                }
                return false;
            }
        });

        gv.setSelector(android.R.color.transparent);
        gv.setCacheColorHint(getResources().getColor(android.R.color.transparent));

        return getRootView();
    }

    @Override
    public void onResume()
    {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        try
        {
            if (!ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), node,
                    ConfigurableActionHelper.ACTION_NODE_COMMENT))
            {
                ((View) commentText.getParent().getParent()).setVisibility(View.GONE);
            }
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }

        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RESULTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected BaseAdapter onAdapterCreation()
    {
        return adapter = new CommentAdapter(getActivity(), getSession(), R.layout.row_comment,
                new ArrayList<Comment>(0));
    }

    @Subscribe
    public void onResult(CommentsEvent event)
    {
        super.onResult(event);
        gv.setColumnWidth(DisplayUtils.getDPI(getResources().getDisplayMetrics(), 1000));
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
            String commentValue = commentText.getText().toString().trim();
            commentValue = commentValue.replaceAll("\n\n", "\n<p>&nbsp;</p>");
            Operator.with(getActivity()).load(new CreateCommentRequest.Builder(node, commentValue));
            onPrepareRefresh();
            commentText.setEnabled(false);
            bAdd.setEnabled(false);
        }
        else
        {
            AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.empty_comment);
        }
    }

    @Subscribe
    public void onCreateComment(CreateCommentEvent result)
    {
        commentText.setEnabled(true);
        commentText.setText("");
        bAdd.setEnabled(false);
        refresh();
    }

    @Override
    protected void prepareEmptyInitialView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        emptyImageView.setImageResource(R.drawable.alfresco_background_logo);
        firstEmptyMessage.setVisibility(View.GONE);
        secondEmptyMessage.setVisibility(View.GONE);
    }

    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        emptyImageView.setImageResource(R.drawable.alfresco_background_logo);
        firstEmptyMessage.setVisibility(View.VISIBLE);
        firstEmptyMessage.setText(R.string.empty_comment);
        secondEmptyMessage.setVisibility(View.GONE);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
            ListingContext lc = new ListingContext();
            lc.setMaxItems(MAX_COMMENT);
            lc.setSortProperty(CommentService.SORT_PROPERTY_CREATED_AT);
            lc.setIsSortAscending(true);
            setListingContext(lc);
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

        public Builder node(Node node)
        {
            extraConfiguration.putSerializable(ARGUMENT_NODE, node);
            return this;
        }
    }
}
