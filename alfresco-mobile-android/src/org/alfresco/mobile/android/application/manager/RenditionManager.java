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
package org.alfresco.mobile.android.application.manager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.services.impl.AbstractDocumentFolderServiceImpl;
import org.alfresco.mobile.android.api.services.impl.AbstractPersonService;
import org.alfresco.mobile.android.api.services.impl.AbstractWorkflowService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.api.utils.OnPremiseUrlRegistry;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.thirdparty.imagezoom.ImageViewTouch;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.Builder;

/**
 * Utility class for downloading content and display it.
 * 
 * @author jpascal
 */
public class RenditionManager
{
    private static final String TAG = "RenditionManager";

    private Activity context;

    private AlfrescoSession session;

    private ImageDownloader imageLoader;

    private Picasso picasso;

    public static final int TYPE_NODE = 0;

    public static final int TYPE_PERSON = 1;

    public static final int TYPE_WORKFLOW = 2;

    protected Map<String, String> streamUriStore = new HashMap<String, String>();

    protected Map<String, String> previewUriStore = new HashMap<String, String>();

    public RenditionManager(Activity context, AlfrescoSession session)
    {
        this.context = context;
        this.session = session;

        if (picasso == null)
        {
            imageLoader = new ImageDownloader(context, session);
            Builder builder = new Picasso.Builder(context);
            picasso = builder.downloader(imageLoader).build();
        }
    }

    /**
     * Display the content of the url inside an imageview. (thumbnails)
     * 
     * @param iv
     * @param url
     * @param initDrawableId
     */
    public void display(ImageView iv, Node n, int initDrawableId)
    {
        display(iv, n.getIdentifier(), initDrawableId, TYPE_NODE, null);
    }

    public void display(ImageView iv, Folder n, int initDrawableId)
    {
        if (picasso != null)
        {
            picasso.cancelRequest(iv);
        }
        display(iv, n.getIdentifier(), initDrawableId, TYPE_NODE, null);
    }

    public void display(ImageView iv, Node n, int initDrawableId, ScaleType scaleType)
    {
        display(iv, n.getIdentifier(), initDrawableId, TYPE_NODE, null, scaleType);
    }

    public void display(ImageView iv, int initDrawableId, String identifier)
    {
        display(iv, identifier, initDrawableId, TYPE_NODE, null);
    }

    public void display(ImageView iv, String username, int initDrawableId)
    {
        display(iv, username, initDrawableId, TYPE_PERSON, null);
    }

    public void displayDiagram(ImageView iv, int initDrawableId, String workflowId)
    {
        display(iv, workflowId, initDrawableId, TYPE_WORKFLOW, null, ScaleType.FIT_CENTER);
    }

    public void preview(ImageView iv, Node n, int initDrawableId, Integer size)
    {
        display(iv, n.getIdentifier(), initDrawableId, TYPE_NODE, size);
    }

    public void preview(ImageView iv, int initDrawableId, String identifier)
    {
        display(iv, identifier, initDrawableId, TYPE_NODE, null);
    }

    private void display(ImageView iv, String identifier, int initDrawableId, int type, Integer preview)
    {
        display(iv, identifier, initDrawableId, type, preview, ScaleType.FIT_CENTER);
    }

