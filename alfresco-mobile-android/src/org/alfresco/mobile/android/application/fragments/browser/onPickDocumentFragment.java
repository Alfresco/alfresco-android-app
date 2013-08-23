package org.alfresco.mobile.android.application.fragments.browser;

import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Document;

public interface onPickDocumentFragment
{
    void onSelectDocument(List<Document> p);

    Map<String, Document> retrieveDocumentSelection();
}
