package org.alfresco.mobile.android.ui.node.search;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;

/**
 * Created by jpascal on 11/08/2015.
 */
public interface SearchNodesTemplate
{

    String ARGUMENT_KEYWORDS = ConfigConstants.KEYWORDS_VALUE;

    String ARGUMENT_FULLTEXT = "fulltext";

    String ARGUMENT_EXACTMATCH = "exact";

    String ARGUMENT_PARENTFOLDER = "folder";

    String ARGUMENT_SEARCH_FOLDER = "searchFolderOnly";

    String ARGUMENT_INCLUDE_DESCENDANTS = "descendants";

    String ARGUMENT_STATEMENT = "statement";

    String AGUMENT_LANGUAGE = "language";
}
