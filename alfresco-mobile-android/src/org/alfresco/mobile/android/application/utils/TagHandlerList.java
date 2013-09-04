/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html.TagHandler;

public class TagHandlerList implements TagHandler
{

    boolean first = true;

    String parent = null;

    int index = 1;

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader)
    {
        if (tag.equals("ul"))
        {
            parent = "ul";
        }
        else if (tag.equals("ol"))
        {
            parent = "ol";
        }
        if (tag.equals("li"))
        {
            if (parent.equals("ul"))
            {
                if (first)
                {
                    output.append("\n\t• ");
                    first = false;
                }
                else
                {
                    first = true;
                }
            }
            else
            {
                if (first)
                {
                    output.append("\n\t" + index + ". ");
                    first = false;
                    index++;
                }
                else
                {
                    first = true;
                }
            }
        }
    }
}
