/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.manager;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;

/**
 * Static Mimetype Manager : Extends SDK version. Should be replaced by a more
 * dynamic implementation based on content provider ?
 * 
 * @author jpascal
 */
public class MimeTypeManager extends org.alfresco.mobile.android.ui.manager.MimeTypeManager
{

    private static final Map<String, Integer> EXT2ICON = new HashMap<String, Integer>();

    private static final Map<String, Integer> EXT2LARGEICON = new HashMap<String, Integer>();

    public static String getExtension(String uri)
    {
        if (uri == null) { return null; }

        int dot = uri.lastIndexOf(".".charAt(0));
        if (dot > 0)
        {
            return uri.substring(dot + 1).toLowerCase();
        }
        else
        {
            return "";
        }
    }
    
    public static String getName(String uri)
    {
        if (uri == null) { return null; }

        int dot = uri.lastIndexOf(".".charAt(0));
        if (dot > 0)
        {
            return uri.substring(0, dot).toLowerCase();
        }
        else
        {
            return uri;
        }
    }

    public static String getMIMEType(String fileName)
    {
        return MimeTypes.getMIMEType(fileName);
    }

    public static int getIcon(String fileName)
    {
        int iconId = R.drawable.mime_generic;
        if (EXT2ICON.get(getExtension(fileName)) != null)
        {
            iconId = EXT2ICON.get(getExtension(fileName));
        }
        return iconId;
    }

    public static int getIcon(String fileName, boolean isLarge)
    {
        if (!isLarge) { return getIcon(fileName); }
        int iconId = R.drawable.mime_256_generic;
        if (EXT2LARGEICON.get(getExtension(fileName)) != null)
        {
            iconId = EXT2LARGEICON.get(getExtension(fileName));
        }
        return iconId;
    }

