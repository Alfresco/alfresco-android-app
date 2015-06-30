/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * <p/>
 * This file is part of Alfresco Mobile for Android.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.managers;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.services.impl.AbstractDocumentFolderServiceImpl;
import org.alfresco.mobile.android.api.services.impl.AbstractPersonService;
import org.alfresco.mobile.android.api.services.impl.AbstractWorkflowService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.api.utils.OnPremiseUrlRegistry;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.platform.network.NetworkSingleton;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.rendition.RenditionRequest;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.picasso.Callback;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.Builder;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

/**
 * Utility class for downloading content and display it.
 *
 * @author jpascal
 */
public class RenditionManagerImpl extends RenditionManager
{
    private static final String TAG = RenditionManagerImpl.class.getSimpleName();

    protected WeakReference<Activity> activityRef;

    private AlfrescoSession session;

    private Picasso picasso;

    public static final int TYPE_NODE = 0;

    public static final int TYPE_PERSON = 1;

    public static final int TYPE_WORKFLOW = 2;

    private static final String AWAIT = "await";

    private static final String NO_RENDITION = "NoRendition";

    protected Map<String, String> streamUriStore = new HashMap<String, String>();

    protected Map<String, String> previewUriStore = new HashMap<String, String>();

    protected Map<String, Integer> urlRetrievers = new HashMap<String, Integer>();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static RenditionManagerImpl getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new RenditionManagerImpl(context);
                if (context instanceof Activity)
                {
                    ((RenditionManagerImpl) mInstance).setCurrentActivity((Activity) context);
                }
            }

            return (RenditionManagerImpl) mInstance;
        }
    }

    protected RenditionManagerImpl(Context applicationContext)
    {
        super(applicationContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public Picasso getPicasso()
    {
        if (picasso == null)
        {
            picasso = new Builder(appContext).build();
        }
        return picasso;
    }

    public void setCurrentActivity(Activity activity)
    {
        activityRef = new WeakReference<Activity>(activity);
    }

    /**
     * Display the content of the url inside an imageview. (thumbnails)
     */
    private void displayPlaceHolder(WeakReference<ImageView> ivRef, int placeHolderId)
    {
        if (ivRef != null && ivRef.isEnqueued())
        {
            ivRef.get().setImageResource(placeHolderId);
        }
    }

    private void displayPlaceHolder(ImageView iv, int placeHolderId)
    {
        iv.setImageResource(placeHolderId);
    }

    public void display(RenditionRequest request)
    {
        // Prerequisites
        if (session == null)
        {
            setSession(SessionUtils.getSession(appContext));
        }
        if (picasso == null)
        {
            picasso = new Builder(appContext).build();
        }

        // Cancel any previous request
        if (picasso != null)
        {
            picasso.cancelRequest(request.iv.get());
        }

        // Wrong identifier so display placeholder directly
        String url = null;
        if (TextUtils.isEmpty(request.itemId) || session == null)
        {
            request.iv.get().setImageResource(request.placeHolderId);
            return;
        }

        // Let's find how to display the itemId
        switch (request.typeId)
        {
            case TYPE_NODE:
                if (session instanceof RepositorySession)
                {
                    url = getDocumentRendition(request.itemId, DocumentFolderService.RENDITION_THUMBNAIL);
                    if (request.renditionTypeId == RenditionRequest.RENDITION_PREVIEW)
                    {
                        url = getDocumentRendition(request.itemId, DocumentFolderService.RENDITION_PREVIEW);
                    }
                    startPicasso(url, request);
                    return;
                }
                else if (hasReference(request.itemId, request.renditionTypeId))
                {
                    url = getReference(request.itemId, request.renditionTypeId);
                    startPicasso(url, request);
                    return;
                }
                break;
            case TYPE_WORKFLOW:
                url = ((AbstractWorkflowService) session.getServiceRegistry().getWorkflowService())
                        .getProcessDiagramUrl(request.itemId).toString();
                startPicasso(url, request);
                return;
            case TYPE_PERSON:
                if (session instanceof RepositorySession
                        && session.getRepositoryInfo().getMajorVersion() >= OnPremiseConstant.ALFRESCO_VERSION_4)
                {
                    url = OnPremiseUrlRegistry.getAvatarUrl(session, request.itemId);
                    startPicasso(url, request);
                    return;
                }
                else if (hasReference(request.itemId, request.renditionTypeId))
                {
                    url = getReference(request.itemId, request.renditionTypeId);
                    startPicasso(url, request);
                    return;
                }
                break;
            default:
                break;
        }

        // We don't know the url associated to the itemId, we need to find it
        // first.
        displayPlaceHolder(request.iv, request.placeHolderId);
        if (getReference(request.itemId, request.renditionTypeId) == null && activityRef != null)
        {
            addReference(request.itemId, AWAIT, request.renditionTypeId);
            urlRetrieverThread thread = new urlRetrieverThread(activityRef.get(), session, request);
            thread.setPriority(Thread.MIN_PRIORITY);
            if (request.renditionTypeId == RenditionRequest.RENDITION_PREVIEW)
            {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            final AsyncDrawable asyncDrawable = new AsyncDrawable(activityRef.get().getResources(), thread);
            request.iv.get().setImageDrawable(asyncDrawable);
            if (thread.getState() == Thread.State.NEW)
            {
                thread.start();
            }
        }
    }

    private void startPicasso(String url, final RenditionRequest request)
    {
        if (request.iv == null || request.iv.get() == null || url == null) { return; }
        picasso.cancelRequest(request.iv.get());
        try
        {
            picasso.load(url).placeholder(request.placeHolderId).into(request.iv.get(), new Callback()
            {
                @Override
                public void onSuccess()
                {
                    if (request.iv.get() instanceof ImageViewTouch)
                    {
                        if (request.touchViewEnabled != null)
                        {
                            ((ImageViewTouch) request.iv.get()).setScaleEnabled(request.touchViewEnabled);
                            ((ImageViewTouch) request.iv.get()).setDoubleTapEnabled(request.touchViewEnabled);
                        }
                        ((ImageViewTouch) request.iv.get()).setDisplayType(DisplayType.FIT_TO_SCREEN);
                        if (((ViewGroup) request.iv.get().getParent()).findViewById(R.id.preview_message) != null)
                        {
                            ((ViewGroup) request.iv.get().getParent()).findViewById(R.id.preview_message)
                                    .setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onError()
                {
                    if (request.iv.get() instanceof ImageViewTouch)
                    {
                        ((ImageViewTouch) request.iv.get()).setDisplayType(DisplayType.FIT_IF_BIGGER);
                        request.iv.get().setScaleType(ScaleType.FIT_CENTER);
                    }
                    displayPlaceHolder(request.iv.get(), request.placeHolderId);
                    displayErrorMessage(request.iv.get());
                }
            });
        }
        catch (IllegalArgumentException e)
        {
            // Do nothing
        }
        catch (Exception e)
        {
            // Do nothing
        }
    }

    private void displayErrorMessage(ImageView iv)
    {
        if (iv != null && ((ViewGroup) iv.getParent()).findViewById(R.id.preview_message) != null)
        {
            ((ViewGroup) iv.getParent()).findViewById(R.id.preview_message).setVisibility(View.VISIBLE);
        }
    }

    private class urlRetrieverThread extends Thread
    {
        private AlfrescoSession session;

        private Activity ctxt;

        private final RenditionRequest request;

        public urlRetrieverThread(Activity ctxt, AlfrescoSession session, RenditionRequest request)
        {
            this.session = session;
            this.ctxt = ctxt;
            this.request = request;
        }

        @Override
        public void run()
        {
            UrlBuilder url = null;
            try
            {
                switch (request.typeId)
                {
                    case TYPE_NODE:
                        String renditionId = DocumentFolderService.RENDITION_THUMBNAIL;
                        if (request.renditionTypeId == RenditionRequest.RENDITION_PREVIEW)
                        {
                            renditionId = DocumentFolderService.RENDITION_PREVIEW;
                        }
                        if (isInterrupted()) { return; }
                        url = ((AbstractDocumentFolderServiceImpl) session.getServiceRegistry()
                                .getDocumentFolderService()).getRenditionUrl(request.itemId, renditionId);
                        break;
                    case TYPE_WORKFLOW:
                        url = ((AbstractWorkflowService) session.getServiceRegistry().getWorkflowService())
                                .getProcessDiagramUrl(request.itemId);
                        break;
                    case TYPE_PERSON:
                        url = ((AbstractPersonService) session.getServiceRegistry().getPersonService())
                                .getAvatarUrl(request.itemId);
                        break;

                    default:
                        break;
                }

                if (url != null && request.iv.get() != null)
                {
                    if (isInterrupted()) { return; }
                    addReference(request.itemId, url.toString(), request.renditionTypeId);
                    ctxt.runOnUiThread(new urlDisplayer(url.toString(), request));
                }
                else if (url == null)
                {
                    addReference(request.itemId, NO_RENDITION, request.renditionTypeId);
                }
            }
            catch (Exception e)
            {
                addReference(request.itemId, NO_RENDITION, request.renditionTypeId);
                ctxt.runOnUiThread(new urlDisplayer(null, request));
            }
        }
    }

    private void addReference(String identifier, String url, Integer preview)
    {
        if (preview != null && preview == RenditionRequest.RENDITION_PREVIEW)
        {
            previewUriStore.put(identifier, url);
        }
        else
        {
            streamUriStore.put(identifier, url);
        }
    }

    private boolean hasReference(String identifier, Integer preview)
    {
        if (preview != null && preview == RenditionRequest.RENDITION_PREVIEW)
        {
            return previewUriStore.containsKey(identifier) && !previewUriStore.get(identifier).equals(AWAIT);
        }
        else
        {
            return streamUriStore.containsKey(identifier) && !streamUriStore.get(identifier).equals(AWAIT);
        }
    }

    private String getReference(String identifier, Integer preview)
    {
        if (preview != null && preview == RenditionRequest.RENDITION_PREVIEW)
        {
            return previewUriStore.get(identifier);
        }
        else
        {
            return streamUriStore.get(identifier);
        }
    }

    // Used to display bitmap in the UI thread
    private class urlDisplayer implements Runnable
    {
        private String url;

        private RenditionRequest request;

        public urlDisplayer(String url, RenditionRequest request)
        {
            this.url = url;
            this.request = request;
        }

        public void run()
        {
            if (url != null && request != null)
            {
                startPicasso(url, request);

            }
            else if (request.iv != null && request.iv.get() != null)
            {
                displayErrorMessage(request.iv.get());
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    public static class AsyncDrawable extends BitmapDrawable
    {
        private final WeakReference<urlRetrieverThread> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, urlRetrieverThread bitmapThread)
        {
            super(res);
            bitmapWorkerTaskReference = new WeakReference<urlRetrieverThread>(bitmapThread);
        }

        public urlRetrieverThread getBitmapWorkerTask()
        {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView)
    {
        final urlRetrieverThread bitmapWorkerTask = getBitmapThread(imageView);

        if (bitmapWorkerTask != null)
        {
            final String workerId = bitmapWorkerTask.request.itemId;
            if (workerId != null && !workerId.equals(data))
            {
                bitmapWorkerTask.interrupt();
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    private static urlRetrieverThread getBitmapThread(ImageView imageView)
    {
        if (imageView != null)
        {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable)
            {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public boolean hasSameSession(AlfrescoSession alfrescoSession)
    {
        return !(alfrescoSession == null || session == null) && alfrescoSession.equals(session);
    }

    public String getDocumentRendition(String identifier, String type)
    {
        try
        {
            String nodeIdentifier = identifier;
            if (NodeRefUtils.isVersionIdentifier(identifier) || NodeRefUtils.isIdentifier(identifier))
            {
                nodeIdentifier = NodeRefUtils.createNodeRefByIdentifier(identifier);
            }
            UrlBuilder url = new UrlBuilder(OnPremiseUrlRegistry.getThumbnailsUrl(session, nodeIdentifier, type));
            url.addParameter("format", "json");

            return url.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void setSession(AlfrescoSession alfrescoSession)
    {
        this.session = alfrescoSession;
        if (picasso != null)
        {
            picasso.shutdown();
        }

        OkHttpClient client = null;
        // Specific to detect if OKhttp is used
        try
        {
            Class.forName("org.alfresco.mobile.android.platform.network.MobileIronHttpInvoker");
            //OKhttp compatible with MobileIron ?
            client = new OkHttpClient();
        }
        catch (ClassNotFoundException e)
        {
            client = NetworkSingleton.getInstance().getHttpClient().clone();
        }
        ImageDownloader imageLoader = new ImageDownloader(client, alfrescoSession);
        Builder builder = new Builder(appContext);
        picasso = builder.downloader(imageLoader).build();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INNER CLASS
    // ///////////////////////////////////////////////////////////////////////////
    public class ImageDownloader extends OkHttpDownloader
    {

        public ImageDownloader(OkHttpClient client, AlfrescoSession alfSession)
        {
            super(client);
            Map<String, List<String>> httpHeaders = ((AbstractAlfrescoSessionImpl) alfSession)
                    .getAuthenticationProvider().getHTTPHeaders();
            Map<String, String> headers = new HashMap<>(httpHeaders.size());
            // set other headers
            if (httpHeaders != null)
            {
                for (Map.Entry<String, List<String>> header : httpHeaders.entrySet())
                {
                    if (header.getValue() != null)
                    {
                        for (String value : header.getValue())
                        {
                            headers.put(header.getKey(), value);
                        }
                    }
                }
                addHeaders(client, headers);
            }
        }

        private void addHeaders(OkHttpClient okHttpClient, final Map<String, String> headers)
        {
            okHttpClient.interceptors().add(new com.squareup.okhttp.Interceptor()
            {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException
                {
                    Request.Builder builder = chain.request().newBuilder();
                    for (Map.Entry<String, String> header : headers.entrySet())
                    {
                        builder.addHeader(header.getKey(), header.getValue()).build();
                    }
                    return chain.proceed(builder.build());
                }
            });
        }
    }
}
