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
package org.alfresco.mobile.android.application.editors.text;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.SingleLineViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * @author Jean Marie Pascal
 */
public class EncodingDialogFragment extends DialogFragment
{
    /** Public Fragment TAG. */
    public static final String TAG = EncodingDialogFragment.class.getName();

    private static final String ARGUMENT_DEFAULT_CHARSET = "EncodingDialogFragmentDefaultCharset";

    private ArrayList<String> list;

    private String defaultCharset = "UTF-8";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public EncodingDialogFragment()
    {
    }

    public static EncodingDialogFragment newInstance(String defaultCharset)
    {
        Bundle b = new Bundle();
        b.putString(ARGUMENT_DEFAULT_CHARSET, defaultCharset);
        EncodingDialogFragment fr = new EncodingDialogFragment();
        fr.setArguments(b);
        return fr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AnalyticsHelper.reportScreen(getActivity(), AnalyticsManager.SCREEN_TEXT_EDITOR_ENCODING);

        if (getArguments() != null && getArguments().containsKey(ARGUMENT_DEFAULT_CHARSET))
        {
            defaultCharset = getArguments().getString(ARGUMENT_DEFAULT_CHARSET, "UTF-8");
        }

        SortedMap<String, Charset> map = Charset.availableCharsets();
        list = new ArrayList<String>(map.keySet());
        EncodingAdapter adapter = new EncodingAdapter(getActivity(), R.layout.row_single_line, list, defaultCharset);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .iconRes(R.drawable.ic_application_logo);
        if (list.isEmpty())
        {
            builder.title(R.string.create_document_editor_not_available)
                    .content(Html.fromHtml(getString(R.string.create_document_editor_not_available_description)));
            return builder.show();
        }
        else
        {
            builder.title(R.string.file_editor_encoding).adapter(adapter, new MaterialDialog.ListCallback()
            {
                @Override
                public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence)
                {
                    if (getActivity() instanceof TextEditorActivity)
                    {
                        ((TextEditorActivity) getActivity()).reload(list.get(i));
                    }
                    materialDialog.dismiss();
                }
            });
        }
        return builder.show();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (list.indexOf(defaultCharset) != -1)
        {
            ((MaterialDialog) getDialog()).getListView().setSelection(list.indexOf(defaultCharset));
        }
    }

    private static class EncodingAdapter extends BaseListAdapter<String, SingleLineViewHolder>
    {
        private String defaultCharSet;

        public EncodingAdapter(FragmentActivity context, int textViewResourceId, List<String> listItems,
                String defaultCharSet)
        {
            super(context, textViewResourceId, listItems);
            this.defaultCharSet = defaultCharSet;
            this.vhClassName = SingleLineViewHolder.class.getCanonicalName();
        }

        @Override
        protected void updateTopText(SingleLineViewHolder vh, String item)
        {
            vh.topText.setText(item);
            if (defaultCharSet.equals(item))
            {
                UIUtils.setBackground(((View) vh.icon.getParent()),
                        getContext().getResources().getDrawable(R.drawable.list_longpressed_holo));
            }
            else
            {
                UIUtils.setBackground(((View) vh.icon.getParent()), null);
            }
        }

        @Override
        protected void updateBottomText(SingleLineViewHolder vh, String item)
        {
        }

        @Override
        protected void updateIcon(SingleLineViewHolder vh, String item)
        {
            vh.icon.setVisibility(View.GONE);
        }
    }
}
