package org.alfresco.mobile.android.application.fragments.workflow.process;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.holder.ViewHolder;

/**
 * Created by Bogdan Roatis on 10/11/2018.
 */
public class ProcessViewHolder extends ViewHolder
{
    public TextView topText;

    public TextView bottomText;

    public ImageView icon;

    public ImageView icon_statut;

    public TextView content;

    public ProcessViewHolder(View v)
    {
        super(v);
        icon = v.findViewById(R.id.icon);
        icon_statut = v.findViewById(R.id.icon_status);
        topText = v.findViewById(R.id.toptext);
        bottomText = v.findViewById(R.id.bottomtext);
        content = v.findViewById(R.id.contentweb);
    }
}