    static
    {
        // extension to MIME type
        EXT2ICON.put("", R.drawable.mime_generic);
        EXT2ICON.put("3gpp", R.drawable.mime_audio);
        EXT2ICON.put("aep", R.drawable.mime_aep);
        EXT2ICON.put("ai", R.drawable.mime_ai);
        EXT2ICON.put("aif", R.drawable.mime_audio);
        EXT2ICON.put("aifc", R.drawable.mime_audio);
        EXT2ICON.put("aiff", R.drawable.mime_audio);
        EXT2ICON.put("asf", R.drawable.mime_video);
        EXT2ICON.put("asnd", R.drawable.mime_asnd);
        EXT2ICON.put("asr", R.drawable.mime_video);
        EXT2ICON.put("asx", R.drawable.mime_video);
        EXT2ICON.put("au", R.drawable.mime_audio);
        EXT2ICON.put("avi", R.drawable.mime_video);
        EXT2ICON.put("bas", R.drawable.mime_txt);
        EXT2ICON.put("bmp", R.drawable.mime_img);
        EXT2ICON.put("c", R.drawable.mime_txt);
        EXT2ICON.put("cmx", R.drawable.mime_img);
        EXT2ICON.put("cod", R.drawable.mime_img);
        EXT2ICON.put("css", R.drawable.mime_txt);
        EXT2ICON.put("doc", R.drawable.mime_doc);
        EXT2ICON.put("docx", R.drawable.mime_doc);
        EXT2ICON.put("doct", R.drawable.mime_doc);
        EXT2ICON.put("dot", R.drawable.mime_doc);
        EXT2ICON.put("eml", R.drawable.mime_eml);
        EXT2ICON.put("eps", R.drawable.mime_txt);
        EXT2ICON.put("etx", R.drawable.mime_txt);
        EXT2ICON.put("fla", R.drawable.mime_fla);
        EXT2ICON.put("fxp", R.drawable.mime_fxp);
        EXT2ICON.put("gif", R.drawable.mime_img);
        EXT2ICON.put("gtar", R.drawable.mime_zip);
        EXT2ICON.put("gz", R.drawable.mime_zip);
        EXT2ICON.put("h", R.drawable.mime_txt);
        EXT2ICON.put("htc", R.drawable.mime_txt);
        EXT2ICON.put("htm", R.drawable.mime_html);
        EXT2ICON.put("html", R.drawable.mime_html);
        EXT2ICON.put("htt", R.drawable.mime_html);
        EXT2ICON.put("ico", R.drawable.mime_img);
        EXT2ICON.put("ief", R.drawable.mime_img);
        EXT2ICON.put("indd", R.drawable.mime_indd);
        EXT2ICON.put("jfif", R.drawable.mime_img);
        EXT2ICON.put("jpe", R.drawable.mime_img);
        EXT2ICON.put("jpeg", R.drawable.mime_img);
        EXT2ICON.put("jpg", R.drawable.mime_img);
        EXT2ICON.put("key", R.drawable.mime_keynote);
        EXT2ICON.put("lsf", R.drawable.mime_video);
        EXT2ICON.put("lsx", R.drawable.mime_video);
        EXT2ICON.put("m3u", R.drawable.mime_audio);
        EXT2ICON.put("mhtv", R.drawable.mime_html);
        EXT2ICON.put("mhtml", R.drawable.mime_html);
        EXT2ICON.put("mid", R.drawable.mime_audio);
        EXT2ICON.put("mov", R.drawable.mime_video);
        EXT2ICON.put("movie", R.drawable.mime_video);
        EXT2ICON.put("mp2", R.drawable.mime_video);
        EXT2ICON.put("mp3", R.drawable.mime_mp3);
        EXT2ICON.put("mp4", R.drawable.mime_video);
        EXT2ICON.put("mpa", R.drawable.mime_video);
        EXT2ICON.put("mpe", R.drawable.mime_video);
        EXT2ICON.put("mpeg", R.drawable.mime_video);
        EXT2ICON.put("mpg", R.drawable.mime_video);
        EXT2ICON.put("mpv2", R.drawable.mime_video);
        EXT2ICON.put("numbers", R.drawable.mime_numbers);
        EXT2ICON.put("odg", R.drawable.mime_odg);
        EXT2ICON.put("odp", R.drawable.mime_odp);
        EXT2ICON.put("ods", R.drawable.mime_ods);
        EXT2ICON.put("odt", R.drawable.mime_odt);
        EXT2ICON.put("pages", R.drawable.mime_pages);
        EXT2ICON.put("pbm", R.drawable.mime_img);
        EXT2ICON.put("pdf", R.drawable.mime_pdf);
        EXT2ICON.put("pgm", R.drawable.mime_img);
        EXT2ICON.put("png", R.drawable.mime_img);
        EXT2ICON.put("pnm", R.drawable.mime_img);
        EXT2ICON.put("pot", R.drawable.mime_ppt);
        EXT2ICON.put("ppm", R.drawable.mime_img);
        EXT2ICON.put("ppj", R.drawable.mime_ppj);
        EXT2ICON.put("ppm", R.drawable.mime_img);
        EXT2ICON.put("pps", R.drawable.mime_ppt);
        EXT2ICON.put("ppt", R.drawable.mime_ppt);
        EXT2ICON.put("pptx", R.drawable.mime_ppt);
        EXT2ICON.put("ppsx", R.drawable.mime_ppt);
        EXT2ICON.put("potx", R.drawable.mime_ppt);
        EXT2ICON.put("qt", R.drawable.mime_video);
        EXT2ICON.put("ra", R.drawable.mime_audio);
        EXT2ICON.put("ram", R.drawable.mime_audio);
        EXT2ICON.put("ras", R.drawable.mime_img);
        EXT2ICON.put("rgb", R.drawable.mime_img);
        EXT2ICON.put("rmi", R.drawable.mime_audio);
        EXT2ICON.put("rtx", R.drawable.mime_txt);
        EXT2ICON.put("sct", R.drawable.mime_txt);
        EXT2ICON.put("snd", R.drawable.mime_audio);
        EXT2ICON.put("stm", R.drawable.mime_html);
        EXT2ICON.put("svg", R.drawable.mime_xml);
        EXT2ICON.put("swf", R.drawable.mime_swf);
        EXT2ICON.put("tar", R.drawable.mime_zip);
        EXT2ICON.put("tgz", R.drawable.mime_zip);
        EXT2ICON.put("tif", R.drawable.mime_img);
        EXT2ICON.put("tiff", R.drawable.mime_img);
        EXT2ICON.put("tsv", R.drawable.mime_txt);
        EXT2ICON.put("txt", R.drawable.mime_txt);
        EXT2ICON.put("uls", R.drawable.mime_txt);
        EXT2ICON.put("vcf", R.drawable.mime_txt);
        EXT2ICON.put("wav", R.drawable.mime_audio);
        EXT2ICON.put("xbm", R.drawable.mime_img);
        EXT2ICON.put("xla", R.drawable.mime_xls);
        EXT2ICON.put("xlc", R.drawable.mime_xls);
        EXT2ICON.put("xlm", R.drawable.mime_xls);
        EXT2ICON.put("xls", R.drawable.mime_xls);
        EXT2ICON.put("xlsx", R.drawable.mime_xls);
        EXT2ICON.put("xlt", R.drawable.mime_xls);
        EXT2ICON.put("xltx", R.drawable.mime_xls);
        EXT2ICON.put("xlw", R.drawable.mime_xls);
        EXT2ICON.put("xml", R.drawable.mime_xml);
        EXT2ICON.put("z", R.drawable.mime_zip);
        EXT2ICON.put("zip", R.drawable.mime_zip);
    }

