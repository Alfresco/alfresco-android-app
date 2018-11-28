package org.alfresco.mobile.android.async.local;

import android.content.Context;

import org.alfresco.mobile.android.async.impl.BaseOperationRequest;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

/**
 * Created by Bogdan Roatis on 11/28/2018.
 */
public class CleanLocalFilesRequest extends BaseOperationRequest {

    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 1499;

    final AlfrescoAccount account;

    protected CleanLocalFilesRequest(Context context, long accountId, String networkId, int notificationVisibility, String title, String mimeType, int requestTypeId, AlfrescoAccount account) {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);

        this.account = account;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey() {
        return account.getUrl();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder {

        protected AlfrescoAccount account;

        protected Builder() {
        }

        public Builder(AlfrescoAccount account) {
            this();
            this.account = account;
            this.requestTypeId = TYPE_ID;
        }

        public CleanLocalFilesRequest build(Context context) {
            return new CleanLocalFilesRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, account);
        }
    }
}
