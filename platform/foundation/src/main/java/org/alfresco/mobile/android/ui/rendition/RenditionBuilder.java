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
package org.alfresco.mobile.android.ui.rendition;

import java.lang.ref.WeakReference;

import org.alfresco.mobile.android.api.model.Node;

import androidx.fragment.app.FragmentActivity;
import android.widget.ImageView;

public class RenditionBuilder
{
    protected ImageView iv;

    protected String itemId;

    protected int placeHolderId;

    protected int typeId;

    protected int subTypeId;

    protected Boolean enableTouchImageView = null;

    protected int rendition = RenditionRequest.RENDITION_DEFAULT;

    protected WeakReference<FragmentActivity> activityRef;

    // RenditionManager.with(getActivity()).placeholder(iconId).type(Content).load(iv)

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public RenditionBuilder(FragmentActivity activity)
    {
        this.activityRef = new WeakReference<FragmentActivity>(activity);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public RenditionBuilder placeHolder(int drawableId)
    {
        this.placeHolderId = drawableId;
        return this;
    }

    public RenditionBuilder rendition(int renditionTypeId)
    {
        this.rendition = renditionTypeId;
        return this;
    }
    
    public RenditionBuilder loadAvatar(String personIdentifier)
    {
        this.itemId = personIdentifier;
        this.typeId = RenditionRequest.TYPE_AVATAR;
        return this;
    }
    
    public RenditionBuilder loadProcessDiagram(String processId)
    {
        this.itemId = processId;
        this.typeId = RenditionRequest.TYPE_PROCESS_DIAGRAM;
        return this;
    }

    public RenditionBuilder loadNode(String nodeIdentifier)
    {
        this.itemId = nodeIdentifier;
        this.typeId = RenditionRequest.TYPE_NODE;
        return this;
    }

    public RenditionBuilder loadNode(Node node)
    {
        this.itemId = node.getIdentifier();
        this.typeId = RenditionRequest.TYPE_NODE;
        if (node.isFolder())
        {
            subTypeId = RenditionRequest.SUBTYPE_FOLDER;
        }
        else
        {
            subTypeId = RenditionRequest.SUBTYPE_DOCUMENT;
        }
        return this;
    }

    public RenditionBuilder touchViewEnable(boolean touchViewEnable)
    {
        this.enableTouchImageView = touchViewEnable;
        return this;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EXECUTION
    // ///////////////////////////////////////////////////////////////////////////
    public void into(ImageView imageView)
    {
        RenditionRequest request = new RenditionRequest(imageView, itemId, rendition, placeHolderId, typeId, subTypeId, enableTouchImageView);
        RenditionManager.getInstance(activityRef.get()).display(request);
        activityRef.clear();
    }
}
