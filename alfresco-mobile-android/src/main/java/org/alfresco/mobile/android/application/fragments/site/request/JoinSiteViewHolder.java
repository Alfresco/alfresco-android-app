package org.alfresco.mobile.android.application.fragments.site.request;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.holder.ViewHolder;

/**
 * Created by Bogdan Roatis on 10/11/2018.
 */
public class JoinSiteViewHolder extends ViewHolder
{
    public TextView topText;

    public TextView bottomText;

    public ImageView icon;

    public Button cancel_request;

    public JoinSiteViewHolder(View v)
    {
        super(v);
        icon = v.findViewById(R.id.icon);
        topText = v.findViewById(R.id.toptext);
        bottomText = v.findViewById(R.id.bottomtext);
        cancel_request = v.findViewById(R.id.cancel_request);
    }
}
