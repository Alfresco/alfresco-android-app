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
package org.alfresco.mobile.android.application.fragments.utils;

import java.io.File;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.create.DocumentTypeRecordHelper;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.platform.data.DocumentTypeRecord;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils.ActionManagerListener;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.SingleLineViewHolder;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * @author Jean Marie Pascal
 */
public class OpenAsDialogFragment extends DialogFragment
{
    /** Public Fragment TAG. */
    public static final String TAG = OpenAsDialogFragment.class.getName();

    /**
     * Used for retrieving document information during document creation. Value
     * must be a DocumentTypeRecord object.
     */
    private static final String ARGUMENT_FILE = "documentType";

    private ListView lv;

    private List<DocumentTypeRecord> fileTypes;

    /**
     * Static constructor.
     * 
     * @param f
     * @return a dialogfragment to dipslay file type creation list.
     */
    public static OpenAsDialogFragment newInstance(File f)
    {
        OpenAsDialogFragment fragment = new OpenAsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_FILE, f);
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        fileTypes = DocumentTypeRecordHelper.getOpenAsDocumentTypeList(getActivity());
        FileTypeAdapter adapter = new FileTypeAdapter(getActivity(), R.layout.row_single_line, fileTypes);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .iconRes(R.drawable.ic_application_logo).title(R.string.open_as_title)
                .adapter(adapter, new MaterialDialog.ListCallback()
                {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int position,
                            CharSequence charSequence)
                    {

                        DocumentTypeRecord record = (DocumentTypeRecord) fileTypes.get(position);
                        // Show properties
                        ActionUtils.actionView(getActivity(), (File) getArguments().get(ARGUMENT_FILE), record.mimetype,
                                new ActionManagerListener()
                        {
                            @Override
                            public void onActivityNotFoundException(ActivityNotFoundException e)
                            {
                                new MaterialDialog.Builder(getActivity()).iconRes(R.drawable.ic_application_logo)
                                        .title(R.string.error_unable_open_file_title)
                                        .content(Html.fromHtml(getString(R.string.error_unable_open_file)))
                                        .positiveText(android.R.string.ok).show();
                            }
                        });
                        dismiss();

                        materialDialog.dismiss();
                    }
                });

        return builder.show();
    }

    /**
     * Inner class responsible to manage the list of File Types available.
     */
    public class FileTypeAdapter extends BaseListAdapter<DocumentTypeRecord, SingleLineViewHolder>
    {

        public FileTypeAdapter(FragmentActivity context, int textViewResourceId, List<DocumentTypeRecord> listItems)
        {
            super(context, textViewResourceId, listItems);
            this.vhClassName = SingleLineViewHolder.class.getCanonicalName();
        }

        @Override
        protected void updateTopText(SingleLineViewHolder vh, DocumentTypeRecord item)
        {
            if (item != null)
            {
                if (TextUtils.isEmpty(item.nameString))
                {
                    vh.topText.setText(item.nameId);
                }
                else
                {
                    vh.topText.setText(item.nameString);
                }
            }
        }

        @Override
        protected void updateBottomText(SingleLineViewHolder vh, DocumentTypeRecord item)
        {
        }

        @Override
        protected void updateIcon(SingleLineViewHolder vh, DocumentTypeRecord item)
        {
            if (item != null)
            {
                vh.icon.setImageResource(item.iconId);
            }
        }
    }

}
