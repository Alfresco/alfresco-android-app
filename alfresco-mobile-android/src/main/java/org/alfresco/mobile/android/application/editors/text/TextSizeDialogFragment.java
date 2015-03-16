/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.editors.text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.SparseArray;
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
public class TextSizeDialogFragment extends DialogFragment
{
    /** Public Fragment TAG. */
    public static final String TAG = TextSizeDialogFragment.class.getName();

    private static final LinkedHashMap<String, Integer> SIZE_MAP = new LinkedHashMap<String, Integer>(10)
    {
        private static final long serialVersionUID = 1L;
        {
            put("8 pt", 8);
            put("10 pt", 10);
            put("12 pt", 12);
            put("14 pt", 14);
            put("16 pt", 16);
            put("18 pt", 18);
            put("20 pt", 20);
            put("24 pt", 24);
            put("28 pt", 28);
            put("32 pt", 32);
            put("36 pt", 36);
            put("42 pt", 48);
        }
    };

    private static final SparseArray<String> INDEX = new SparseArray<String>(10)
    {
        {
            put(8, "8 pt");
            put(10, "10 pt");
            put(12, "12 pt");
            put(14, "14 pt");
            put(16, "16 pt");
            put(18, "18 pt");
            put(20, "20 pt");
            put(24, "24 pt");
            put(28, "28 pt");
            put(32, "32 pt");
            put(36, "36 pt");
            put(48, "42 pt");
        }
    };

    public static final int DEFAULT_TEXT_SIZE = 16;

    private static final String ARGUMENT_DEFAULT_SIZE = "TextSizeDialogFragmentSize";

    private int textSize = DEFAULT_TEXT_SIZE;

    public TextSizeDialogFragment()
    {
    }

    public static TextSizeDialogFragment newInstance(int textSize)
    {
        Bundle b = new Bundle();
        b.putInt(ARGUMENT_DEFAULT_SIZE, textSize);
        TextSizeDialogFragment fr = new TextSizeDialogFragment();
        fr.setArguments(b);
        return fr;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int title = R.string.file_editor_text_size;

        if (getArguments() != null && getArguments().containsKey(ARGUMENT_DEFAULT_SIZE))
        {
            textSize = getArguments().getInt(ARGUMENT_DEFAULT_SIZE, DEFAULT_TEXT_SIZE);
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.sdk_list, null);
        ListView lv = (ListView) v.findViewById(R.id.listView);

        ArrayList<Integer> list = new ArrayList<>(SIZE_MAP.values());

        if (list.isEmpty())
        {
            // If there's no 3rd party application able to create, we display a
            // warning message.
            lv.setVisibility(View.GONE);
            v.findViewById(R.id.empty).setVisibility(View.VISIBLE);
            ((TextView) v.findViewById(R.id.empty_text))
                    .setText(R.string.create_document_editor_not_available_description);
            title = R.string.create_document_editor_not_available;

            return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v).create();
        }
        else
        {
            lv.setAdapter(new EncodingAdapter(getActivity(), R.layout.sdk_list_row, list, textSize));

            lv.setOnItemClickListener(new OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> l, View v, int position, long id)
                {
                    if (getActivity() instanceof TextEditorActivity)
                    {
                        ((TextEditorActivity) getActivity()).setTextSize((Integer) l.getItemAtPosition(position));
                    }
                    dismiss();
                }
            });
            return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v).create();
        }

    }

    private static class EncodingAdapter extends BaseListAdapter<Integer, GenericViewHolder>
    {
        private int textSize;

        public EncodingAdapter(Activity context, int textViewResourceId, List<Integer> listItems, int textSize)
        {
            super(context, textViewResourceId, listItems);
            this.textSize = textSize;
        }

        @Override
        protected void updateTopText(GenericViewHolder vh, Integer item)
        {
            vh.topText.setText(INDEX.get(item));
            if (textSize == item)
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
        protected void updateBottomText(GenericViewHolder vh, Integer item)
        {
            vh.bottomText.setVisibility(View.GONE);
        }

        @Override
        protected void updateIcon(GenericViewHolder vh, Integer item)
        {
            vh.icon.setVisibility(View.GONE);
        }
    }
}
