package org.alfresco.mobile.android.application.integration.node.favorite;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.node.NodeOperationTask;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FavoriteNodeTask extends NodeOperationTask<Boolean>
{
    private static final String TAG = FavoriteNodeTask.class.getName();

    private Boolean value;
    private Boolean isFavorite = Boolean.FALSE;


    public FavoriteNodeTask(Context ctx, OperationRequest request)
    {
        super(ctx, request);
        if (request instanceof FavoriteNodeRequest)
        {
            this.value = ((FavoriteNodeRequest) request).markAsFavorite();
        }
    }

    @Override
    protected LoaderResult<Boolean> doInBackground(Void... params)
    {
        LoaderResult<Boolean> result = null;
        try
        {
            result = super.doInBackground();

            isFavorite = session.getServiceRegistry().getDocumentFolderService().isFavorite(node);

            if ((value == null && isFavorite) || (value != null && !value && isFavorite))
            {
                session.getServiceRegistry().getDocumentFolderService().removeFavorite(node);
                isFavorite = false;
            }
            else if ((value == null && !isFavorite) || (value != null && value && !isFavorite))
            {
                session.getServiceRegistry().getDocumentFolderService().addFavorite(node);
                isFavorite = true;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData(isFavorite);

        return result;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_FAVORITE_COMPLETE);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, node);
        b.putString(IntentIntegrator.EXTRA_FAVORITE, isFavorite.toString());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
