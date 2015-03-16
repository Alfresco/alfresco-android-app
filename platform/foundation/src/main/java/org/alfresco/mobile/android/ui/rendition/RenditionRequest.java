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
package org.alfresco.mobile.android.ui.rendition;

import java.lang.ref.WeakReference;

import android.widget.ImageView;

public class RenditionRequest
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANT
    // ///////////////////////////////////////////////////////////////////////////
    public static final int TYPE_NODE = 0;

    public static final int SUBTYPE_DOCUMENT = 0;

    public static final int SUBTYPE_FOLDER = 0;

    public static final int TYPE_AVATAR = 1;

    public static final int TYPE_PROCESS_DIAGRAM = 2;
    
    public static final int RENDITION_DEFAULT = 1;

    public static final int RENDITION_PREVIEW = 2;
    
    // ///////////////////////////////////////////////////////////////////////////
    // MEMBERS
    // ///////////////////////////////////////////////////////////////////////////
    public final WeakReference<ImageView> iv;

    public final String itemId;

    public final int placeHolderId;

    public final int typeId;

    public final int subTypeId;
    
    public final int renditionTypeId;
    
    public final Boolean touchViewEnabled;
    
    public RenditionRequest(ImageView iv, String itemId, int renditionType, int placeHolderId, int typeId, int subTypeId, Boolean touchViewEnabled)
    {
        super();
        this.iv = new WeakReference<ImageView>(iv);
        this.itemId = itemId;
        this.placeHolderId = placeHolderId;
        this.typeId = typeId;
        this.subTypeId = subTypeId;
        this.renditionTypeId = renditionType;
        this.touchViewEnabled = touchViewEnabled;
    }
    
}
