package org.alfresco.mobile.android.application.integration.node.like;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.node.NodeOperationTask;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LikeNodeTask extends NodeOperationTask<Boolean>
{
    private static final String TAG = LikeNodeTask.class.getName();

    private Boolean value;
    private Boolean isLiked = Boolean.FALSE;

    public LikeNodeTask(Context ctx, OperationRequest request)
    {
        super(ctx, request);
        if (request instanceof LikeNodeRequest)
        {
            this.value = ((LikeNodeRequest) request).getLikeOperation();
        }
    }

    @Override
    protected LoaderResult<Boolean> doInBackground(Void... params)
    {
        LoaderResult<Boolean> result = null;
        
        try
        {
            result = super.doInBackground();

            isLiked = session.getServiceRegistry().getRatingService().isLiked(node);

            if ((value == null && isLiked) || (value != null && !value && isLiked))
            {
                session.getServiceRegistry().getRatingService().unlike(node);
                isLiked = false;
            }
            else if ((value == null && !isLiked) || (value != null && value && !isLiked))
            {
                session.getServiceRegistry().getRatingService().like(node);
                isLiked = true;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData(isLiked);

        return result;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_LIKE_COMPLETE);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, node);
        b.putString(IntentIntegrator.EXTRA_LIKE, isLiked.toString());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
