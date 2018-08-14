package org.alfresco.mobile.android.application.fragments.node.download;

import org.alfresco.mobile.android.api.model.Node;

/**
 * Created by Bogdan Roatis on 8/14/2018.
 */
public class DownloadDocumentHolder {

    private static final DownloadDocumentHolder instance = new DownloadDocumentHolder();
    private Node node;

    public static DownloadDocumentHolder getInstance() {
        return instance;
    }

    public void setDocument(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    private DownloadDocumentHolder() {

    }
}
