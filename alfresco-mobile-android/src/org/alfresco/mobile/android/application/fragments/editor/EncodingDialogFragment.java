/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.editor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author Jean Marie Pascal
 */
public class EncodingDialogFragment extends DialogFragment
{
    /** Public Fragment TAG. */
    public static final String TAG = EncodingDialogFragment.class.getName();

    private static final String PARAM_DEFAULT_CHARSET = "EncodingDialogFragmentDefaultCharset";

    private ArrayList<String> list;

    private String defaultCharset = "UTF-8";

    public EncodingDialogFragment()
    {
    }

    public static EncodingDialogFragment newInstance(String defaultCharset)
    {
        Bundle b = new Bundle();
        b.putString(PARAM_DEFAULT_CHARSET, defaultCharset);
        EncodingDialogFragment fr = new EncodingDialogFragment();
        fr.setArguments(b);
        return fr;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getArguments() != null && getArguments().containsKey(PARAM_DEFAULT_CHARSET))
        {
            defaultCharset = getArguments().getString(PARAM_DEFAULT_CHARSET, "UTF-8");
        }

        int title = R.string.file_editor_encoding;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.sdk_list, null);
        ListView lv = (ListView) v.findViewById(R.id.listView);

        SortedMap<String, Charset> map = Charset.availableCharsets();
        list = new ArrayList<String>(map.keySet());
        
        if (list.isEmpty())
        {
            lv.setVisibility(View.GONE);
            v.findViewById(R.id.empty).setVisibility(View.VISIBLE);
            v.findViewById(R.id.empty_picture).setVisibility(View.GONE);
            ((TextView) v.findViewById(R.id.empty_text))
                    .setText(R.string.create_document_editor_not_available_description);
            title = R.string.create_document_editor_not_available;

            return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v).create();
        }
        else
        {
            lv.setAdapter(new EncodingAdapter(getActivity(), R.layout.sdk_list_row, list, defaultCharset));
            lv.setSelection(list.indexOf(defaultCharset));

            lv.setOnItemClickListener(new OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> l, View v, int position, long id)
                {
                    if (getActivity() instanceof TextEditorActivity)
                    {
                        ((TextEditorActivity) getActivity()).reload(list.get(position));
                    }
                    dismiss();
                }
            });
            return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v).create();
        }

    }

    private static class EncodingAdapter extends BaseListAdapter<String, GenericViewHolder>
    {
        private String defaultCharSet;

        public EncodingAdapter(Activity context, int textViewResourceId, List<String> listItems, String defaultCharSet)
        {
            super(context, textViewResourceId, listItems);
            this.defaultCharSet = defaultCharSet;
        }

        @Override
        protected void updateTopText(GenericViewHolder vh, String item)
        {
            vh.topText.setText(item);
            if (defaultCharSet.equals(item))
            {
                UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()), getContext().getResources()
                        .getDrawable(R.drawable.list_longpressed_holo));
            }
            else
            {
                UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()), null);
            }
        }

        @Override
        protected void updateBottomText(GenericViewHolder vh, String item)
        {
            vh.bottomText.setVisibility(View.GONE);
        }

        @Override
        protected void updateIcon(GenericViewHolder vh, String item)
        {
            vh.icon.setVisibility(View.GONE);
        }
    }
}
