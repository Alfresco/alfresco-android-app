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

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.alfresco.mobile.android.async.node.download.DownloadRequest;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

@SuppressWarnings("rawtypes")
public class Operator
{
    private static final String TAG = Operator.class.getName();

    static Operator singleton = null;

    final OperationsDispatcher dispatcher;

    private final Context context;

    boolean debugging;

    boolean shutdown;

    private AlfrescoAccount account;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public static Operator with(Context context)
    {
        if (singleton == null)
        {
            singleton = new Builder(context).build();
        }

        return singleton;
    }

    public static Operator with(Context context, AlfrescoAccount acc)
    {
        if (singleton == null)
        {
            singleton = new Builder(context, acc).build();
        }

        if (singleton.account == null || (acc != null && acc.getId() != singleton.account.getId()))
        {
            singleton.account = acc;
        }

        return singleton;
    }

    Operator(Context context, OperationsDispatcher dispatcher, AlfrescoAccount acc, boolean debugging)
    {
        this.context = context;
        this.dispatcher = dispatcher;
        this.debugging = debugging;
        this.account = acc;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CANCEL
    // ///////////////////////////////////////////////////////////////////////////
    public void cancel(String operationUri)
    {
        dispatcher.dispatchCancel(operationUri);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RETRY
    // ///////////////////////////////////////////////////////////////////////////
    public void retry(Uri operationUri)
    {
        OperationRequest request = OperationsFactory.getRequest(context, operationUri);
        if (request == null) { throw new IllegalArgumentException("Request must not be null."); }
        new OperationCreator(this, request).execute();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST
    // ///////////////////////////////////////////////////////////////////////////
    public String load(OperationRequest.OperationBuilder builder)
    {
        if (builder == null) { throw new IllegalArgumentException("Request must not be null."); }
        return new OperationCreator(this, builder).execute();
    }

    public String load(List<OperationRequest.OperationBuilder> builders)
    {
        if (builders == null) { throw new IllegalArgumentException("Requests must not be null."); }
        return new OperationCreator(this, builders).execute();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST
    // ///////////////////////////////////////////////////////////////////////////
    void enqueueAndSubmit(OperationAction action)
    {
        submit(action);
    }

    void submit(OperationAction action)
    {
        if (action.request.notificationVisibility == OperationRequest.VISIBILITY_NOTIFICATIONS)
        {
            AlfrescoNotificationManager.getInstance(context).monitorChannel(action.request.requestTypeId);
        }
        dispatcher.dispatchSubmit(action);
    }

    void complete(Operation operation)
    {
        Log.d("Operations", "[Complete] " + operation.getOperationId());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    /** Stops this instance from accepting further requests. */
    public void shutdown()
    {
        if (this == singleton) { throw new UnsupportedOperationException(
                "Default singleton instance cannot be shutdown."); }
        if (shutdown) { return; }
        dispatcher.shutdown();
        shutdown = true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Context getContext()
    {
        return context;
    }

    public AlfrescoAccount getAccount()
    {
        return account;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // HANDLER
    // ///////////////////////////////////////////////////////////////////////////
    static final Handler HANDLER = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case OperationsDispatcher.OPERATION_BATCH_COMPLETE:
                {
                    List<Operation> batch = (List<Operation>) msg.obj;
                    for (Operation operation : batch)
                    {
                        operation.operator.complete(operation);
                        if (operation.request.notificationVisibility == OperationRequest.VISIBILITY_NOTIFICATIONS)
                        {
                            AlfrescoNotificationManager.getInstance(operation.operator.context).unMonitorChannel(operation.request.requestTypeId);
                        }
                        EventBusManager.getInstance().post(new BatchOperationEvent(operation.action.groupKey));
                        /*if (!operation.isCancelled() || (!operation.request.isLongRunning() && operation.isCancelled()))
                        {
                            OperationsUtils.removeOperationUri(operation.operator.context, operation.action.request);
                        }*/
                    }
                    break;
                }
                default:
                    throw new AssertionError("Unknown handler message received: " + msg.what);
            }
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    /** Fluent API for creating {@link Operator} instances. */
    public static class Builder
    {
        private final Context context;

        private ExecutorService service;

        private ExecutorService serviceLongRunning;

        private boolean debugging;

        private AlfrescoAccount account;

        public Builder(Context context)
        {
            if (context == null) { throw new IllegalArgumentException("Context must not be null."); }
            this.context = context.getApplicationContext();
        }

        /** Start building a new {@link Operator} instance. */
        public Builder(Context context, AlfrescoAccount acc)
        {
            if (context == null) { throw new IllegalArgumentException("Context must not be null."); }
            this.context = context.getApplicationContext();
            this.account = acc;
        }

        /** Whether debugging is enabled or not. */
        public Builder debugging(boolean debugging)
        {
            this.debugging = debugging;
            return this;
        }

        /** Create the {@link Operator} instance. */
        public Operator build()
        {
            Context context = this.context;

            if (service == null)
            {
                service = new OperationsPoolExecutor();
            }

            if (serviceLongRunning == null)
            {
                serviceLongRunning = new OperationsPoolExecutor();
            }

            OperationsDispatcher dispatcher = new OperationsDispatcher(context, service, serviceLongRunning, HANDLER);
            return new Operator(context, dispatcher, account, debugging);
        }
    }

}
