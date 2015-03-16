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

import java.lang.reflect.Constructor;

import org.alfresco.mobile.android.async.Operation.OperationCallback;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.configuration.ConfigUtils;
import org.alfresco.mobile.android.platform.provider.CursorUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class OperationsFactory
{

    // ////////////////////////////////////////////////////
    // REQUEST
    // ////////////////////////////////////////////////////
    public static OperationRequest getRequest(Context context, Uri operationUri)
    {
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(operationUri, OperationSchema.COLUMN_ALL, null, null, null);
            cursor.moveToFirst();
            int operationId = cursor.getInt(OperationSchema.COLUMN_REQUEST_TYPE_ID);

            String s = ConfigUtils.getString(context, ConfigUtils.FAMILY_OPERATION,
                    Integer.toString(operationId));
            if (s == null)
            {
                Log.e("ApplicationManager", "Error during OperationRequest creation : " + operationId);
                return null;
            }
            return createRequest(cursor, s);
        }
        catch (Exception e)
        {
            Log.e("ApplicationManager", "Error during OperationRequest creation : " + operationUri);
        }
        finally
        {
            CursorUtils.closeCursor(cursor);
        }
        return null;
    }

    private static OperationRequest createRequest(Cursor cursor, String className)
    {
        OperationRequest s = null;
        try
        {
            Constructor<?> t = Class.forName(className).getDeclaredConstructor(Cursor.class);
            s = (OperationRequest) t.newInstance(cursor);
        }
        catch (Exception e)
        {
            Log.e("ApplicationManager", "Error during OperationRequest creation : " + className);
        }
        finally
        {
            CursorUtils.closeCursor(cursor);
        }
        return s;
    }

    // ////////////////////////////////////////////////////
    // TASK
    // ////////////////////////////////////////////////////
    @SuppressWarnings("rawtypes")
    public static Operation getTask(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        String s = ConfigUtils.getString(operator.getContext(), ConfigUtils.FAMILY_OPERATION,
                action.request.getClass().getSimpleName());
        if (s == null)
        {
            Log.e("ApplicationManager", "Error during Operation creation : "
                    + action.request.getClass().getSimpleName());
            return null;
        }
        return createTask(s, operator, dispatcher, action);
    }

    @SuppressWarnings("rawtypes")
    private static Operation createTask(String className, Operator operator, OperationsDispatcher dispatcher,
            OperationAction request)
    {
        BaseOperation s = null;
        try
        {
            Constructor<?> t = Class.forName(className).getDeclaredConstructor(Operator.class,
                    OperationsDispatcher.class, OperationAction.class);
            s = (BaseOperation) t.newInstance(operator, dispatcher, request);
        }
        catch (Exception e)
        {
            Log.e("ApplicationManager", "Error during Operation creation : " + className);
        }
        return s;
    }

    // ////////////////////////////////////////////////////
    // CALLBACK
    // ////////////////////////////////////////////////////
    public static OperationCallback getCallBack(Operator operator, OperationsDispatcher dispatcher,
            OperationAction action)
    {
        String s = ConfigUtils.getString(operator.getContext(),
                ConfigUtils.FAMILY_OPERATION_CALLBACK, action.request.getClass().getSimpleName());
        if (s == null)
        {
            Log.w("ApplicationManager", "No callback for : " + action.request.getClass().getSimpleName());
            return null;
        }
        return createCallBack(s, operator.getContext());
    }

    @SuppressWarnings("rawtypes")
    private static OperationCallback createCallBack(String className, Context context)
    {
        OperationCallback s = null;
        try
        {
            Constructor<?> t = Class.forName(className).getDeclaredConstructor(Context.class);
            s = (OperationCallback) t.newInstance(context);
        }
        catch (Exception e)
        {
            Log.w("ApplicationManager", "No callback for : " + className);
        }
        return s;
    }
}
