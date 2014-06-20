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
package org.alfresco.mobile.android.async.session.network;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.api.session.CloudNetwork;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.impl.CloudNetworkImpl;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class NetworksOperation extends ListingOperation<PagingResult<CloudNetwork>>
{
    private static final String TAG = NetworksOperation.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public NetworksOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<PagingResult<CloudNetwork>> doInBackground()
    {
        try
        {
            LoaderResult<PagingResult<CloudNetwork>> result = new LoaderResult<PagingResult<CloudNetwork>>();
            PagingResult<CloudNetwork> pagingResult = null;

            try
            {
                List<CloudNetwork> tmpNetworks = ((CloudSession) session).getNetworks();
                List<CloudNetwork> networks = new ArrayList<CloudNetwork>(tmpNetworks.size());
                for (CloudNetwork cloudNetwork : tmpNetworks)
                {
                    if (((CloudNetworkImpl) cloudNetwork).isEnabled())
                    {
                        networks.add(cloudNetwork);
                    }
                }
                pagingResult = new PagingResultImpl<CloudNetwork>(networks, false, networks.size());
            }
            catch (Exception e)
            {
                result.setException(e);
            }

            result.setData(pagingResult);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<PagingResult<CloudNetwork>>();
    }

    @Override
    protected void onPostExecute(LoaderResult<PagingResult<CloudNetwork>> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new NetworksEvent(getRequestId(), result));
    }
}
