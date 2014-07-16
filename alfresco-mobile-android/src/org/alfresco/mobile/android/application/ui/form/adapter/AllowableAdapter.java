package org.alfresco.mobile.android.application.ui.form.adapter;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.application.ui.utils.CheckBoxViewHolder;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class AllowableAdapter extends BaseListAdapter<String, CheckBoxViewHolder>
{
    private Map<String, Object> selectedItems;

    /**
     * For cardinality == single
     * 
     * @param context
     * @param textViewResourceId
     * @param objects
     * @param selectedItems
     */
    public AllowableAdapter(Context context, int textViewResourceId, Map<String, Object> objects,
            Map<String, Object> selectedItems)
    {
        super(context, textViewResourceId, new ArrayList<String>(objects.keySet()));
        this.vhClassName = CheckBoxViewHolder.class.getCanonicalName();
        this.selectedItems = selectedItems;
    }

    @Override
    protected void updateTopText(CheckBoxViewHolder vh, String item)
    {
        vh.topText.setText(item);
    }

    @Override
    protected void updateBottomText(CheckBoxViewHolder vh, String map)
    {
        vh.bottomText.setVisibility(View.GONE);
        if (selectedItems.containsKey(map))
        {
            vh.checkBox.setChecked(true);
        }
        else
        {
            vh.checkBox.setChecked(false);
        }
    }

    @Override
    protected void updateIcon(CheckBoxViewHolder vh, String map)
    {
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }

}
