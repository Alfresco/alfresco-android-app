package org.alfresco.mobile.android.application.utils;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.utils.NodeRefUtils;

import android.text.TextUtils;

public class UrlFinder
{
    private static final String NODEREF = "noderef=";

    private static final String ID = "id=";

    private static final List<String> patterns = new ArrayList<String>(2);
    static
    {
        patterns.add(NODEREF);
        patterns.add(ID);
    }

    public static String getIdentifier(String url)
    {
        String identifier = null, tmp = null;
        tmp = url.toLowerCase();
        for (String pattern : patterns)
        {
            if (tmp.contains(pattern.toLowerCase()))
            {
                identifier = TextUtils.substring(tmp, tmp.lastIndexOf(pattern) + pattern.length(), tmp.length());

                if (identifier.contains("&"))
                {
                    identifier = TextUtils.substring(identifier, 0, identifier.indexOf("&"));
                }

                if (NodeRefUtils.isNodeRef(identifier)) { return identifier; }
                if (NodeRefUtils.isVersionIdentifier(identifier)) { return identifier; }
                if (NodeRefUtils.isIdentifier(identifier)) { return identifier; }
            }
        }
        return null;
    }

}
