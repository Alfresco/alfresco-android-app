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
package org.alfresco.mobile.android.ui.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * List of static utils methods that can be used inside adapter.
 * 
 * @author Jean Marie Pascal
 */
public final class AdapterUtils
{
    private AdapterUtils()
    {
    }

    /**
     * Create or recycle View inside a listview.
     * 
     * @param c : Android context.
     * @param v : list view item.
     * @param layoutId : Unique identifier for the ressource layout.
     * @return a new or recycled item.
     */
    public static View recycleOrCreateView(Context c, View v, int layoutId)
    {
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(layoutId, null);
            GenericViewHolder vh = new GenericViewHolder(v);
            v.setTag(vh);
        }
        return v;
    }
}
