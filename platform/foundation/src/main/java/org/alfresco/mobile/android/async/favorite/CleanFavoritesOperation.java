package org.alfresco.mobile.android.async.favorite;

import android.database.Cursor;
import android.util.Log;

import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.favorite.FavoritesManager;
import org.alfresco.mobile.android.platform.favorite.FavoritesProvider;
import org.alfresco.mobile.android.platform.favorite.FavoritesSchema;

/**
 * Created by Bogdan Roatis on 11/28/2018.
 */
public class CleanFavoritesOperation extends BaseOperation<Void> {

    private static final String TAG = CleanFavoritesOperation.class.getName();


    public CleanFavoritesOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action) {
        super(operator, dispatcher, action);

        if (request instanceof CleanFavoritesRequest) {
            this.accountId = ((CleanFavoritesRequest) request).account.getId();
        }
    }

    @Override
    protected LoaderResult<Void> doInBackground() {
        LoaderResult<Void> result = new LoaderResult<Void>();

        try {
            Cursor favoriteCursor = context.getContentResolver().query(FavoritesProvider.CONTENT_URI,
                    FavoritesSchema.COLUMN_ALL, FavoritesProvider.getAccountFilter(accountId), null, null);

            while (favoriteCursor.moveToNext()) {
                context.getContentResolver().delete(
                        FavoritesManager.getUri(favoriteCursor.getLong(FavoritesSchema.COLUMN_ID_ID)),
                        null, null);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Void> result) {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new CleanFavoritesEvent());
    }
}
