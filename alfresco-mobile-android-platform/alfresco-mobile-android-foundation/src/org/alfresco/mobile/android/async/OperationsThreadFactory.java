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
package org.alfresco.mobile.android.async;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import java.util.concurrent.ThreadFactory;

import android.os.Process;

public class OperationsThreadFactory implements ThreadFactory
{

    @Override
    public Thread newThread(Runnable r)
    {
        return new OperationThread(r);
    }

    private static class OperationThread extends Thread
    {
        int priority = THREAD_PRIORITY_BACKGROUND;

        public OperationThread(Runnable r)
        {
            super(r);
            if (r instanceof Operation)
            {
                priority = ((Operation) r).getPriority();
                setName(r.getClass().getName());
            }
        }

        @Override
        public void run()
        {
            Process.setThreadPriority(priority);
            super.run();
        }
    }

}
