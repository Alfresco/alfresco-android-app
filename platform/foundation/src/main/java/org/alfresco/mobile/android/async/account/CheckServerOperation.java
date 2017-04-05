/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.async.account;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.network.NetworkHttpInvoker;
import org.alfresco.mobile.android.api.session.authentication.SamlInfo;
import org.alfresco.mobile.android.api.session.authentication.impl.Saml2AuthHelper;
import org.alfresco.mobile.android.api.session.authentication.impl.Saml2InfoImpl;
import org.alfresco.mobile.android.api.utils.JsonUtils;
import org.alfresco.mobile.android.api.utils.OnPremiseUrlRegistry;
import org.alfresco.mobile.android.api.utils.PublicAPIUrlRegistry;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

public class CheckServerOperation extends BaseOperation<URLInfo>
{
    protected String baseUrl, username, password;

    protected Boolean https;

    protected ArrayList<URLInfo> infos = new ArrayList<>(5);

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public CheckServerOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof CheckServerRequest)
        {
            this.baseUrl = ((CheckServerRequest) request).baseUrl;
            this.https = ((CheckServerRequest) request).https;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<URLInfo> doInBackground()
    {
        LoaderResult<URLInfo> result = new LoaderResult<>();
        URLInfo finalUrlInfo = null;
        Exception latestError = null;
        String tmpURL = baseUrl.toLowerCase();
        String testUrl;
        boolean isComplete = false;
        try
        {
            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            // Default Value
            if (tmpURL.startsWith("http://") || tmpURL.startsWith("https://"))
            {
                // Plain URL
                infos.add(new URLInfo(baseUrl));
                isComplete = true;
            }
            else
            {

                boolean isCMIS = false;
                // Incomplete URL
                StringBuilder builder = new StringBuilder();
                builder.append((https) ? "https://" : "http://");
                builder.append(tmpURL);
                if (tmpURL.endsWith("/alfresco") || tmpURL.endsWith("/alfresco/"))
                {
                    // Do nothing
                    isCMIS = false;
                }
                else if (tmpURL.endsWith(PublicAPIUrlRegistry.BINDING_NETWORK_CMISATOM)
                        || tmpURL.endsWith(OnPremiseUrlRegistry.BINDING_CMISATOM)
                        || tmpURL.endsWith(OnPremiseUrlRegistry.BINDING_CMIS))
                {
                    // Do nothing
                    isCMIS = true;
                }
                else
                {
                    builder.append(tmpURL.endsWith("/") ? "alfresco" : "/alfresco");
                }
                testUrl = builder.toString();
                tmpURL = testUrl;
                infos.add(new URLInfo(testUrl, testUrl, isCMIS, false));
            }

            // Lets prepare other cases
            // Remove alfresco at the end
            if (tmpURL.endsWith("/alfresco/") || tmpURL.endsWith("/alfresco"))
            {
                testUrl = tmpURL.replace(tmpURL.endsWith("/alfresco/") ? "/alfresco/" : "/alfresco", "");
                infos.add(new URLInfo(tmpURL, testUrl, false, isComplete));
            }
            else if (tmpURL.endsWith("/share/") || tmpURL.endsWith("/share"))
            {
                testUrl = tmpURL.replace(tmpURL.endsWith("/share/") ? "/share/" : "/share", "/alfresco");
                infos.add(new URLInfo(tmpURL, testUrl, false, isComplete));
            }
            else if (tmpURL.endsWith(PublicAPIUrlRegistry.BINDING_NETWORK_CMISATOM))
            {
                testUrl = tmpURL.replace(PublicAPIUrlRegistry.BINDING_NETWORK_CMISATOM, "");
                infos.add(new URLInfo(tmpURL, testUrl, true, isComplete));
            }
            else if (tmpURL.endsWith(OnPremiseUrlRegistry.BINDING_CMISATOM))
            {
                testUrl = tmpURL.replace(OnPremiseUrlRegistry.BINDING_CMISATOM, "");
                infos.add(new URLInfo(tmpURL, testUrl, true, isComplete));
            }
            else if (tmpURL.endsWith(OnPremiseUrlRegistry.BINDING_CMIS))
            {
                testUrl = tmpURL.replace(OnPremiseUrlRegistry.BINDING_CMIS, "");
                infos.add(new URLInfo(tmpURL, testUrl, true, isComplete));
            }
            else
            {
                testUrl = tmpURL.concat(tmpURL.endsWith("/") ? "alfresco" : "/alfresco");
                infos.add(new URLInfo(tmpURL, testUrl));
            }

            // List of endpoint
            for (URLInfo urlInfo : infos)
            {
                try
                {
                    UrlBuilder builder = new UrlBuilder(urlInfo.testUrl);
                    builder.addPath(urlInfo.testUrl.endsWith("/") ? "service/api/server" : "/service/api/server");
                    Response resp = NetworkHttpInvoker.invokeGET(builder, new HashMap<String, List<String>>(0));

                    if (resp.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        // We got our ticket!
                        finalUrlInfo = urlInfo;
                        break;
                    }
                }
                catch (Exception e)
                {
                    latestError = e;
                }
            }

            if (finalUrlInfo == null)
            {
                latestError = new UnknownHostException("Unable to find Alfresco server.");
            }
            else
            {
                // Time to Test SAML configuration
                Saml2AuthHelper helper = new Saml2AuthHelper(finalUrlInfo.baseUrl);
                UrlBuilder builder = new UrlBuilder(helper.getInfoUrl());
                Response resp = NetworkHttpInvoker.invokeGET(builder, new HashMap<String, List<String>>(0));

                if (resp.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());
                    Saml2InfoImpl data = new Saml2InfoImpl(json);
                    finalUrlInfo = new URLInfo(finalUrlInfo.baseUrl, finalUrlInfo.testUrl, finalUrlInfo.enforceCMIS,
                            finalUrlInfo.isComplete, data);
                }
            }
        }
        catch (Exception e)
        {
            latestError = e;
        }

        result.setException(latestError);
        result.setData(finalUrlInfo);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public String getBaseUrl()
    {
        return baseUrl;
    }

    public static SamlInfo getSamlInfo(String baseSamlUrl)
    {
        try
        {
            // Time to Test SAML configuration
            Saml2AuthHelper helper = new Saml2AuthHelper(baseSamlUrl);
            UrlBuilder builder = new UrlBuilder(helper.getInfoUrl());
            Response resp = NetworkHttpInvoker.invokeGET(builder, new HashMap<String, List<String>>(0));

            if (resp.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());
                return new Saml2InfoImpl(json);
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<URLInfo> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new CheckServerEvent(getRequestId(), result));
    }
}
