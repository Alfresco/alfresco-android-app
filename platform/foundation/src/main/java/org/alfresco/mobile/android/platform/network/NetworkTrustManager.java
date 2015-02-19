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
package org.alfresco.mobile.android.platform.network;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * This class is application specific implementation of TrustManager. It's
 * passed as parameter to the SDK and it disables SSL certificate validation
 * during SSL Handshake. <br/>
 * This TrustManager use by default the default system TrustManager. It's only
 * in case of failure we accept the server certification.
 * 
 * @author Jean Marie Pascal
 */
public class NetworkTrustManager implements X509TrustManager
{
    private X509TrustManager defaultTrustManager;

    public NetworkTrustManager() throws NoSuchAlgorithmException, KeyStoreException
    {
        super();

        // Initiate the default trust manager.
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        this.defaultTrustManager = findX509TrustManager(factory);
    }

    // Client certificate is checked by the default Manager.
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        defaultTrustManager.checkClientTrusted(chain, authType);
    }

    // Server certificate are checked firstly by the default manager
    // In a second time, if the first check failed, we allow all certificate.
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {

    }

    static X509TrustManager findX509TrustManager(TrustManagerFactory tmf)
    {
        TrustManager tms[] = tmf.getTrustManagers();
        for (int i = 0; i < tms.length; i++)
        {
            if (tms[i] instanceof X509TrustManager) { return (X509TrustManager) tms[i]; }
        }
        return null;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return this.defaultTrustManager.getAcceptedIssuers();
    }

}
