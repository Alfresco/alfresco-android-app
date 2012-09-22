package org.alfresco.mobile.android.application.accounts.signup;

import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.CloudConstant;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.utils.DateUtils;
import org.alfresco.mobile.android.api.utils.JsonDataWriter;
import org.alfresco.mobile.android.api.utils.JsonUtils;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils.Response;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.http.HttpStatus;

import android.util.Log;

/**
 * Represents a request from a user to sign up to Alfresco Cloud.
 * 
 * @author Jean Marie Pascal
 */
public class CloudSignupRequest
{
    private String identifier;

    private String apiKey;

    private String registrationKey;

    private String emailAdress;

    private String registrationTime;

    private Boolean isRegistered;

    private Boolean isActivated;

    private Boolean isPreRegistered;

    public static CloudSignupRequest parsePublicAPIJson(Map<String, Object> json)
    {
        CloudSignupRequest request = new CloudSignupRequest();

        request.identifier = JSONConverter.getString(json, CloudConstant.ID_VALUE);
        request.apiKey = JSONConverter.getString(json, CloudConstant.CLOUD_KEY);
        request.registrationKey = JSONConverter.getString(json, CloudConstant.CLOUD_REGISTRATION_KEY);
        request.emailAdress = JSONConverter.getString(json, CloudConstant.CLOUD_EMAIL_VALUE);
        request.registrationTime = JSONConverter.getString(json, CloudConstant.CLOUD_REGISTRATIONDATE);

        if (json.containsKey(CloudConstant.CLOUD_ISACTIVATED))
            request.isActivated = JSONConverter.getBoolean(json, CloudConstant.CLOUD_ISACTIVATED);
        if (json.containsKey(CloudConstant.CLOUD_ISREGISTERED))
            request.isRegistered = JSONConverter.getBoolean(json, CloudConstant.CLOUD_ISREGISTERED);
        if (json.containsKey(CloudConstant.CLOUD_ISPREREGISTERED))
            request.isPreRegistered = JSONConverter.getBoolean(json, CloudConstant.CLOUD_ISPREREGISTERED);

        return request;
    }
    
    // TODO Replace by official one.
    private static final String SIGNUP_CLOUD_URL = "http://devapis.alfresco.com";

    // private static final String SIGNUP_CLOUD_URL = CLOUD_URL;

    @SuppressWarnings("unchecked")
    public static CloudSignupRequest signup(String firstName, String lastName, String emailAddress, String password,
            String apiKey)
    {
        UrlBuilder url = new UrlBuilder(getCloudSignupUrl(SIGNUP_CLOUD_URL));

        // prepare json data
        JSONObject jo = new JSONObject();
        jo.put(CloudConstant.CLOUD_EMAIL_VALUE, emailAddress);
        jo.put(CloudConstant.CLOUD_FIRSTNAME_VALUE, firstName);
        jo.put(CloudConstant.CLOUD_LASTNAME_VALUE, lastName);
        jo.put(CloudConstant.CLOUD_PASSWORD_VALUE, password);
        jo.put(CloudConstant.CLOUD_KEY, apiKey);
        jo.put(CloudConstant.CLOUD_SOURCE_VALUE, "mobile-android");

        final JsonDataWriter formData = new JsonDataWriter(jo);

        // send and parse
        HttpUtils.Response resp = org.alfresco.mobile.android.api.utils.HttpUtils.invokePOST(url,
                formData.getContentType(), new HttpUtils.Output()
                {
                    public void write(OutputStream out) throws Exception
                    {
                        formData.write(out);
                    }
                });

        if (resp.getErrorContent() == null)
        {
            Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());
            return parsePublicAPIJson((Map<String, Object>) json
                    .get(CloudConstant.CLOUD_REGISTRATION));
        }
        else
        {
            Log.d("error", resp.getErrorContent());
            throw new AlfrescoServiceException(ErrorCodeRegistry.SESSION_SIGNUP_ERROR, resp.getErrorContent());
        }
    }

    public static boolean checkAccount(CloudSignupRequest signupRequest)
    {
        UrlBuilder url = new UrlBuilder(getVerifiedAccountUrl(signupRequest, SIGNUP_CLOUD_URL));

        Response resp = org.alfresco.mobile.android.api.utils.HttpUtils.invokeGET(url, null);
        if (resp.getResponseCode() == HttpStatus.SC_NOT_FOUND)
        {
            return true;
        }
        else if (resp.getErrorContent() == null)
        {
            Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());
            CloudSignupRequest request = parsePublicAPIJson(json);
            return request.isActivated() && request.isRegistered();
        }
        else
        {
            Log.d("error", resp.getErrorContent());
            return false;
        }
    }
    
    // ///////////////////////////////////////////////////////////////////////////////
    // CLOUD ACCOUNT
    // //////////////////////////////////////////////////////////////////////////////

    private static final String CLOUD_SIGNUP = "/alfresco/service/internal/cloud/accounts/signupqueue";

    private static final String VARIABLE_ACCOUNTID = "{accountid}";

    private static final String VARIABLE_ACCOUNTKEY = "{accountkey}";

    private static final String CLOUD_SIGNUP_ACCOUNT = CLOUD_SIGNUP + "/{accountid}?key={accountkey}";

    private static String getCloudSignupUrl(String baseUrl)
    {
        return new StringBuilder(baseUrl).append(CLOUD_SIGNUP).toString();
    }

    private static String getVerifiedAccountUrl(CloudSignupRequest request, String baseUrl)
    {
        return new StringBuilder(baseUrl).append(
                CLOUD_SIGNUP_ACCOUNT.replace(VARIABLE_ACCOUNTID, request.getIdentifier()).replace(VARIABLE_ACCOUNTKEY,
                        request.getRegistrationKey())).toString();
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public String getRegistrationKey()
    {
        return registrationKey;
    }

    public String getEmailAddress()
    {
        return emailAdress;
    }

    public GregorianCalendar getRegistrationTime()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(DateUtils.parseJsonDate(registrationTime));
        return cal;
    }

    public Boolean isRegistered()
    {
        return isRegistered;
    }

    public Boolean isActivated()
    {
        return isActivated;
    }

    public Boolean isPreRegistered()
    {
        return isPreRegistered;
    }
}
