/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.utils;

import org.alfresco.mobile.android.application.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

public class EmailUtils
{
    public static boolean createMailWithAttachment(Context c, String subject, String content, Uri attachment)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(content));
        i.putExtra(Intent.EXTRA_STREAM, attachment);
        i.setType("text/plain");
        c.startActivity(Intent.createChooser(i, c.getString(R.string.send_email)));
        return true;
    }

    public static boolean createMailWithLink(Context c, String subject, String content, Uri link)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, content);
        i.setType("text/plain");
        c.startActivity(Intent.createChooser(i, String.format(c.getString(R.string.send_email), link.toString())));

        return true;
    }
}
