package org.alfresco.mobile.android.application.ui.form.adapter;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.SingleLineViewHolder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class AllowableAdapter extends BaseListAdapter<String, SingleLineViewHolder>
{
    /**
     * For cardinality == single
     */
    public AllowableAdapter(Context context, int textViewResourceId, Map<String, Object> objects)
    {
        super(context, textViewResourceId, new ArrayList<>(objects.keySet()));
        this.vhClassName = SingleLineViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateTopText(SingleLineViewHolder vh, String item)
    {
        vh.topText.setText(item);
    }

    @Override
    protected void updateBottomText(SingleLineViewHolder vh, String map)
    {
    }

    @Override
    protected void updateIcon(SingleLineViewHolder vh, String map)
    {
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }
}
