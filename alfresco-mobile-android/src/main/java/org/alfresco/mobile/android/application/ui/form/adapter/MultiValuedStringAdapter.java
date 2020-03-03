/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.ui.form.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;

/**
 * @since 1.4.0
 * @author jpascal
 */
public class MultiValuedStringAdapter extends BaseListAdapter<String, TwoLinesViewHolder>
{
    private WeakReference<Fragment> fragmentRef;

    private boolean isEditable = false;

    public MultiValuedStringAdapter(Fragment fr, int textViewResourceId, List<String> listItems, boolean isEditable)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.fragmentRef = new WeakReference<Fragment>(fr);
        this.vhClassName = TwoLinesViewHolder.class.getCanonicalName();
        this.isEditable = isEditable;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, String item)
    {
        vh.topText.setVisibility(View.GONE);
    }
    
    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, String item)
    {
        vh.bottomText.setText(item);
    }

    @Override
    protected void updateIcon(final TwoLinesViewHolder vh, final String item)
    {
        vh.icon.setVisibility(View.GONE);
        if (isEditable)
        {
            vh.choose.setVisibility(View.VISIBLE);
            vh.choose.setScaleType(ScaleType.CENTER_INSIDE);
            int d_16 = DisplayUtils.getPixels(getContext(), R.dimen.d_16);
            vh.choose.setPadding(d_16, d_16, d_16, d_16);
            vh.choose.setImageResource(R.drawable.ic_cancel);
            vh.choose.setTag(vh);
            vh.choose.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    remove(item);
                    notifyDataSetChanged();
                    if (fragmentRef.get() instanceof EditPropertiesPickerFragment)
                    {
                        ((EditPropertiesPickerFragment) fragmentRef.get()).removeValue(item);
                    }
                }
            });
        }
        else
        {
            vh.choose.setVisibility(View.GONE);
        }
    }
}
