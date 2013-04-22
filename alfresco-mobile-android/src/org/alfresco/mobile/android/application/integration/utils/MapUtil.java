package org.alfresco.mobile.android.application.integration.utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MapUtil
{
    public static String mapToString(Map<String, Serializable> map)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet())
        {
            if (stringBuilder.length() > 0)
            {
                stringBuilder.append("&");
            }
            Serializable value = map.get(key);
            try
            {
                stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                stringBuilder.append("=");
                stringBuilder.append(value != null ? URLEncoder.encode(value.toString(), "UTF-8") : "");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return stringBuilder.toString();
    }

    public static Map<String, String> stringToMap(String input)
    {
        Map<String, String> map = new HashMap<String, String>();

        String[] nameValuePairs = input.split("&");
        for (String nameValuePair : nameValuePairs)
        {
            String[] nameValue = nameValuePair.split("=");
            try
            {
                map.put(URLDecoder.decode(nameValue[0], "UTF-8"),
                        nameValue.length > 1 ? URLDecoder.decode(nameValue[1], "UTF-8") : "");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return map;
    }
}
