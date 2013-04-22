package org.alfresco.mobile.android.application.accounts.fragment;

import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountSchema;
import org.alfresco.mobile.android.application.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.widget.LinearLayout;

public class AccountCursorAdapter extends BaseCursorLoader<GenericViewHolder>
{
    private List<Long> selectedItems;

    private int layoutId;

    public AccountCursorAdapter(Context context, Cursor c, int layoutId, List<Long> selectedItems)
    {
        super(context, c, layoutId);
        this.selectedItems = selectedItems;
        this.layoutId = layoutId;
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Cursor cursor)
    {
        vh.topText.setText(cursor.getString(AccountSchema.COLUMN_NAME_ID));
    }

    @Override
    protected void updateBottomText(GenericViewHolder v, Cursor cursor)
    {
        switch (layoutId)
        {
            case R.layout.app_account_list_row:
                // Do nothing
                break;
            default:
                updateBottomTextList(v, cursor);
                break;
        }
    }

    private void updateBottomTextList(GenericViewHolder v, Cursor cursor)
    {
        if (cursor.getString(AccountSchema.COLUMN_ACTIVATION_ID) != null)
        {
            v.bottomText.setText(context.getText(R.string.sign_up_cloud_awaiting_email));
        }
        else
        {
            v.bottomText.setText(cursor.getString(AccountSchema.COLUMN_USERNAME_ID));
        }

        if (selectedItems != null && selectedItems.contains(cursor.getLong(AccountSchema.COLUMN_ID_ID)))
        {
            UIUtils.setBackground(((LinearLayout) v.icon.getParent().getParent()),
                    context.getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((LinearLayout) v.icon.getParent().getParent()), null);
        }
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Cursor cursor)
    {
        switch (layoutId)
        {
            case R.layout.app_account_list_row:
                int itemName = Integer.parseInt(cursor.getString(AccountSchema.COLUMN_ID_ID));
                int defaultIcon = R.drawable.ic_account_light;
                switch (itemName)
                {
                    case NETWORK_ITEM:
                    case MANAGE_ITEM:
                        defaultIcon = R.drawable.ic_settings_light;
                        break;
                    default:
                        break;
                }
                vh.icon.setImageDrawable(context.getResources().getDrawable(defaultIcon));
                break;
            default:
                updateIconList(vh, cursor);
                break;
        }

    }

    private void updateIconList(GenericViewHolder vh, Cursor cursor)
    {
        int iconId = R.drawable.ic_onpremise;
        switch (cursor.getInt(AccountSchema.COLUMN_REPOSITORY_TYPE_ID))
        {
            case Account.TYPE_ALFRESCO_TEST_BASIC:
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
                iconId = R.drawable.ic_cloud_alf;
                break;
            case Account.TYPE_ALFRESCO_CLOUD:
                iconId = R.drawable.ic_cloud;
                break;
            default:
                iconId = R.drawable.ic_onpremise;
                break;
        }
        vh.icon.setImageDrawable(context.getResources().getDrawable(iconId));
    }

    public static final int NETWORK_ITEM = -3;

    public static final int MANAGE_ITEM = -4;

    public static Cursor createMergeCursor(Context c, Cursor cursor)
    {
        MatrixCursor extras = new MatrixCursor(new String[] { AccountSchema.COLUMN_ID, AccountSchema.COLUMN_NAME });
        if (SessionUtils.getAccount(c) != null)
        {
            long type = SessionUtils.getAccount(c).getTypeId();
            if (type == Account.TYPE_ALFRESCO_CLOUD || type == Account.TYPE_ALFRESCO_TEST_OAUTH)
            {
                extras.addRow(new String[] { NETWORK_ITEM + "", c.getString(R.string.cloud_networks_switch) });
            }
        }
        extras.addRow(new String[] { MANAGE_ITEM + "", c.getString(R.string.manage_accounts) });
        Cursor[] cursors = { cursor, extras };
        return new MergeCursor(cursors);
    }
}
