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
package org.alfresco.mobile.android.application.fragments.create;

import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.commons.data.DocumentTypeRecord;
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
import android.widget.ListView;

/**
 * This Fragment is responsible to display the list of File that can be created
 * inside the application via third party application.
 * 
 * @author Jean Marie Pascal
 */
public class DocumentTypesDialogFragment extends DialogFragment
{
    /** Public Fragment TAG. */
    public static final String TAG = "FileTypePropertiesDialogFragment";

    /**
     * Used for retrieving default storage folder. Value must be an Account object.
     */
    public static final String PARAM_ACCOUNT = "account";
    
    /**
     * Used for retrieving from which Fragment the wizard has been started.
     */
    public static final String PARAM_FRAGMENT_TAG = "FragmentTag";

    /**
     * Used for retrieving document information during document creation.
     * Value must be a DocumentTypeRecord object.
     */
    public static final String PARAM_DOCUMENT_TYPE = "documentType";

    /**
     * Static constructor.
     * 
     * @param currentAccount : Alfresco Account.
     * @return a dialogfragment to dipslay file type creation list.
     */
    public static DocumentTypesDialogFragment newInstance(Account currentAccount, String fragmentTag)
    {
        DocumentTypesDialogFragment fragment = new DocumentTypesDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_ACCOUNT, currentAccount);
        args.putSerializable(PARAM_FRAGMENT_TAG, fragmentTag);
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
                Bundle b = getArguments();
                b.putSerializable(PARAM_DOCUMENT_TYPE, (DocumentTypeRecord) l.getItemAtPosition(position));
                EditorsDialogFragment dialogft = EditorsDialogFragment.newInstance(b);
                dialogft.show(getFragmentManager(), EditorsDialogFragment.TAG);

                dismiss();
            }
        });

        List<DocumentTypeRecord> fileTypes = DocumentTypeRecordHelper.getCreationDocumentTypeList(getActivity());
        FileTypeAdapter adapter = new FileTypeAdapter(getActivity(), R.layout.sdk_list_row, fileTypes);
        lv.setAdapter(adapter);

        return new AlertDialog.Builder(getActivity()).setTitle(R.string.create_document_title).setView(v).create();
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
                vh.topText.setText(getString(item.nameId));
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
