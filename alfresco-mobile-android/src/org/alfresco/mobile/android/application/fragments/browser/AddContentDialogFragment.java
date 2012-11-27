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
package org.alfresco.mobile.android.application.fragments.browser;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.application.utils.ProgressNotification;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.documentfolder.actions.CreateDocumentDialogFragment;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeCreateListener;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddContentDialogFragment extends CreateDocumentDialogFragment
{
    public AddContentDialogFragment()
    {
    }

    public static AddContentDialogFragment newInstance(Folder folder, File f)
    {
        AddContentDialogFragment adf = new AddContentDialogFragment();
        adf.setArguments(createBundle(folder, new ContentFileProgressImpl(f)));
        return adf;
    }

    public static AddContentDialogFragment newInstance(Folder folder)
    {
        AddContentDialogFragment adf = new AddContentDialogFragment();
        adf.setArguments(createBundle(folder));
        return adf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
        setOnCreateListener(nodeCreateListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    OnNodeCreateListener nodeCreateListener = new OnNodeCreateListener()
    {

        @Override
        public void beforeContentCreation(Folder parentFolder, String name, Map<String, Serializable> props,
                ContentFile contentFile)
        {
            if (contentFile != null)
            {
                Bundle progressBundle = new Bundle();
                ContentFile f = (ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE);

                if (f.getClass() == ContentFileProgressImpl.class)
                {
                    ((ContentFileProgressImpl) f).setFilename(name);
                    progressBundle.putString("name", name);
                }
                else
                {
                    progressBundle.putString("name", f.getFile().getName());
                }

                progressBundle.putInt("dataSize", (int) f.getFile().length());
                progressBundle.putInt("dataIncrement", (int) (f.getFile().length() / 10));

                ProgressNotification
                        .createProgressNotification(getActivity(), progressBundle, getActivity().getClass());
            }

        }

        @Override
        public void afterContentCreation(Node arg0)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onExeceptionDuringCreation(Exception e)
        {
            CloudExceptionUtils.handleCloudException(getActivity(), e, false);
        }
    };

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
    }
}
