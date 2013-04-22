package org.alfresco.mobile.android.application.fragments.operations;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.BaseCursorListFragment;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.OperationContentProvider;
import org.alfresco.mobile.android.application.integration.OperationManager;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class OperationsFragment extends BaseCursorListFragment
{
    public static final String TAG = OperationsFragment.class.getName();

    private Button cancel_all;

    private Button dismiss_all;

    public OperationsFragment()
    {
        emptyListMessageId = R.string.operations_empty;
        layoutId = R.layout.app_operations_list;
        title = R.string.operations;
    }
    
    // /////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        adapter = new OperationCursorAdapter(getActivity(), null, R.layout.app_list_operation_row);
        lv.setAdapter(adapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    // /////////////////////////////////////////////////////////////
    // UTILS
    // ////////////////////////////////////////////////////////////
    protected void init(View v, int estring)
    {
        super.init(v, estring);
        cancel_all = (Button) v.findViewById(R.id.cancel_all);
        cancel_all.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelAll();
            }
        });

        dismiss_all = (Button) v.findViewById(R.id.dismiss_all);
        dismiss_all.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismissAll();
            }
        });
    }

    protected void cancelAll()
    {
        OperationManager.cancelAll(getActivity());
    }

    protected void dismissAll()
    {
        Cursor cursor = adapter.getCursor();
        Uri uri = null;
        if (!cursor.isFirst())
        {
            cursor.moveToPosition(-1);
        }

        for (int i = 0; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            uri = Uri.parse(OperationContentProvider.CONTENT_URI + "/" + cursor.getInt(OperationSchema.COLUMN_ID_ID));
            getActivity().getContentResolver().delete(uri, null, null);
        }
        cancel_all.setVisibility(View.GONE);
        dismiss_all.setVisibility(View.GONE);
    }

    public void onListItemClick(ListView l, View v, int position, long id)
    {
        MessengerManager.showToast(getActivity(), TAG);
    }

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        return false;
    }

    // /////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);
        Uri baseUri = OperationContentProvider.CONTENT_URI;
        return new CursorLoader(getActivity(), baseUri, OperationSchema.COLUMN_ALL, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        super.onLoadFinished(arg0, cursor);
        if (cursor.getCount() == 0)
        {
            dismiss_all.setVisibility(View.GONE);
        }
        else
        {
            dismiss_all.setVisibility(View.VISIBLE);
            if (!cursor.isFirst())
            {
                cursor.moveToPosition(-1);
            }

            boolean isVisible = false;
            while (cursor.moveToNext())
            {
                if (cursor.getInt(OperationSchema.COLUMN_STATUS_ID) == Operation.STATUS_RUNNING
                        || cursor.getInt(OperationSchema.COLUMN_STATUS_ID) == Operation.STATUS_PENDING)
                {
                    isVisible = true;
                    break;
                }
            }

            cancel_all.setVisibility(View.GONE);
            if (isVisible)
            {
                cancel_all.setVisibility(View.VISIBLE);
            }
        }
    }
}
