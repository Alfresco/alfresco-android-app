/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.alfresco.mobile.android.ui.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jpascal on 27/03/2015.
 */
public class HolderUtils
{
    private HolderUtils()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW CREATION + HOLDER
    // ///////////////////////////////////////////////////////////////////////////
    public static TwoLinesViewHolder configure(ViewGroup viewRoot, int layoutId, String topText, String bottomText,
            int imageId)
    {
        View v = LayoutInflater.from(viewRoot.getContext()).inflate(layoutId, viewRoot, false);
        TwoLinesViewHolder vh = configure(v, topText, bottomText, imageId);
        viewRoot.addView(v);
        return vh;
    }

    public static TwoLinesViewHolder configure(ViewGroup viewRoot, int layoutId, String topText, String bottomText,
            int imageId, View.OnClickListener listener)
    {
        View v = LayoutInflater.from(viewRoot.getContext()).inflate(layoutId, viewRoot, false);
        if (listener != null)
        {
            v.setOnClickListener(listener);
        }
        TwoLinesViewHolder vh = configure(v, topText, bottomText, imageId);
        viewRoot.addView(v);
        return vh;
    }

    public static TwoLinesCaptionViewHolder configure(View v, String topText, String caption, String bottomText,
            int imageId)
    {
        TwoLinesCaptionViewHolder vh = new TwoLinesCaptionViewHolder(v);
        HolderUtils.configure(vh, topText, caption, bottomText, imageId);
        return vh;
    }

    public static TwoLinesViewHolder configure(View v, String topText, String bottomText, int imageId)
    {
        TwoLinesViewHolder vh = new TwoLinesViewHolder(v);
        configure(vh, topText, bottomText, imageId);
        return vh;
    }

    public static TwoLinesCheckboxViewHolder configure(View v, String topText, String bottomText, boolean checked)
    {
        TwoLinesCheckboxViewHolder vh = new TwoLinesCheckboxViewHolder(v);
        configure(vh, topText, bottomText, -1, checked);
        return vh;
    }

    public static SingleLineViewHolder configure(View v, String topText, int imageId)
    {
        SingleLineViewHolder vh = new SingleLineViewHolder(v);
        configure(vh, topText, imageId);
        return vh;
    }

    public static SingleLineSwitchViewHolder configure(View v, String topText, int imageId, boolean checked)
    {
        SingleLineSwitchViewHolder vh = new SingleLineSwitchViewHolder(v);
        configure(vh, topText, imageId, checked);
        return vh;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER
    // ///////////////////////////////////////////////////////////////////////////
    public static void configure(SingleLineSwitchViewHolder vh, String topText, int imageId, boolean checked)
    {
        vh.topText.setText(topText);
        if (imageId == -1)
        {
            vh.icon.setVisibility(View.GONE);
        }
        else
        {
            vh.icon.setImageResource(imageId);
        }
        vh.switcher.setChecked(checked);
    }

    public static void configure(SingleLineViewHolder vh, String topText, int imageId)
    {
        vh.topText.setText(topText);
        if (imageId == -1)
        {
            vh.icon.setVisibility(View.GONE);
        }
        else
        {
            vh.icon.setVisibility(View.VISIBLE);
            vh.icon.setImageResource(imageId);
        }
    }

    public static void configure(TwoLinesCheckboxViewHolder vh, String topText, String bottomText, int imageId,
            boolean checked)
    {
        configure(vh, topText, imageId);
        if (bottomText != null)
        {
            vh.bottomText.setText(bottomText);
        }
        else
        {
            vh.bottomText.setVisibility(View.GONE);
        }
        vh.choose.setChecked(checked);
    }

    public static void configure(TwoLinesViewHolder vh, String topText, String bottomText, int imageId)
    {
        configure(vh, topText, imageId);
        if (bottomText != null)
        {
            vh.bottomText.setText(bottomText);
        }
        else
        {
            vh.bottomText.setVisibility(View.GONE);
        }
    }

    public static void configure(TwoLinesCaptionViewHolder vh, String topText, String caption, String bottomText,
            int imageId)
    {
        configure(vh, topText, bottomText, imageId);
        if (caption != null)
        {
            vh.topTextRight.setText(caption);
        }
        else
        {
            vh.topTextRight.setVisibility(View.GONE);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TEXVIEW
    // ///////////////////////////////////////////////////////////////////////////
    public static void makeMultiLine(TextView tv, int maxLines)
    {
        tv.setSingleLine(false);
        tv.setMaxLines(maxLines);
    }
}
