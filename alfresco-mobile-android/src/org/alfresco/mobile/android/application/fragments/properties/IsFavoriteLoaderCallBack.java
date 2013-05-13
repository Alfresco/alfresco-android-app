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
package org.alfresco.mobile.android.application.fragments.properties;

import org.alfresco.mobile.android.api.asynchronous.FavoriteLoader;
import org.alfresco.mobile.android.api.asynchronous.IsFavoriteLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseLoaderCallback;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class IsFavoriteLoaderCallBack extends BaseLoaderCallback implements LoaderCallbacks<LoaderResult<Boolean>>
{
    private static final String TAG = "IsLikedLoaderCallBack";

    private Node node;

    private ImageView favoriteButton;

    private View progressView;

    public IsFavoriteLoaderCallBack(AlfrescoSession session, Activity context, Node node)
    {
        super();
        this.session = session;
        this.context = context;
        this.node = node;
    }

    @Override
    public Loader<LoaderResult<Boolean>> onCreateLoader(int id, Bundle args)
    {
        boolean isCreate = false;
        if (args != null)
        {
            isCreate = args.getBoolean(IS_CREATE);
        }

        if (!isCreate)
        {
            return new IsFavoriteLoader(context, session, node);
        }
        else
        {
            return new FavoriteLoader(context, session, node);
        }
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Boolean>> arg0, LoaderResult<Boolean> isFavorited)
    {
        if (progressView != null)
        {
            progressView.setVisibility(View.GONE);
        }
        if (isFavorited.getData() == null)
        {
            Log.e(TAG, Log.getStackTraceString(isFavorited.getException()));
            MessengerManager.showToast(context, R.string.error_retrieve_favorite);
        }
        else if (isFavorited.getData())
        {
            favoriteButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_dark));
        }
        else
        {
            favoriteButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_unfavorite_dark));
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Boolean>> arg0)
    {

    }

    public void setImageButton(ImageView mi)
    {
        this.favoriteButton = mi;
    }

    public void setProgressView(View v)
    {
        this.progressView = v;
    }

    private static final String IS_CREATE = "isCreate";

    public void execute(boolean isCreate)
    {
        int id = (isCreate) ? FavoriteLoader.ID : IsFavoriteLoader.ID;

        if (progressView != null)
        {
            progressView.setVisibility(View.VISIBLE);
        }

        Bundle b = new Bundle();
        b.putBoolean(IS_CREATE, isCreate);

        if (getLoaderManager().getLoader(id) == null)
        {
            getLoaderManager().initLoader(id, b, this);
        }
        getLoaderManager().restartLoader(id, b, this);
        getLoaderManager().getLoader(id).forceLoad();

    }
}
