package org.alfresco.mobile.android.application.integration.node.create;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationCallback;

import android.content.Context;

public class CreateFolderCallBack extends AbstractOperationCallback<Folder>
{

    public CreateFolderCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.create_folder_in_progress);
        complete = getBaseContext().getString(R.string.create_folder_complete);
    }

}
