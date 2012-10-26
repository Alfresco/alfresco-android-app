package org.alfresco.mobile.android.application.fragments.browser;

import java.util.List;

import org.alfresco.mobile.android.application.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PathAdapter extends ArrayAdapter<String>
{
    private String item;

    public PathAdapter(Context context, int textViewResourceId, List<String> objects)
    {
        super(context, textViewResourceId, objects);
        // TODO Auto-generated constructor stub
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(android.R.layout.simple_spinner_dropdown_item, null);
        }
        item = getItem(position);
        if (item != null)
        {
            TextView tv = (TextView) v.findViewById(android.R.id.text1);
            if (tv != null)
            {
                tv.setText(item);
            }
        }
        return v;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.sdkapp_list_quickaccount, null);
        }
        item = getItem(position);
        if (item != null)
        {
            ((TextView) v.findViewById(R.id.toptext)).setText("jeanmarie.pascal@alfresco.com");
            ((TextView) v.findViewById(R.id.bottomtext)).setText("Document Library ");
        }
        return v;
    }
    
}
