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
package org.alfresco.mobile.android.application.fragments.node.rendition;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.NodeImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.node.details.TabsNodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.node.download.DownloadDialogFragment;
import org.alfresco.mobile.android.application.fragments.utils.OpenAsDialogFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils.ActionManagerListener;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.rendition.RenditionBuilder;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.rendition.RenditionRequest;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

/**
 * @since 1.1
 * @author Jean Marie Pascal
 */
public class PDFPreviewFragment extends AlfrescoFragment
{

    public static final String TAG = PDFPreviewFragment.class.getSimpleName();

    public static final String ARGUMENT_PDF = "pdfFile";

    private File pdfFile;

    protected boolean isRestrictable = false;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public PDFPreviewFragment()
    {
        requiredSession = true;
        checkSession = true;
    }

    protected static PDFPreviewFragment newInstanceByTemplate(Bundle b)
    {
        PDFPreviewFragment cbf = new PDFPreviewFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);

        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }
        setSession(SessionUtils.getSession(getActivity()));
        SessionUtils.checkSession(getActivity(), getSession());

        setRootView(inflater.inflate(R.layout.app_fragment_pdfjs, container, false));
        if (getSession() == null) { return getRootView(); }

        pdfFile = (File) getArguments().get(ARGUMENT_PDF);
        if (pdfFile == null) { return null; }

        WebView wv = (WebView) viewById(R.id.webView1);

        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setSupportZoom(true);

        wv.setWebViewClient(new WebViewClient()
        {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                Log.d("Debug", "Error");
            }
        });

        wv.setWebChromeClient(new WebChromeClient());

        wv.loadUrl("file:///android_asset/pdfjs-1.0.277-dist/web/viewer.html?file=" + pdfFile.getPath());

        return getRootView();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };

        public Builder pdf(File file)
        {
            extraConfiguration.putSerializable(ARGUMENT_PDF, file);
            return this;
        }
    }
}
