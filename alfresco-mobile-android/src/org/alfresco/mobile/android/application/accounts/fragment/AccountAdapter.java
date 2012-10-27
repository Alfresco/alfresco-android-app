package org.alfresco.mobile.android.application.accounts.fragment;

import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountAdapter extends ArrayAdapter<String>
{
    private Account item;

    private List<Account> accounts;

    private Account selectedAccount;

    public AccountAdapter(Context context, int textViewResourceId, List<Account> accounts)
    {
        super(context, textViewResourceId);
        this.accounts = accounts;
        refreshData(accounts);
    }
    
    public void refreshData( List<Account> accounts){
        clear();
        this.accounts = accounts;
        for (int i = 0; i < accounts.size(); i++)
        {
            add(getLabel(accounts.get(i)));
        }
        add(getContext().getText(R.string.manage_accounts).toString());
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getInternalView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return getInternalView(position, convertView, parent);
    }
    
    private View getInternalView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.app_account_list_row, null);
        }

        TextView tv = (TextView) v.findViewById(R.id.toptext);
        ImageView iv = (ImageView) v.findViewById(R.id.icon);
        if (position == accounts.size())
        {
            tv.setText(getContext().getText(R.string.manage_accounts));
            iv.setVisibility(View.VISIBLE);
            iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_settings_light));
        }
        else
        {
            iv.setVisibility(View.VISIBLE);
            item = accounts.get(position);
            tv.setText(item.getDescription());
            if (selectedAccount != null && item.getId() == selectedAccount.getId())
            {
                iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_confirm_light));
            }
            else
            {
                iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_account_light));
            }
        }
        return v;
    }

    private String getLabel(Account account)
    {
        String label = account.getDescription();
        if (label == null || label.isEmpty())
        {
            label = account.getUsername();
        }
        return label;
    }
}
