package org.alfresco.mobile.android.application.integration.node.delete;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.node.NodeOperationThread;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DeleteNodeThread extends NodeOperationThread<Void>
{
    private static final String TAG = DeleteNodeTask.class.getName();

    private Folder parent;
    
    public DeleteNodeThread(Context ctx, AbstractOperationRequestImpl request)
    {
        super(ctx, request);
    }
    
    @Override
    public LoaderResult<Void> doInBackground()
    {
        Log.d(TAG, "Start Delete");
        
        LoaderResult<Void> result = null;
        try
        {
            result = super.doInBackground();

            parent = session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);
            if (parent == null) { return result; }
            
            session.getServiceRegistry().getDocumentFolderService().deleteNode(node);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        
        return result;
    }
    
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_DELETE_COMPLETE);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, parent);
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, node);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
    
}
