package org.alfresco.mobile.android.application.fragments.search;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.mimetype.MimeType;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.text.TextUtils;
import android.util.Log;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class QueryHelper
{

    private static final String QUERY_DOCUMENT_TITLED = "SELECT d.* FROM cmis:document as d JOIN cm:titled as t ON d.cmis:objectId = t.cmis:objectId WHERE ";

    private static final String QUERY_DOCUMENT = "SELECT * FROM cmis:document WHERE ";

    private static final String CMIS_PROP_TITLE = " cm:title ";

    private static final String CMIS_PROP_DESCRIPTION = " cm:description ";

    private static final String OPERATOR_EQUAL = "=";

    private static final String OPERATOR_SUPERIOR = ">";

    private static final String OPERATOR_INFERIOR = "<";

    // ///////////////////////////////////////////////////////////////////////////
    // QUERY
    // ///////////////////////////////////////////////////////////////////////////
    public static String createQuery(String name, String title, String description, String modifiedById,
            GregorianCalendar modificationFrom, GregorianCalendar modificationTo)
    {
        return createQuery(name, title, description, -1, modifiedById, modificationFrom, modificationTo, null);
    }

    public static String createQuery(String name, String title, String description, int mimetype, String modifiedById,
            GregorianCalendar modificationFrom, GregorianCalendar modificationTo, Folder parentFolder)
    {
        // Detect if cm:titled is applied
        StringBuilder queryBuilder;
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(description))
        {
            queryBuilder = new StringBuilder(QUERY_DOCUMENT);
        }
        else
        {
            queryBuilder = new StringBuilder(QUERY_DOCUMENT_TITLED);
        }

        // Create the query based on properties
        StringBuilder whereClause = new StringBuilder();
        
        // Name
        addParenFolderParameter(whereClause, parentFolder);

        // Name
        addContainsParameter(whereClause, PropertyIds.NAME, name);

        // Title
        addAspectContainsParameter(whereClause, CMIS_PROP_TITLE, title);

        // Description
        addAspectContainsParameter(whereClause, CMIS_PROP_DESCRIPTION, description);

        // Mimetype
        addMimeTypeParameter(whereClause, mimetype);

        // ModifiedBy
        addParameter(whereClause, PropertyIds.LAST_MODIFIED_BY, OPERATOR_EQUAL, modifiedById);

        // Modified FROM
        if (modificationFrom != null)
        {
            GregorianCalendar localModificationFrom = (GregorianCalendar) modificationFrom.clone();
            localModificationFrom.add(Calendar.DAY_OF_MONTH, -1);
            addDateParameter(whereClause, PropertyIds.LAST_MODIFICATION_DATE, OPERATOR_SUPERIOR,
                    formatLast(localModificationFrom));
        }

        // Modified TO
        if (modificationTo != null)
        {
            GregorianCalendar localModificationTo = (GregorianCalendar) modificationTo.clone();
            localModificationTo.add(Calendar.DAY_OF_MONTH, 1);
            addDateParameter(whereClause, PropertyIds.LAST_MODIFICATION_DATE, OPERATOR_INFERIOR,
                    formatFirst(localModificationTo));
        }

        queryBuilder.append(whereClause);

        Log.d("Query", queryBuilder.toString());

        return queryBuilder.toString();
    }
    
    public static String createPersonSearchQuery(String name, String jobTitle, String company, String location)
    {
        StringBuilder queryBuilder = new StringBuilder(name);
        addPersonParameter(queryBuilder, "jobtitle", jobTitle);
        addPersonParameter(queryBuilder, "organization", company);
        addPersonParameter(queryBuilder, "location", location);
        return queryBuilder.toString();
    }

    
    // ///////////////////////////////////////////////////////////////////////////
    // HELPER
    // ///////////////////////////////////////////////////////////////////////////
    public static String formatFirst(GregorianCalendar calendar)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime()).concat("T00:00:00.000Z");
    }
    public static String formatLast(GregorianCalendar calendar)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime()).concat("T23:59:59.999Z");
    }
    
    private static void addParenFolderParameter(StringBuilder builder, Folder value)
    {
        if (value == null) { return; }
        if (builder.length() != 0)
        {
            builder.append(" AND ");
        }
        builder.append(" IN_TREE('" + value.getIdentifier() + "')");
    }

    private static void addParameter(StringBuilder builder, String key, String operator, String value)
    {
        if (TextUtils.isEmpty(value)) { return; }
        if (builder.length() != 0)
        {
            builder.append(" AND ");
        }
        builder.append(key);
        builder.append(operator);
        builder.append("'" + value + "'");
    }

    private static void addContainsParameter(StringBuilder builder, String key, String value)
    {
        if (TextUtils.isEmpty(value)) { return; }
        if (builder.length() != 0)
        {
            builder.append(" AND ");
        }
        builder.append("CONTAINS('~" + key + ":\\\'" + value + "\\\'')");
    }

    private static void addDateParameter(StringBuilder builder, String key, String operator, String value)
    {
        if (TextUtils.isEmpty(value)) { return; }
        if (builder.length() != 0)
        {
            builder.append(" AND ");
        }
        builder.append(key);
        builder.append(" ");
        builder.append(operator);
        builder.append(" TIMESTAMP ");
        builder.append("'" + value + "'");
    }

    private static void addAspectContainsParameter(StringBuilder builder, String key, String value)
    {
        if (TextUtils.isEmpty(value)) { return; }
        if (builder.length() != 0)
        {
            builder.append(" AND ");
        }
        builder.append("CONTAINS(t, '~" + key + ":\\\'" + value + "\\\'')");
    }

    private static void addPersonParameter(StringBuilder builder, String key, String value)
    {
        if (TextUtils.isEmpty(value)) { return; }
        if (builder.length() != 0)
        {
            builder.append(" ");
        }
        builder.append(key);
        builder.append(":");
        builder.append(value);
    }

    private static void addMimeTypeParameter(StringBuilder builder, int mimetypeKey)
    {
        List<MimeType> types = null;
        switch (mimetypeKey)
        {
            case R.string.mimetype_unknown:
                return;
            case R.string.mimetype_documents:
                types = createDocumentsList();
                break;
            case R.string.mimetype_music:
                types = createAudioList();
                break;
            case R.string.mimetype_images:
                types = createImageList();
                break;
            case R.string.mimetype_presentations:
                types = createPresentationsList();
                break;
            case R.string.mimetype_spreadsheets:
                types = createSpreadsheetsList();
                break;
            case R.string.mimetype_text:
                types = createTextList();
                break;
            case R.string.mimetype_videos:
                types = createVideoList();
                break;

            default:
                break;
        }

        if (types == null) { return; }
        StringBuilder sb = new StringBuilder();
        for (MimeType mimeType : types)
        {
            if (sb.length() == 0)
            {
                sb.append("'");
                sb.append(mimeType.getMimeType());
                sb.append("'");
                continue;
            }
            sb.append(", ");
            sb.append("'");
            sb.append(mimeType.getMimeType());
            sb.append("'");
        }

        if (builder.length() != 0)
        {
            builder.append(" AND ");
        }
        builder.append(PropertyIds.CONTENT_STREAM_MIME_TYPE);
        builder.append(" IN (");
        builder.append(sb);
        builder.append(")");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MIMETYPE LIST
    // ///////////////////////////////////////////////////////////////////////////
    private static List<MimeType> createDocumentsList()
    {
        return new ArrayList<MimeType>()
        {
            private static final long serialVersionUID = 1L;

            {
                add(new MimeType(MimeType.TYPE_APPLICATION, "msword"));
                add(new MimeType(MimeType.TYPE_APPLICATION,
                        "vnd.openxmlformats-officedocument.wordprocessingml.template"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-word.document.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION,
                        "vnd.openxmlformats-officedocument.wordprocessingml.document"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "msword"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-word.template.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION,
                        "vnd.openxmlformats-officedocument.wordprocessingml.template"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "epub+zip"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.apple.pages"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "pdf"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.sun.xml.writer.template"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.sun.xml.writer"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "wordperfect"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "x-mswrite"));
            }
        };
    };

    private static List<MimeType> createAudioList()
    {
        return new ArrayList<MimeType>()
        {
            private static final long serialVersionUID = 1L;
            {
                add(new MimeType(MimeType.TYPE_AUDIO, "x-aiff"));
                add(new MimeType(MimeType.TYPE_AUDIO, "vnd.adobe.soundbooth"));
                add(new MimeType(MimeType.TYPE_AUDIO, "basic"));
                add(new MimeType(MimeType.TYPE_AUDIO, "x-flac"));
                add(new MimeType(MimeType.TYPE_AUDIO, "x-mpegurl"));
                add(new MimeType(MimeType.TYPE_AUDIO, "mp4"));
                add(new MimeType(MimeType.TYPE_AUDIO, "mid"));
                add(new MimeType(MimeType.TYPE_AUDIO, "mpeg"));
                add(new MimeType(MimeType.TYPE_AUDIO, "ogg"));
                add(new MimeType(MimeType.TYPE_AUDIO, "x-pn-realaudio"));
                add(new MimeType(MimeType.TYPE_AUDIO, "basic"));
                add(new MimeType(MimeType.TYPE_AUDIO, "x-wav"));
                add(new MimeType(MimeType.TYPE_AUDIO, "webm"));
                add(new MimeType(MimeType.TYPE_AUDIO, "x-ms-wma"));
            }
        };
    };

    private static List<MimeType> createImageList()
    {
        return new ArrayList<MimeType>()
        {
            private static final long serialVersionUID = 1L;

            {
                add(new MimeType(MimeType.TYPE_APPLICATION, "eps"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-hasselblad"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-sony"));
                add(new MimeType(MimeType.TYPE_IMAGE, "bmp"));
                add(new MimeType(MimeType.TYPE_IMAGE, "cgm"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-cmx"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-canon"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-kodak"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-adobe"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-dwt"));
                add(new MimeType(MimeType.TYPE_IMAGE, "vnd.dwg"));
                add(new MimeType(MimeType.TYPE_IMAGE, "gif"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-icon"));
                add(new MimeType(MimeType.TYPE_IMAGE, "ief"));
                add(new MimeType(MimeType.TYPE_IMAGE, "jp2"));
                add(new MimeType(MimeType.TYPE_IMAGE, "jpeg"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-minolta"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-nikon"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-olympus"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-portable-bitmap"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-pentax"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-portable-graymap"));
                add(new MimeType(MimeType.TYPE_IMAGE, "png"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-portable-anymap"));
                add(new MimeType(MimeType.TYPE_IMAGE, "vnd.adobe.premiere"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-portable-pixmap"));
                add(new MimeType(MimeType.TYPE_IMAGE, "vnd.adobe.photoshop"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-red"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-fuji"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-cmu-raster"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-rgb"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-portable-anymap"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-panasonic"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-leica"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-sony"));
                add(new MimeType(MimeType.TYPE_IMAGE, "svg+xml"));
                add(new MimeType(MimeType.TYPE_IMAGE, "tiff"));
                add(new MimeType(MimeType.TYPE_IMAGE, "webp"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-raw-sigma"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-xbitmap"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-xpixmap"));
                add(new MimeType(MimeType.TYPE_IMAGE, "x-xwindowdump"));
            }
        };
    };

    private static List<MimeType> createPresentationsList()
    {
        return new ArrayList<MimeType>()
        {
            private static final long serialVersionUID = 1L;

            {
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.apple.keynote"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.presentation"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.presentation-template"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint.template.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.presentationml.template"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint.slideshow.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint.presentation.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION,
                        "vnd.openxmlformats-officedocument.presentationml.presentation"));
                add(new MimeType(MimeType.TYPE_APPLICATION,
                        "vnd.openxmlformats-officedocument.presentationml.slideshow"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-powerpoint.slide.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.presentationml.slide"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.sun.xml.impress.template"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.sun.xml.impress"));
            }
        };
    };

    private static List<MimeType> createSpreadsheetsList()
    {
        return new ArrayList<MimeType>()
        {
            private static final long serialVersionUID = 1L;

            {
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.apple.numbers"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.spreadsheet"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.spreadsheet-template"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.sun.xml.calc.template"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.sun.xml.calc"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-excel.addin.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-excel"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-excel.sheet.binary.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-excel.sheet.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.ms-excel.template.macroenabled.12"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.openxmlformats-officedocument.spreadsheetml.template"));
            }
        };
    };

    private static List<MimeType> createTextList()
    {
        return new ArrayList<MimeType>()
        {
            private static final long serialVersionUID = 1L;

            {
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.text"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "vnd.oasis.opendocument.text-template"));
                add(new MimeType(MimeType.TYPE_APPLICATION, "rtf"));
                add(new MimeType(MimeType.TYPE_TEXT, "plain"));
                add(new MimeType(MimeType.TYPE_TEXT, "csv"));
                add(new MimeType(MimeType.TYPE_TEXT, "richtext"));
                add(new MimeType(MimeType.TYPE_TEXT, "tab-separated-values"));
            }
        };
    };

    private static List<MimeType> createVideoList()
    {
        return new ArrayList<MimeType>()
        {
            private static final long serialVersionUID = 1L;

            {
                add(new MimeType(MimeType.TYPE_APPLICATION, "quicktime"));
                add(new MimeType(MimeType.TYPE_VIDEO, "x-msvideo"));
                add(new MimeType(MimeType.TYPE_VIDEO, "h261"));
                add(new MimeType(MimeType.TYPE_VIDEO, "h263"));
                add(new MimeType(MimeType.TYPE_VIDEO, "h264"));
                add(new MimeType(MimeType.TYPE_VIDEO, "mpeg"));
                add(new MimeType(MimeType.TYPE_VIDEO, "x-m4v"));
                add(new MimeType(MimeType.TYPE_VIDEO, "quicktime"));
                add(new MimeType(MimeType.TYPE_VIDEO, "x-sgi-movie"));
                add(new MimeType(MimeType.TYPE_VIDEO, "mpeg2"));
                add(new MimeType(MimeType.TYPE_VIDEO, "mp4"));
            }
        };
    }
}
