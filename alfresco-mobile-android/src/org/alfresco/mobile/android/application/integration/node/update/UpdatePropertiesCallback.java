package org.alfresco.mobile.android.application.integration.node.update;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationCallback;

import android.content.Context;

public class UpdatePropertiesCallback  extends AbstractOperationCallback<Node>
{
    public UpdatePropertiesCallback(Context context, int totalItems, int pendingItems)
    {
       super(context, totalItems, pendingItems);
       inProgress = getBaseContext().getString(R.string.update);
       complete = getBaseContext().getString(R.string.update_sucess);
    }
}
