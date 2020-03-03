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

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskPickerFragment;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.application.ui.form.fields.AuthorityField;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.person.PeopleAdapter;

import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView.ScaleType;

/**
 * @since 1.5.0
 * @author jpascal
 */
public class AuthorityAdapter extends PeopleAdapter
{
    private WeakReference<Fragment> fragmentRef;

    private boolean isEditable = true;

    private String outputValue;

    public AuthorityAdapter(Fragment fr, int textViewResourceId, List<Person> listItems, String propertyId)
    {
        super(fr, textViewResourceId, listItems);
        this.fragmentRef = new WeakReference<Fragment>(fr);
        this.renditionManager = RenditionManagerImpl.getInstance(fr.getActivity());
        ((RenditionManagerImpl) this.renditionManager).setCurrentActivity(fr.getActivity());
        this.outputValue = propertyId;
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, Person item)
    {
        switch (outputValue)
        {
            case AuthorityField.OUTPUT_FULLNAME:
                vh.topText.setText(item.getFullName());
                break;
            case AuthorityField.OUTPUT_OBJECT:
                vh.topText.setText(item.getFullName());
                break;
            default:
                vh.topText.setText(item.getIdentifier());
        }
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, Person item)
    {
        vh.bottomText.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(final TwoLinesViewHolder vh, final Person item)
    {
        super.updateIcon(vh, item);

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
                    if (fragmentRef.get() instanceof CreateTaskPickerFragment)
                    {
                        ((CreateTaskPickerFragment) fragmentRef.get()).removeAssignee(item);
                    }
                    else if (fragmentRef.get() instanceof EditPropertiesPickerFragment)
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
