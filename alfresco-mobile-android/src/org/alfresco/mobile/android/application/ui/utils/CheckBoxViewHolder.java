package org.alfresco.mobile.android.application.ui.utils;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.utils.ViewHolder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class CheckBoxViewHolder extends ViewHolder
{
    public TextView topText;

    public TextView bottomText;

    public CheckBox checkBox;

    public CheckBoxViewHolder(View v)
    {
        super(v);
        topText = (TextView) v.findViewById(R.id.toptext);
        bottomText = (TextView) v.findViewById(R.id.bottomtext);
        checkBox = (CheckBox) v.findViewById(R.id.checkbox);
    }
}
