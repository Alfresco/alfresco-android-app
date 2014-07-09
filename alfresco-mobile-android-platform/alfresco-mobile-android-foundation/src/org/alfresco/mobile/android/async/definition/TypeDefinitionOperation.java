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
package org.alfresco.mobile.android.async.definition;

import org.alfresco.mobile.android.api.model.TypeDefinition;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class TypeDefinitionOperation extends BaseOperation<TypeDefinition>
{
    private static final String TAG = TypeDefinitionOperation.class.getName();

    private TypeDefinition typeDefinition;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public TypeDefinitionOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<TypeDefinition> doInBackground()
    {
        LoaderResult<TypeDefinition> result = new LoaderResult<TypeDefinition>();

        try
        {
            checkCancel();
            result = super.doInBackground();

            switch (((TypeDefinitionRequest) request).typeDefinitionId)
            {
                case TypeDefinitionRequest.DOCUMENT:
                    typeDefinition = session.getServiceRegistry().getModelDefinitionService()
                            .getDocumentTypeDefinition(((TypeDefinitionRequest) request).type);
                    break;
                case TypeDefinitionRequest.FOLDER:
                    typeDefinition = session.getServiceRegistry().getModelDefinitionService()
                            .getFolderTypeDefinition(((TypeDefinitionRequest) request).type);
                /*case TypeDefinitionRequest.ASPECT:
                    typeDefinition = session.getServiceRegistry().getModelDefinitionService()
                            .getAspectDefinition(((TypeDefinitionRequest) request).type);*/
                case TypeDefinitionRequest.TASK:
                    typeDefinition = session.getServiceRegistry().getModelDefinitionService()
                            .getTaskTypeDefinition(((TypeDefinitionRequest) request).type);
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData((TypeDefinition) typeDefinition);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<TypeDefinition> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new TypeDefinitionEvent(getRequestId(), result));
    }
}