    private void display(final ImageView iv, String identifier, int initDrawableId, int type, Integer preview,
            ScaleType scaleType)
    {
        // Wrong identifier so display placeholder
        String url = null;
        if (identifier == null || identifier.isEmpty())
        {
            iv.setImageResource(initDrawableId);
            return;
        }
        Log.d(TAG, identifier);

        //
        switch (type)
        {
            case TYPE_NODE:
                if (session instanceof RepositorySession)
                {
                    url = getDocumentRendition(identifier, DocumentFolderService.RENDITION_THUMBNAIL);
                    if (preview != null)
                    {
                        url = getDocumentRendition(identifier, DocumentFolderService.RENDITION_PREVIEW);
                    }
                    startPicasso(url, initDrawableId, iv);
                    return;
                }
                else if (hasReference(identifier, preview))
                {
                    url = getReference(identifier, preview);
                    startPicasso(url, initDrawableId, iv);
                    return;
                }
                break;
            case TYPE_WORKFLOW:
                url = ((AbstractWorkflowService) session.getServiceRegistry().getWorkflowService())
                        .getProcessDiagramUrl(identifier).toString();
                startPicasso(url, initDrawableId, iv);
                return;
            case TYPE_PERSON:
                if (session instanceof RepositorySession
                        && session.getRepositoryInfo().getMajorVersion() >= OnPremiseConstant.ALFRESCO_VERSION_4)
                {
                    url = OnPremiseUrlRegistry.getAvatarUrl(session, identifier);
                    startPicasso(url, initDrawableId, iv);
                    return;
                }
                break;
            default:
                break;
        }

        addReference(identifier, null, preview);
        iv.setImageResource(initDrawableId);
        urlRetrieverThread thread = new urlRetrieverThread(context, session, iv, initDrawableId, identifier, type,
                preview);
        thread.setPriority(Thread.MIN_PRIORITY);
        if (preview != null)
        {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), thread);
        iv.setImageDrawable(asyncDrawable);
        if (thread.getState() == Thread.State.NEW)
        {
            thread.start();
        }
    }

    private void startPicasso(String url, int initDrawableId, final ImageView iv)
    {
        if (url == null)
        {
            iv.setImageResource(initDrawableId);
            displayErrorMessage(iv);
            return;
        }
        picasso.cancelRequest(iv);
        picasso.load(url).placeholder(initDrawableId).into(iv, new Callback()
        {
            @Override
            public void onSuccess()
            {
                if (iv instanceof ImageViewTouch)
                {
                    ((ImageViewTouch) iv).setScaleEnabled(true);
                    ((ImageViewTouch) iv).setDoubleTapEnabled(true);
                    if (iv != null && ((ViewGroup) iv.getParent()).findViewById(R.id.preview_message) != null)
                    {
                        ((ViewGroup) iv.getParent()).findViewById(R.id.preview_message).setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError()
            {
                displayErrorMessage(iv);
            }
        });
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
        private final WeakReference<ImageView> imageViewReference;

        private String identifier;

        private AlfrescoSession session;

        private Activity ctxt;

        private int type;

        private Integer preview;

        private int initDrawableId;

        public urlRetrieverThread(Activity ctxt, AlfrescoSession session, ImageView imageView, int initDrawableId,
                String identifier, int type, Integer preview)
        {
            this.imageViewReference = new WeakReference<ImageView>(imageView);
            this.session = session;
            this.ctxt = ctxt;
            this.type = type;
            this.identifier = identifier;
            this.preview = preview;
            this.initDrawableId = initDrawableId;
        }

        @Override
        public void run()
        {
            UrlBuilder url = null;
            try
            {
                switch (type)
                {
                    case TYPE_NODE:
                        String renditionId = DocumentFolderService.RENDITION_THUMBNAIL;
                        if (preview != null)
                        {
                            renditionId = DocumentFolderService.RENDITION_PREVIEW;
                        }
                        if (isInterrupted()) { return; }
                        url = ((AbstractDocumentFolderServiceImpl) session.getServiceRegistry()
                                .getDocumentFolderService()).getRenditionUrl(identifier, renditionId);
                        break;
                    case TYPE_WORKFLOW:
                        url = ((AbstractWorkflowService) session.getServiceRegistry().getWorkflowService())
                                .getProcessDiagramUrl(identifier);
                        break;
                    case TYPE_PERSON:
                        url = ((AbstractPersonService) session.getServiceRegistry().getPersonService())
                                .getAvatarUrl(identifier);
                        break;

                    default:
                        break;
                }

                if (url != null && imageViewReference.get() != null)
                {
                    if (isInterrupted()) { return; }
                    addReference(identifier, url.toString(), preview);
                    ctxt.runOnUiThread(new urlDisplayer(url.toString(), imageViewReference, initDrawableId));
                }
                else if (url == null)
                {
                    addReference(identifier, null, preview);
                }
            }
            catch (Exception e)
            {
                Log.w(TAG, Log.getStackTraceString(e));
                addReference(identifier, null, preview);
                ctxt.runOnUiThread(new urlDisplayer(null, imageViewReference, initDrawableId));
            }
        }
    }

    private void addReference(String identifier, String url, Integer preview)
    {
        if (preview != null)
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
        if (preview != null)
        {
            return previewUriStore.containsKey(identifier);
        }
        else
        {
            return streamUriStore.containsKey(identifier);
        }
    }

    private String getReference(String identifier, Integer preview)
    {
        if (preview != null)
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

        private ImageView imageView;

        private int initDrawableId;

        public urlDisplayer(String url, WeakReference<ImageView> imageViewReference, int initDrawableId)
        {
            this.url = url;
            this.imageView = imageViewReference.get();
            this.initDrawableId = initDrawableId;
        }

        public void run()
        {
            if (url != null)
            {
                startPicasso(url, initDrawableId, imageView);
            }
            else
            {
                imageView.setImageResource(initDrawableId);
                displayErrorMessage(imageView);
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
            Log.d(TAG, "cancel");
            final String workerId = bitmapWorkerTask.identifier;
            if (workerId != null && !workerId.equals(data))
            {
                Log.d(TAG, "interrupt");
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
                Log.d(TAG, "drawable");
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public boolean hasSameSession(AlfrescoSession alfrescoSession)
    {
        if (alfrescoSession == null || session == null) { return false; }
        return alfrescoSession.equals(session);
    }

    public String getDocumentRendition(String identifier, String type)
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
}