    static
    {
        // extension to MIME type
        EXT2LARGEICON.put("", R.drawable.mime_256_generic);
        EXT2LARGEICON.put("3gpp", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("aep", R.drawable.mime_256_aep);
        EXT2LARGEICON.put("ai", R.drawable.mime_256_ai);
        EXT2LARGEICON.put("aif", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("aifc", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("aiff", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("asf", R.drawable.mime_256_video);
        EXT2LARGEICON.put("asnd", R.drawable.mime_256_asnd);
        EXT2LARGEICON.put("asr", R.drawable.mime_256_video);
        EXT2LARGEICON.put("asx", R.drawable.mime_256_video);
        EXT2LARGEICON.put("au", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("avi", R.drawable.mime_256_video);
        EXT2LARGEICON.put("bas", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("bmp", R.drawable.mime_256_img);
        EXT2LARGEICON.put("c", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("cmx", R.drawable.mime_256_img);
        EXT2LARGEICON.put("cod", R.drawable.mime_256_img);
        EXT2LARGEICON.put("css", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("doc", R.drawable.mime_256_doc);
        EXT2LARGEICON.put("docx", R.drawable.mime_256_doc);
        EXT2LARGEICON.put("doct", R.drawable.mime_256_doc);
        EXT2LARGEICON.put("dot", R.drawable.mime_256_doc);
        EXT2LARGEICON.put("eml", R.drawable.mime_256_eml);
        EXT2LARGEICON.put("eps", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("etx", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("fla", R.drawable.mime_256_fla);
        EXT2LARGEICON.put("fxp", R.drawable.mime_256_fxp);
        EXT2LARGEICON.put("gif", R.drawable.mime_256_img);
        EXT2LARGEICON.put("gtar", R.drawable.mime_256_zip);
        EXT2LARGEICON.put("gz", R.drawable.mime_256_zip);
        EXT2LARGEICON.put("h", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("htc", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("htm", R.drawable.mime_256_html);
        EXT2LARGEICON.put("html", R.drawable.mime_256_html);
        EXT2LARGEICON.put("htt", R.drawable.mime_256_html);
        EXT2LARGEICON.put("ico", R.drawable.mime_256_img);
        EXT2LARGEICON.put("ief", R.drawable.mime_256_img);
        EXT2LARGEICON.put("indd", R.drawable.mime_256_indd);
        EXT2LARGEICON.put("jfif", R.drawable.mime_256_img);
        EXT2LARGEICON.put("jpe", R.drawable.mime_256_img);
        EXT2LARGEICON.put("jpeg", R.drawable.mime_256_img);
        EXT2LARGEICON.put("jpg", R.drawable.mime_256_img);
        EXT2LARGEICON.put("key", R.drawable.mime_256_keynote);
        EXT2LARGEICON.put("lsf", R.drawable.mime_256_video);
        EXT2LARGEICON.put("lsx", R.drawable.mime_256_video);
        EXT2LARGEICON.put("m3u", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("mhtv", R.drawable.mime_256_html);
        EXT2LARGEICON.put("mhtml", R.drawable.mime_256_html);
        EXT2LARGEICON.put("mid", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("mov", R.drawable.mime_256_video);
        EXT2LARGEICON.put("movie", R.drawable.mime_256_video);
        EXT2LARGEICON.put("mp2", R.drawable.mime_256_video);
        EXT2LARGEICON.put("mp3", R.drawable.mime_256_mp3);
        EXT2LARGEICON.put("mp4", R.drawable.mime_256_video);
        EXT2LARGEICON.put("mpa", R.drawable.mime_256_video);
        EXT2LARGEICON.put("mpe", R.drawable.mime_256_video);
        EXT2LARGEICON.put("mpeg", R.drawable.mime_256_video);
        EXT2LARGEICON.put("mpg", R.drawable.mime_256_video);
        EXT2LARGEICON.put("mpv2", R.drawable.mime_256_video);
        EXT2LARGEICON.put("numbers", R.drawable.mime_256_numbers);
        EXT2LARGEICON.put("odg", R.drawable.mime_256_odg);
        EXT2LARGEICON.put("odp", R.drawable.mime_256_odp);
        EXT2LARGEICON.put("ods", R.drawable.mime_256_ods);
        EXT2LARGEICON.put("odt", R.drawable.mime_256_odt);
        EXT2LARGEICON.put("pages", R.drawable.mime_256_pages);
        EXT2LARGEICON.put("pbm", R.drawable.mime_256_img);
        EXT2LARGEICON.put("pdf", R.drawable.mime_256_pdf);
        EXT2LARGEICON.put("pgm", R.drawable.mime_256_img);
        EXT2LARGEICON.put("png", R.drawable.mime_256_img);
        EXT2LARGEICON.put("pnm", R.drawable.mime_256_img);
        EXT2LARGEICON.put("pot", R.drawable.mime_256_ppt);
        EXT2LARGEICON.put("ppm", R.drawable.mime_256_img);
        EXT2LARGEICON.put("ppj", R.drawable.mime_256_ppj);
        EXT2LARGEICON.put("ppm", R.drawable.mime_256_img);
        EXT2LARGEICON.put("pps", R.drawable.mime_256_ppt);
        EXT2LARGEICON.put("ppt", R.drawable.mime_256_ppt);
        EXT2LARGEICON.put("pptx", R.drawable.mime_256_ppt);
        EXT2LARGEICON.put("ppsx", R.drawable.mime_256_ppt);
        EXT2LARGEICON.put("potx", R.drawable.mime_256_ppt);
        EXT2LARGEICON.put("qt", R.drawable.mime_256_video);
        EXT2LARGEICON.put("ra", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("ram", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("ras", R.drawable.mime_256_img);
        EXT2LARGEICON.put("rgb", R.drawable.mime_256_img);
        EXT2LARGEICON.put("rmi", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("rtx", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("sct", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("snd", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("stm", R.drawable.mime_256_html);
        EXT2LARGEICON.put("svg", R.drawable.mime_256_xml);
        EXT2LARGEICON.put("swf", R.drawable.mime_256_swf);
        EXT2LARGEICON.put("tar", R.drawable.mime_256_zip);
        EXT2LARGEICON.put("tgz", R.drawable.mime_256_zip);
        EXT2LARGEICON.put("tif", R.drawable.mime_256_img);
        EXT2LARGEICON.put("tiff", R.drawable.mime_256_img);
        EXT2LARGEICON.put("tsv", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("txt", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("uls", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("vcf", R.drawable.mime_256_txt);
        EXT2LARGEICON.put("wav", R.drawable.mime_256_audio);
        EXT2LARGEICON.put("xbm", R.drawable.mime_256_img);
        EXT2LARGEICON.put("xla", R.drawable.mime_256_xls);
        EXT2LARGEICON.put("xlc", R.drawable.mime_256_xls);
        EXT2LARGEICON.put("xlm", R.drawable.mime_256_xls);
        EXT2LARGEICON.put("xls", R.drawable.mime_256_xls);
        EXT2LARGEICON.put("xlsx", R.drawable.mime_256_xls);
        EXT2LARGEICON.put("xlt", R.drawable.mime_256_xls);
        EXT2LARGEICON.put("xltx", R.drawable.mime_256_xls);
        EXT2LARGEICON.put("xlw", R.drawable.mime_256_xls);
        EXT2LARGEICON.put("xml", R.drawable.mime_256_xml);
        EXT2LARGEICON.put("z", R.drawable.mime_256_zip);
        EXT2LARGEICON.put("zip", R.drawable.mime_256_zip);
    }
}
