/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.mimetype;

import org.alfresco.mobile.android.application.database.DatabaseVersionNumber;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public final class MimeTypeSchema
{

    private MimeTypeSchema()
    {
    }

    public static final String TABLENAME = "mimetypes";

    public static final String COLUMN_ID = "_id";

    public static final int COLUMN_ID_ID = 0;

    public static final String COLUMN_EXTENSION = "extension";

    public static final int COLUMN_EXTENSION_ID = 1;

    public static final String COLUMN_TYPE = "type";

    public static final int COLUMN_TYPE_ID = COLUMN_EXTENSION_ID + 1;

    public static final String COLUMN_SUBTYPE = "subType";

    public static final int COLUMN_SUBTYPE_ID = COLUMN_TYPE_ID + 1;

    public static final String COLUMN_DESCRIPTION = "description";

    public static final int COLUMN_DESCRIPTION_ID = COLUMN_SUBTYPE_ID + 1;

    public static final String COLUMN_SMALL_ICON = "smallIcon";

    public static final int COLUMN_SMALL_ICON_ID = COLUMN_DESCRIPTION_ID + 1;

    public static final String COLUMN_LARGE_ICON = "largeIcon";

    public static final int COLUMN_LARGE_ICON_ID = COLUMN_SMALL_ICON_ID + 1;

    public static final String[] COLUMN_ALL = { COLUMN_ID, COLUMN_EXTENSION, COLUMN_TYPE, COLUMN_SUBTYPE,
            COLUMN_DESCRIPTION, COLUMN_SMALL_ICON, COLUMN_LARGE_ICON };

    private static final String QUERY_TABLE_CREATE = "CREATE TABLE " + TABLENAME + " (" 
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
            + COLUMN_EXTENSION + " TEXT ," 
            + COLUMN_TYPE + " TEXT NOT NULL," 
            + COLUMN_SUBTYPE + " TEXT NOT NULL," 
            + COLUMN_DESCRIPTION + " TEXT NOT NULL,"
            + COLUMN_SMALL_ICON + " TEXT," 
            + COLUMN_LARGE_ICON + " TEXT" + ");";

    public static void onCreate(Context context, SQLiteDatabase db)
    {
        create(db);
    }

    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion <= DatabaseVersionNumber.VERSION_1_4_0)
        {
            create(db);
        }
    }
    
    
    private static final String ICON_GENERIC = "R.drawable.mime_generic";
    private static final String ICON_GENERIC_LARGE = "R.drawable.mime_256_generic";
    private static final String ICON_VIDEO = "R.drawable.mime_video";
    private static final String ICON_VIDEO_LARGE = "R.drawable.mime_256_video";
    private static final String ICON_ZIP = "R.drawable.mime_zip";
    private static final String ICON_ZIP_LARGE = "R.drawable.mime_256_zip";
    private static final String ICON_IMG = "R.drawable.mime_img";
    private static final String ICON_IMG_LARGE = "R.drawable.mime_256_img";
    private static final String ICON_AUDIO = "R.drawable.mime_audio";
    private static final String ICON_AUDIO_LARGE = "R.drawable.mime_256_audio";
    private static final String ICON_TXT = "R.drawable.mime_txt";
    private static final String ICON_TXT_LARGE = "R.drawable.mime_256_txt"; 
    private static final String ICON_XML = "R.drawable.mime_xml";
    private static final String ICON_XML_LARGE = "R.drawable.mime_256_xml"; 
    private static final String ICON_HTML = "R.drawable.mime_html";
    private static final String ICON_HTML_LARGE = "R.drawable.mime_256_html";
    private static final String ICON_PSD = "R.drawable.mime_psd";
    private static final String ICON_PSD_LARGE = "R.drawable.mime_256_psd"; 
    
    

    private static void create(SQLiteDatabase db){
        db.execSQL(QUERY_TABLE_CREATE);
        insert(db, "", MimeType.TYPE_APPLICATION, "octet-stream", "Binary Data", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "3fr", MimeType.TYPE_IMAGE, "x-raw-hasselblad", "Hasselblad RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "3g2", MimeType.TYPE_VIDEO, "3gpp2", "3G2 Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "3gp2", MimeType.TYPE_VIDEO, "3gpp2", "3G2 Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "3gp", MimeType.TYPE_VIDEO, "3gp", "3G Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "7z", MimeType.TYPE_APPLICATION, "x-7z-compressed", "7-Zip", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "acp", MimeType.TYPE_APPLICATION, "acp", "Alfresco Content Package", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "aep", MimeType.TYPE_APPLICATION, "vnd.adobe.aftereffects.project", "Adobe AfterEffects Project", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "aet", MimeType.TYPE_APPLICATION, "vnd.adobe.aftereffects.template", "Adobe AfterEffects Template", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "ai", MimeType.TYPE_APPLICATION, "illustrator", "Adobe Illustrator File", "R.drawable.mime_ai", "R.drawable.mime_256_ai");
        insert(db, "aif", MimeType.TYPE_AUDIO, "x-aiff", "AIFF Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "aifc", MimeType.TYPE_AUDIO, "x-aiff", "AIFF Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "aiff", MimeType.TYPE_AUDIO, "x-aiff", "AIFF Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "air", MimeType.TYPE_APPLICATION, "vnd.adobe.air-application-installer-package+zip", "Adobe AIR", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "apk", MimeType.TYPE_APPLICATION, "vnd.android.package-archive", "Android Package", "R.drawable.mime_apk", "R.drawable.mime_256_apk");
        insert(db, "arw", MimeType.TYPE_IMAGE, "x-raw-sony", "Sony RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "asf", MimeType.TYPE_VIDEO, "x-ms-asf", "MS ASF Streaming Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "asnd", MimeType.TYPE_AUDIO, "vnd.adobe.soundbooth", "Adobe Soundbooth", "R.drawable.mime_asnd", "R.drawable.mime_256_asnd");
        insert(db, "asr", MimeType.TYPE_VIDEO, "x-ms-asf", "MS ASF Streaming Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "asx", MimeType.TYPE_VIDEO, "x-ms-asf", "MS ASF Streaming Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "atom", MimeType.TYPE_APPLICATION, "atom+xml", "Atom Syndication Format", ICON_XML, ICON_XML_LARGE);
        insert(db, "au", MimeType.TYPE_AUDIO, "basic", "Basic Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "avi", MimeType.TYPE_VIDEO, "x-msvideo", "MS Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "avx", MimeType.TYPE_VIDEO, "x-rad-screenplay", "RAD Screen Display", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "azw", MimeType.TYPE_APPLICATION, "vnd.amazon.ebook", "Amazon Kindle eBook format", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "bas", MimeType.TYPE_TEXT, "plain", "Basic Source file", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "bat", MimeType.TYPE_TEXT, "plain", "Plain Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "bin", MimeType.TYPE_APPLICATION, "octet-stream", "Binary File (Octet Stream)", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "bmp", MimeType.TYPE_IMAGE, "bmp", "Bitmap Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "body", MimeType.TYPE_TEXT, "html", "HTML", ICON_HTML, ICON_HTML_LARGE);
        insert(db, "bz", MimeType.TYPE_APPLICATION, "x-bzip", "Bzip Archive", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "bz2", MimeType.TYPE_APPLICATION, "x-bzip2", "Bzip2 Archive", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "c", MimeType.TYPE_TEXT, "plain", "C Source File", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "cab", MimeType.TYPE_APPLICATION, "vnd.ms-cab-compressed", "Microsoft Cabinet File", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "cat", MimeType.TYPE_APPLICATION, "vnd.ms-pkiseccat", "Microsoft Security Catalog", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "cer", MimeType.TYPE_APPLICATION, "x-x509-ca-cert", "Internet Public Key Infrastructure - Certificate", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "cgm", MimeType.TYPE_IMAGE, "cgm", "CGM  Image", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "class", MimeType.TYPE_APPLICATION, "java", "Java Class", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "clp", MimeType.TYPE_APPLICATION, "x-msclip", "Microsoft Clipboard Clip", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "cmx", MimeType.TYPE_IMAGE, "x-cmx", "Corel Metafile Exchange ", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "cr2", MimeType.TYPE_IMAGE, "x-raw-canon", "Canon RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "crw", MimeType.TYPE_IMAGE, "x-raw-canon", "Canon RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "csh", MimeType.TYPE_APPLICATION, "x-csh", "C Shell Script", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "css", MimeType.TYPE_TEXT, "css", "Style Sheet", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "csv", MimeType.TYPE_TEXT, "csv", "Comma Separated Values (CSV)", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "dcs", MimeType.TYPE_IMAGE, "x-raw-kodak", "Kodak RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "deb", MimeType.TYPE_APPLICATION, "x-debian-package", "Debian Package", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "dita", MimeType.TYPE_APPLICATION, "dita+xml", "DITA", ICON_XML, ICON_XML_LARGE);
        insert(db, "ditaval", MimeType.TYPE_APPLICATION, "dita+xml", "DITA", ICON_XML, ICON_XML_LARGE);
        insert(db, "ditamap", MimeType.TYPE_APPLICATION, "dita+xml", "DITA", ICON_XML, ICON_XML_LARGE);
        insert(db, "dng", MimeType.TYPE_IMAGE, "x-raw-adobe", "Adobe Digital Negative Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "doc", MimeType.TYPE_APPLICATION, "msword", "Microsoft Word", "R.drawable.mime_doc", "R.drawable.mime_256_doc");
        insert(db, "doct", MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.wordprocessingml.template", "Microsoft Word 2007 Template", "R.drawable.mime_doc", "R.drawable.mime_256_doc");
        insert(db, "docm", MimeType.TYPE_APPLICATION, "vnd.ms-word.document.macroenabled.12", "Microsoft Word 2007 macro-enabled document", "R.drawable.mime_doc", "R.drawable.mime_256_doc");
        insert(db, "docx", MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.wordprocessingml.document", "Microsoft Word 2007", "R.drawable.mime_doc", "R.drawable.mime_256_doc");
        insert(db, "dot", MimeType.TYPE_APPLICATION, "msword", "Microsoft Word", "R.drawable.mime_doc", "R.drawable.mime_256_doc");
        insert(db, "dotm", MimeType.TYPE_APPLICATION, "vnd.ms-word.template.macroenabled.12", "Microsoft Word 2007 macro-enabled document template", "R.drawable.mime_doc", "R.drawable.mime_256_doc");
        insert(db, "dotx", MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.wordprocessingml.template", "Microsoft Word 2007 template", "R.drawable.mime_doc", "R.drawable.mime_256_doc");
        insert(db, "drf", MimeType.TYPE_IMAGE, "x-raw-kodak", "Kodak RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "dtd", MimeType.TYPE_TEXT, "xml", "XML", ICON_XML, ICON_XML_LARGE);
        insert(db, "dvi", MimeType.TYPE_APPLICATION, "x-dvi", "Device Independent File Format", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "dwt", MimeType.TYPE_IMAGE, "x-dwt", "AutoCAD Template", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "dwg", MimeType.TYPE_IMAGE, "vnd.dwg", "AutoCAD Drawing", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "ear", MimeType.TYPE_APPLICATION, "zip", "Zip", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "eml", MimeType.TYPE_MESSAGE, "rfc822", "Email", "R.drawable.mime_eml", "R.drawable.mime_256_eml");
        insert(db, "eps", MimeType.TYPE_APPLICATION, "eps", "EPS Type PostScript", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "epub", MimeType.TYPE_APPLICATION, "epub+zip", "Electronic Publication", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "etx", MimeType.TYPE_TEXT, "x-setext", "Setext", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "exe", MimeType.TYPE_APPLICATION, "octet-stream", "Binary File (Octet Stream)", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "f4v", MimeType.TYPE_VIDEO, "x-f4v", "Flash Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "fla", MimeType.TYPE_APPLICATION, "x-fla", "Flash Source", "R.drawable.mime_fla", "R.drawable.mime_256_fla");
        insert(db, "flac", MimeType.TYPE_AUDIO, "x-flac", "FLAC Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "fli", MimeType.TYPE_VIDEO, "x-fli", "Animation Format", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "flv", MimeType.TYPE_VIDEO, "x-flv", "Flash Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "fm", MimeType.TYPE_APPLICATION, "vnd.framemaker", "Adobe FrameMaker", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "ftl", MimeType.TYPE_TEXT, "plain", "Plain Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "fxp", MimeType.TYPE_APPLICATION, "x-zip", "Adobe Flex Project File", "R.drawable.mime_fxp", "R.drawable.mime_256_fxp");
        insert(db, "gif", MimeType.TYPE_IMAGE, "gif", "GIF Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "gml", MimeType.TYPE_APPLICATION, "sgml", "SGML (Machine Readable)", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "gtar", MimeType.TYPE_APPLICATION, "x-gtar", "GZIP Tarball", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "gz", MimeType.TYPE_APPLICATION, "x-gzip", "GZIP", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "gzip", MimeType.TYPE_APPLICATION, "x-gzip", "GZIP", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "h", MimeType.TYPE_TEXT, "plain", "", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "h261", MimeType.TYPE_VIDEO, "h261", "H.261", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "h263", MimeType.TYPE_VIDEO, "h263", "H.263", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "h264", MimeType.TYPE_VIDEO, "h264", "H.264", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "h264", MimeType.TYPE_VIDEO, "h264", "H.264", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "hdf", MimeType.TYPE_APPLICATION, "x-hdf", "Hierarchical Data Format", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "hlp", MimeType.TYPE_APPLICATION, "winhlp", "WinHelp", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "hqx", MimeType.TYPE_APPLICATION, "mac-binhex40", "Macintosh BinHex 4.0", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "htm", MimeType.TYPE_TEXT, "html", "HTML", ICON_HTML, ICON_HTML_LARGE);
        insert(db, "html", MimeType.TYPE_TEXT, "html", "HTML", ICON_HTML, ICON_HTML_LARGE);
        insert(db, "htt", MimeType.TYPE_TEXT, "webviewhtml", "HTML", ICON_HTML, ICON_HTML_LARGE);
        insert(db, "ico", MimeType.TYPE_IMAGE, "x-icon", "Icon Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "ics", MimeType.TYPE_APPLICATION, "calendar", "iCalendar File", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "ief", MimeType.TYPE_IMAGE, "ief", "IEF Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "ind", MimeType.TYPE_APPLICATION, "x-indesign", "Adobe InDesign Document", "R.drawable.mime_indd", "R.drawable.mime_256_indd");
        insert(db, "indd", MimeType.TYPE_APPLICATION, "x-indesign", "Adobe InDesign Document", "R.drawable.mime_indd", "R.drawable.mime_256_indd");
        insert(db, "ini", MimeType.TYPE_TEXT, "plain", "Plain Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "j2k", MimeType.TYPE_IMAGE, "jp2", "JPEG 2000 Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "jar", MimeType.TYPE_APPLICATION, "zip", "Zip", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "java", MimeType.TYPE_TEXT, "plain", "Plain Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "jp2", MimeType.TYPE_IMAGE, "jp2", "JPEG 2000 Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "jpc", MimeType.TYPE_IMAGE, "jp2", "JPEG 2000 Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "jpf", MimeType.TYPE_IMAGE, "jp2", "JPEG 2000 Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "jpe", MimeType.TYPE_IMAGE, "jpeg", "JPEG Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "jpeg", MimeType.TYPE_IMAGE, "jpeg", "JPEG Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "jpg", MimeType.TYPE_IMAGE, "jpeg", "JPEG Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "jpm", MimeType.TYPE_IMAGE, "jp2", "JPEG 2000 Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "jpx", MimeType.TYPE_IMAGE, "jp2", "JPEG 2000 Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "js", MimeType.TYPE_TEXT, "x-javascript", "Java Script", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "json", MimeType.TYPE_APPLICATION, "json", "JSON", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "jsp", MimeType.TYPE_TEXT, "plain", "Plain Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "k25", MimeType.TYPE_IMAGE, "x-raw-kodak", "Kodak RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "kdc", MimeType.TYPE_IMAGE, "x-raw-kodak", "Kodak RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "key", MimeType.TYPE_APPLICATION, "vnd.apple.keynote", "Apple iWork Keynote", "R.drawable.mime_keynote", "R.drawable.mime_256_keynote");
        insert(db, "kml", MimeType.TYPE_APPLICATION, "vnd.google-earth.kml+xml", "Google Earth KML", ICON_XML, ICON_XML_LARGE);
        insert(db, "kmz", MimeType.TYPE_APPLICATION, "vnd.google-earth.kmz", "Google Earth Zipped KML", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "latex", MimeType.TYPE_APPLICATION, "x-latex", "LaTeX", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "log", MimeType.TYPE_TEXT, "plain", "Plain Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "m1v", MimeType.TYPE_VIDEO, "mpeg", "MPEG Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "m2ts", MimeType.TYPE_VIDEO, "mp2t", "MPEG Transport Stream", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "m2v", MimeType.TYPE_VIDEO, "mpeg", "MPEG Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "m3u", MimeType.TYPE_AUDIO, "x-mpegurl", "Multimedia Playlist", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "m4a", MimeType.TYPE_AUDIO, "mp4", "MPEG4 Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "m4b", MimeType.TYPE_AUDIO, "mp4", "MPEG4 Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "mp4a", MimeType.TYPE_AUDIO, "mp4", "MPEG4 Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "m4v", MimeType.TYPE_VIDEO, "x-m4v", "MPEG4 Video (m4v)", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "man", MimeType.TYPE_APPLICATION, "x-troff-man", "Man Page", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "md", MimeType.TYPE_TEXT, "x-markdown", "Markdown", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "mdb", MimeType.TYPE_APPLICATION, "x-msaccess", "Microsoft Access", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "mid", MimeType.TYPE_AUDIO, "mid", "Musical Instrument Digital Interface", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "mov", MimeType.TYPE_VIDEO, "quicktime", "Quicktime Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "movie", MimeType.TYPE_VIDEO, "x-sgi-movie", "SGI Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mp2", MimeType.TYPE_AUDIO, "mpeg", "MPEG Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "mp3", MimeType.TYPE_AUDIO, "mpeg", "MPEG Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "mp4", MimeType.TYPE_VIDEO, "mp4", "MPEG4 Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mp4v", MimeType.TYPE_VIDEO, "mp4", "MPEG4 Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mpe", MimeType.TYPE_VIDEO, "mpeg", "MPEG Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mpeg", MimeType.TYPE_VIDEO, "mpeg", "MPEG Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mpeg2", MimeType.TYPE_VIDEO, "mpeg2", "MPEG2 Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mpg", MimeType.TYPE_VIDEO, "mpeg", "MPEG Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mpg4", MimeType.TYPE_VIDEO, "mp4", "MPEG4 Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mpp", MimeType.TYPE_APPLICATION, "vnd.ms-project", "Microsoft Project", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "mpv2", MimeType.TYPE_VIDEO, "x-sgi-movie", "SGI Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mrw", MimeType.TYPE_IMAGE, "x-raw-minolta", "Minolta RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "msg", MimeType.TYPE_APPLICATION, "vnd.ms-outlook", "Microsoft Outlook Message", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "mv", MimeType.TYPE_VIDEO, "x-sgi-movie", "SGI Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "mvb", MimeType.TYPE_APPLICATION, "x-msmediaview", "Microsoft MediaView", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "mw", MimeType.TYPE_TEXT, "mediawiki", "MediaWiki Markup", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "numbers", MimeType.TYPE_APPLICATION, "vnd.apple.numbers", "Apple iWork Numbers", "R.drawable.mime_numbers", "R.drawable.mime_256_numbers");
        insert(db, "nef", MimeType.TYPE_IMAGE, "x-raw-nikon", "Nikon RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "nrw", MimeType.TYPE_IMAGE, "x-raw-nikon", "Nikon RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "nsf", MimeType.TYPE_APPLICATION, "vnd.lotus-notes", "Lotus Notes", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "oda", MimeType.TYPE_APPLICATION, "oda", "Office Document Architecture", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "odb", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.database", "OpenDocument Database", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "odc", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.chart", "OpenDocument Chart", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "odf", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.formula", "OpenDocument Formula", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "odft", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.formula-template", "OpenDocument Formula Template", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "odg", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.graphics", "OpenDocument Drawing", "R.drawable.mime_odg", "R.drawable.mime_256_odg");
        insert(db, "odi", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.image", "OpenDocument Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "odm", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.text-master", "OpenDocument Master Document", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "odp", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.presentation", "OpenDocument Presentation", "R.drawable.mime_odp", "R.drawable.mime_256_odp");
        insert(db, "ods", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.spreadsheet", "OpenDocument Spreadsheet", "R.drawable.mime_ods", "R.drawable.mime_256_ods");
        insert(db, "odt", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.text", "OpenDocument Text", "R.drawable.mime_odt", "R.drawable.mime_256_odt");
        insert(db, "oga", MimeType.TYPE_AUDIO, "ogg", "Ogg Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "ogg", MimeType.TYPE_AUDIO, "ogg", "Ogg Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "ogv", MimeType.TYPE_VIDEO, "ogg", "Ogg Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "ogx", MimeType.TYPE_APPLICATION, "ogg", "Ogg Multiplex", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "onetoc", MimeType.TYPE_APPLICATION, "onenote", "Microsoft OneNote", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "onetoc2", MimeType.TYPE_APPLICATION, "onenote", "Microsoft OneNote", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "onetmp", MimeType.TYPE_APPLICATION, "onenote", "Microsoft OneNote", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "onepkg", MimeType.TYPE_APPLICATION, "onenote", "Microsoft OneNote", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "orf", MimeType.TYPE_IMAGE, "x-raw-olympus", "Olympus  RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "otc", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.chart-template", "OpenDocument Chart Template", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "otg", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.graphics-template", "OpenDocument Drawing Template", "R.drawable.mime_odg", "R.drawable.mime_256_odg");
        insert(db, "oth", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.text-web", "OpenDocument HTML", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "oti", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.image-template", "OpenDocument Image Template", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "otp", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.presentation-template", "OpenDocument Presentation Template", "R.drawable.mime_odp", "R.drawable.mime_256_odp");
        insert(db, "ots", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.spreadsheet-template", "OpenDocument Spreadsheet Template", "R.drawable.mime_ods", "R.drawable.mime_256_ods");
        insert(db, "ott", MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.text-template", "OpenDocument Text Template", "R.drawable.mime_odt", "R.drawable.mime_256_odt");
        insert(db, "p65", MimeType.TYPE_APPLICATION, "pagemaker", "Adobe PageMaker", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "pages", MimeType.TYPE_APPLICATION, "vnd.apple.pages", "Apple iWork Pages", "R.drawable.mime_pages", "R.drawable.mime_256_pages");
        insert(db, "pbm", MimeType.TYPE_IMAGE, "x-portable-bitmap", "Portable Bitmap", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "pdf", MimeType.TYPE_APPLICATION, "pdf", "Adobe PDF Document", "R.drawable.mime_pdf", "R.drawable.mime_256_pdf");
        insert(db, "pef", MimeType.TYPE_IMAGE, "x-raw-pentax", "Pentax RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "pgm", MimeType.TYPE_IMAGE, "x-portable-graymap", "Greymap Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "pm", MimeType.TYPE_APPLICATION, "pagemaker", "Adobe PageMaker", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "pm6", MimeType.TYPE_APPLICATION, "pagemaker", "Adobe PageMaker", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "pmd", MimeType.TYPE_APPLICATION, "pagemaker", "Adobe PageMaker", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "png", MimeType.TYPE_IMAGE, "png", "PNG Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "pnm", MimeType.TYPE_IMAGE, "x-portable-anymap", "Anymap Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "pot", MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint", "Micosoft PowerPoint", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "potm", MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint.template.macroenabled.12", "Microsoft PowerPoint 2007 macro-enabled presentation template", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "potx", MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.presentationml.template", "Microsoft PowerPoint 2007 template", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "ppa", MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint", "Microsoft PowerPoint", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "ppam", MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint.addin.macroenabled.12", "Microsoft PowerPoint 2007 add-in", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "ppj", MimeType.TYPE_IMAGE, "vnd.adobe.premiere", "Adobe Premiere", "R.drawable.mime_ppj", "R.drawable.mime_256_ppj");
        insert(db, "ppm", MimeType.TYPE_IMAGE, "x-portable-pixmap", "Pixmap Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "pps", MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint", "Micosoft PowerPoint", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "ppsm", MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint.slideshow.macroenabled.12", "Microsoft PowerPoint 2007 macro-enabled slide show", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "ppt", MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint", "Microsoft PowerPoint", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "pptm", MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint.presentation.macroenabled.12", "Microsoft PowerPoint 2007 macro-enabled presentation", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "pptx", MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.presentationml.presentation", "Microsoft PowerPoint 2007 Presentation", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "ppsx", MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.presentationml.slideshow", "Microsoft PowerPoint 2007 slide show", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "prf", MimeType.TYPE_APPLICATION, "pics-rules", "PICSRules", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "prn", MimeType.TYPE_APPLICATION, "remote-printing", "Printer Text File", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "properties", MimeType.TYPE_TEXT, "plain", "Plain Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "prproj", MimeType.TYPE_IMAGE, "vnd.adobe.premiere", "Adobe Premiere", "R.drawable.mime_ppj", "R.drawable.mime_256_ppj");
        insert(db, "ps", MimeType.TYPE_APPLICATION, "postscript", "PostScript", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "psd", MimeType.TYPE_IMAGE, "vnd.adobe.photoshop", "Adobe Photoshop", ICON_PSD , ICON_PSD_LARGE);
        insert(db, "ptx", MimeType.TYPE_IMAGE, "x-raw-pentax", "Pentax RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "pub", MimeType.TYPE_APPLICATION, "x-mspublisher", "Microsoft Publisher", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "qt", MimeType.TYPE_APPLICATION, "quicktime", "Quicktime Video", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "qvi", MimeType.TYPE_VIDEO, "x-msvideo", "MS Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "r3d", MimeType.TYPE_IMAGE, "x-raw-red", "RED RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "ra", MimeType.TYPE_AUDIO, "x-pn-realaudio", "Real Audio Sound", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "raf", MimeType.TYPE_IMAGE, "x-raw-fuji", "Fuji RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "ram", MimeType.TYPE_AUDIO, "x-pn-realaudio", "Real Audio Sound", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "rar", MimeType.TYPE_APPLICATION, "x-rar-compressed", "RAR Archive", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "ras", MimeType.TYPE_IMAGE, "x-cmu-raster", "Raster Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "rgb", MimeType.TYPE_IMAGE, "x-rgb", "RGB Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "rpnm", MimeType.TYPE_IMAGE, "x-portable-anymap", "Anymap Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "rss", MimeType.TYPE_APPLICATION, "rss+xml", "RSS", ICON_XML, ICON_XML_LARGE);
        insert(db, "rtf", MimeType.TYPE_APPLICATION, "rtf", "Rich Text Format", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "rtx", MimeType.TYPE_TEXT, "richtext", "Rich Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "rw2", MimeType.TYPE_IMAGE, "x-raw-panasonic", "Panasonic  RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "rwl", MimeType.TYPE_IMAGE, "x-raw-leica", "Leica RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "scd", MimeType.TYPE_APPLICATION, "x-msschedule", "Microsoft Schedule+", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sda", MimeType.TYPE_APPLICATION, "vnd.stardivision.draw", "StarDraw 5.x", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "ser", MimeType.TYPE_APPLICATION, "java-serialized-object", "Java Serialized Object", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "scd", MimeType.TYPE_APPLICATION, "x-msschedule", "Microsoft Schedule+", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sds", MimeType.TYPE_APPLICATION, "vnd.stardivision.chart", "StaChart 5.x", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sdc", MimeType.TYPE_APPLICATION, "vnd.stardivision.calc", "StarCalc 5.x", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sdp", MimeType.TYPE_APPLICATION, "vnd.stardivision.impress-packed", "StarImpress Packed 5.x", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sdd", MimeType.TYPE_APPLICATION, "vnd.stardivision.impress", "StarImpress 5.x", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sdw", MimeType.TYPE_APPLICATION, "vnd.stardivision.writer", "StarWriter 5.x", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sgl", MimeType.TYPE_APPLICATION, "vnd.stardivision.writer-global", "StarWriter 5.x global", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sgm", MimeType.TYPE_TEXT, "sgml", "SGML (Human Readable)", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sgml", MimeType.TYPE_TEXT, "sgml", "SGML (Human Readable)", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sh", MimeType.TYPE_APPLICATION, "x-sh", "Shell Script", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "shtml", MimeType.TYPE_TEXT, "html", "HTML", ICON_HTML, ICON_HTML_LARGE);
        insert(db, "sldm", MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint.slide.macroenabled.12", "Microsoft PowerPoint 2007 macro-enabled slide", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "sldx", MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.presentationml.slide", "Microsoft PowerPoint 2007 slide", "R.drawable.mime_ppt", "R.drawable.mime_256_ppt");
        insert(db, "smf", MimeType.TYPE_APPLICATION, "vnd.stardivision.math", "StarMath 5.x", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "snd", MimeType.TYPE_AUDIO, "basic", "Basic Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "spd", MimeType.TYPE_APPLICATION, "samsung_note", "Samsung SNote", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "spx", MimeType.TYPE_AUDIO, "ogg", "Ogg Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "sql", MimeType.TYPE_TEXT, "plain", "Plain Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "sr2", MimeType.TYPE_IMAGE, "x-raw-sony", "Sony RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "srf", MimeType.TYPE_IMAGE, "x-raw-sony", "Sony RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "stc", MimeType.TYPE_APPLICATION, "vnd.sun.xml.calc.template", "OpenOffice 1.0/StarOffice6.0 Calc 6.0 Template", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sti", MimeType.TYPE_APPLICATION, "vnd.sun.xml.impress.template", "OpenOffice 1.0/StarOffice6.0 Impress 6.0 Template", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "stw", MimeType.TYPE_APPLICATION, "vnd.sun.xml.writer.template", "OpenOffice 1.0/StarOffice6.0 Writer 6.0 Template", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "svg", MimeType.TYPE_IMAGE, "svg+xml", "Scalable Vector Graphics Image", ICON_XML, ICON_XML_LARGE);
        insert(db, "swf", MimeType.TYPE_APPLICATION, "x-shockwave-flash", "Shockwave Flash", "R.drawable.mime_swf", "R.drawable.mime_256_swf");
        insert(db, "sxc", MimeType.TYPE_APPLICATION, "vnd.sun.xml.calc", "OpenOffice 1.0/StarOffice6.0 Calc 6.0", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sxd", MimeType.TYPE_APPLICATION, "vnd.sun.xml.draw", "OpenOffice 1.0/StarOffice6.0 Draw 6.0", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sxi", MimeType.TYPE_APPLICATION, "vnd.sun.xml.impress", "OpenOffice 1.0/StarOffice6.0 Impress 6.0", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "sxw", MimeType.TYPE_APPLICATION, "vnd.sun.xml.writer", "OpenOffice 1.0/StarOffice6.0 Writer 6.0", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "t", MimeType.TYPE_APPLICATION, "x-troff", "troff", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "tar", MimeType.TYPE_APPLICATION, "x-tar", "Tarball", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "tcl", MimeType.TYPE_APPLICATION, "x-tcl", "Tcl Script", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "tex", MimeType.TYPE_APPLICATION, "x-tex", "Tex", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "texinfo", MimeType.TYPE_APPLICATION, "x-texinfo", "Tex Info", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "texi", MimeType.TYPE_APPLICATION, "x-texinfo", "Tex Info", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "tgz", MimeType.TYPE_APPLICATION, "x-compressed", "TGZ", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "tif", MimeType.TYPE_IMAGE, "tiff", "TIFF Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "tiff", MimeType.TYPE_IMAGE, "tiff", "TIFF Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "ts", MimeType.TYPE_VIDEO, "mp2t", "MPEG Transport Stream", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "tsv", MimeType.TYPE_TEXT, "tab-separated-values", "Tab Seperated Values", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "txt", MimeType.TYPE_TEXT, "plain", "Plain Text", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "vcf", MimeType.TYPE_TEXT, "x-vcard", "vCard", ICON_TXT, ICON_TXT_LARGE);
        insert(db, "vsd", MimeType.TYPE_APPLICATION, "vnd.visio", "Microsoft Visio", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "war", MimeType.TYPE_APPLICATION, "zip", "Zip", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "wav", MimeType.TYPE_AUDIO, "x-wav", "WAV Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "weba", MimeType.TYPE_AUDIO, "webm", "Open Web Media Project - Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "webm", MimeType.TYPE_VIDEO, "webm", "WebM Video", ICON_VIDEO, ICON_VIDEO_LARGE);
        insert(db, "webp", MimeType.TYPE_IMAGE, "webp", "WebP Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "wma", MimeType.TYPE_AUDIO, "x-ms-wma", "MS WMA Streaming Audio", ICON_AUDIO, ICON_AUDIO_LARGE);
        insert(db, "wmf", MimeType.TYPE_APPLICATION, "x-msmetafile", "Microsoft Windows Metafile", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "wmv", MimeType.TYPE_VIDEO, "x-ms-wmv", "MS WMV Streaming Video", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "wpd", MimeType.TYPE_APPLICATION, "wordperfect", "WordPerfect", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "wps", MimeType.TYPE_APPLICATION, "vnd.ms-works", "Microsoft Works", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "wri", MimeType.TYPE_APPLICATION, "x-mswrite", "Microsoft Wordpad", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "wrl", MimeType.TYPE_MODEL, "x-vrml", "VRML", ICON_GENERIC, ICON_GENERIC_LARGE);
        insert(db, "wsdl", MimeType.TYPE_APPLICATION, "wsdl+xml", "Web Services Description Language", ICON_XML, ICON_XML_LARGE);
        insert(db, "x3f", MimeType.TYPE_IMAGE, "x-raw-sigma", "Sigma RAW Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "xbm", MimeType.TYPE_IMAGE, "x-xbitmap", "XBitMap Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "xdp", MimeType.TYPE_APPLICATION, "vnd.adobe.xdp+xml", "Adobe Acrobat XML Data Package", ICON_XML, ICON_XML_LARGE);
        insert(db, "xhtml", MimeType.TYPE_APPLICATION, "xhtml+xml", "XHTML", ICON_XML, ICON_XML_LARGE);
        insert(db, "xla", MimeType.TYPE_APPLICATION, "vnd.ms-excel", "Microsoft Excel", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xlam", MimeType.TYPE_APPLICATION, "vnd.ms-excel.addin.macroenabled.12", "Microsoft Excel  2007 add-in", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xlc", MimeType.TYPE_APPLICATION, "vnd.ms-excel", "Microsoft Excel", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xlm", MimeType.TYPE_APPLICATION, "vnd.ms-excel", "Microsoft Excel", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xls", MimeType.TYPE_APPLICATION, "vnd.ms-excel", "Microsoft Excel", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xlsb", MimeType.TYPE_APPLICATION, "vnd.ms-excel.sheet.binary.macroenabled.12", "Microsoft Excel 2007 binary workbook", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xlsm", MimeType.TYPE_APPLICATION, "vnd.ms-excel.sheet.macroenabled.12", "Microsoft Excel 2007 macro-enabled workbook", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xlsx", MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Microsoft Excel 2007", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xlt", MimeType.TYPE_APPLICATION, "vnd.ms-excel", "Microsoft Excel", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xltm", MimeType.TYPE_APPLICATION, "vnd.ms-excel.template.macroenabled.12", "Microsoft Excel 2007 macro-enabled workbook template", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xltx", MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.spreadsheetml.template", "Microsoft Excel template 2007", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xlw", MimeType.TYPE_APPLICATION, "vnd.ms-excel", "Microsoft Excel", "R.drawable.mime_xls", "R.drawable.mime_256_xls");
        insert(db, "xpm", MimeType.TYPE_IMAGE, "x-xpixmap", "XPixMap Image", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "xml", MimeType.TYPE_TEXT, "xml", "XML", ICON_XML, ICON_XML_LARGE);
        insert(db, "xsd", MimeType.TYPE_TEXT, "xml", "XML", ICON_XML, ICON_XML_LARGE);
        insert(db, "xsl", MimeType.TYPE_TEXT, "xml", "XML", ICON_XML, ICON_XML_LARGE);
        insert(db, "xslt", MimeType.TYPE_TEXT, "xml", "XML", ICON_XML, ICON_XML_LARGE);
        insert(db, "xwd", MimeType.TYPE_IMAGE, "x-xwindowdump", "XWindow Dump", ICON_IMG, ICON_IMG_LARGE);
        insert(db, "z", MimeType.TYPE_APPLICATION, "x-compress", "Z Compress", ICON_ZIP, ICON_ZIP_LARGE);
        insert(db, "zip", MimeType.TYPE_APPLICATION, "zip", "Zip Archive", ICON_ZIP, ICON_ZIP_LARGE);
    }

    public static long insert(SQLiteDatabase db, String extension, String type, String subtype, String description,
            String smallIcon, String largeIcon)
    {
        ContentValues insertValues = MimeTypeManager.createContentValues(extension, type, subtype, description,
                smallIcon, largeIcon);
        return db.insert(MimeTypeSchema.TABLENAME, null, insertValues);
    }

    
    // ////////////////////////////////////////////////////
    // DEBUG
    // ////////////////////////////////////////////////////
    private static final String QUERY_TABLE_DROP = "DROP TABLE IF EXISTS " + TABLENAME;

    // TODO REMOVE BEFORE RELEASE
    public static void reset(SQLiteDatabase db)
    {
        db.execSQL(QUERY_TABLE_DROP);
        create(db);
    }
    
}
