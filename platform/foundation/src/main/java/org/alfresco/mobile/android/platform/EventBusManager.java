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
package org.alfresco.mobile.android.platform;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import java.util.ArrayList;

public class EventBusManager extends Bus
{
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    protected static final Object LOCK = new Object();

    protected static EventBusManager mInstance;

    private ArrayList<Object> registeredObjects = new ArrayList<>();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static EventBusManager getInstance()
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new EventBusManager();
            }

            return mInstance;
        }
    }

    public EventBusManager()
    {

    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void unregister(Object object)
    {
        if (registeredObjects.contains(object)) {
            registeredObjects.remove(object);
            try {
                super.unregister(object);
            } catch (Exception e) { }
        }
    }

    @Override
    public void register(Object object) {
        if (!registeredObjects.contains(object)) {
            registeredObjects.add(object);
            super.register(object);
        }
    }

    @Override
    public void post(final Object event)
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            super.post(event);
        }
        else
        {
            mainThread.post(new Runnable()
            {
                @Override
                public void run()
                {
                    post(event);
                }
            });
        }
    }
}
