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
package org.alfresco.mobile.android.application.fragments.utils;

import java.io.File;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.create.DocumentTypeRecordHelper;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.platform.data.DocumentTypeRecord;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils.ActionManagerListener;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

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
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.sdk_list, null);

        ListView lv = (ListView) v.findViewById(R.id.listView);

        lv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id)
            {
                DocumentTypeRecord record = (DocumentTypeRecord) l.getItemAtPosition(position);
                // Show properties
                ActionUtils.actionView(getActivity(), (File) getArguments().get(ARGUMENT_FILE), record.mimetype,
                        new ActionManagerListener()
                        {
                            @Override
                            public void onActivityNotFoundException(ActivityNotFoundException e)
                            {
                                Bundle b = new Bundle();
                                b.putInt(SimpleAlertDialogFragment.ARGUMENT_TITLE,
                                        R.string.error_unable_open_file_title);
                                b.putInt(SimpleAlertDialogFragment.ARGUMENT_MESSAGE, R.string.error_unable_open_file);
                                b.putInt(SimpleAlertDialogFragment.ARGUMENT_POSITIVE_BUTTON, android.R.string.ok);
                                ActionUtils.actionDisplayDialog(getActivity(), b);
                            }
                        });
                dismiss();
            }
        });

        List<DocumentTypeRecord> fileTypes = DocumentTypeRecordHelper.getOpenAsDocumentTypeList(getActivity());
        FileTypeAdapter adapter = new FileTypeAdapter(getActivity(), R.layout.sdk_list_row, fileTypes);
        lv.setAdapter(adapter);

        return new AlertDialog.Builder(getActivity()).setTitle(R.string.open_as_title).setView(v).create();
    }

    /**
     * Inner class responsible to manage the list of File Types available.
     */
    public class FileTypeAdapter extends BaseListAdapter<DocumentTypeRecord, GenericViewHolder>
    {

        public FileTypeAdapter(Activity context, int textViewResourceId, List<DocumentTypeRecord> listItems)
        {
            super(context, textViewResourceId, listItems);
        }

        @Override
        protected void updateTopText(GenericViewHolder vh, DocumentTypeRecord item)
        {
            if (item != null)
            {
                vh.topText.setText(item.nameString);
            }
        }

        @Override
        protected void updateBottomText(GenericViewHolder vh, DocumentTypeRecord item)
        {
            if (item != null)
            {
                vh.bottomText.setVisibility(View.GONE);
            }
        }

        @Override
        protected void updateIcon(GenericViewHolder vh, DocumentTypeRecord item)
        {
            if (item != null)
            {
                vh.icon.setImageResource(item.iconId);
            }
        }
    }

}
