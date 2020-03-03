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
package org.alfresco.mobile.android.application.extension.scansnap;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.extension.scansnap.presets.DefaultPreset;
import org.alfresco.mobile.android.application.extension.scansnap.presets.PhotoPreset;
import org.alfresco.mobile.android.application.extension.scansnap.presets.ScanSnapPreset;
import org.alfresco.mobile.android.platform.extensions.ScanSnapManager;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.SingleLineViewHolder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * This Fragment is responsible to display the list of ScanSnap preset.
 * 
 * @author Jean Marie Pascal
 */
public class ScanSnapPresetsDialogFragment extends DialogFragment
{

    public static final String TAG = ScanSnapPresetsDialogFragment.class.getSimpleName();

    public static ScanSnapPresetsDialogFragment newInstance()
    {
        return new ScanSnapPresetsDialogFragment();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.sdk_list, null);

        ListView lv = (ListView) v.findViewById(R.id.listView);

        lv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id)
            {
                ScanSnapManager.getInstance(getActivity()).scan(getActivity(),
                        ((ScanSnapPreset) l.getItemAtPosition(position)).getIdentifier());
                dismiss();
            }
        });

        List<ScanSnapPreset> presets = new ArrayList<>();
        presets.add(new DefaultPreset(getActivity().getApplicationContext().getPackageName()));
        presets.add(new PhotoPreset(getActivity().getApplicationContext().getPackageName()));
        ScanSnapPresetAdapter adapter = new ScanSnapPresetAdapter(getActivity(), R.layout.row_single_line, presets);
        lv.setAdapter(adapter);
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.scan_preset).setView(v).create();
    }

    /**
     * Inner class responsible to manage the list of File Types available.
     */
    public class ScanSnapPresetAdapter extends BaseListAdapter<ScanSnapPreset, SingleLineViewHolder>
    {

        public ScanSnapPresetAdapter(FragmentActivity context, int textViewResourceId, List<ScanSnapPreset> listItems)
        {
            super(context, textViewResourceId, listItems);
            this.vhClassName = SingleLineViewHolder.class.getCanonicalName();
        }

        @Override
        protected void updateTopText(SingleLineViewHolder vh, ScanSnapPreset item)
        {
            if (item != null)
            {
                vh.topText.setText(getContext().getResources().getString(item.getTitleId()));
            }
        }

        @Override
        protected void updateBottomText(SingleLineViewHolder vh, ScanSnapPreset item)
        {
        }

        @Override
        protected void updateIcon(SingleLineViewHolder vh, ScanSnapPreset item)
        {
            if (item != null)
            {
                vh.icon.setImageResource(item.getIconId());
            }
        }
    }

}
