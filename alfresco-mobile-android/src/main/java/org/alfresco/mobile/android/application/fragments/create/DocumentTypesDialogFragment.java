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
package org.alfresco.mobile.android.application.fragments.create;

import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.data.DocumentTypeRecord;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.SingleLineViewHolder;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

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
     * Used for retrieving default storage folder. Value must be an
     * AlfrescoAccount object.
     */
    public static final String ARGUMENT_ACCOUNT = "account";

    /**
     * Used for retrieving from which Fragment the wizard has been started.
     */
    public static final String ARGUMENT_FRAGMENT_TAG = "FragmentTag";

    /**
     * Used for retrieving document information during document creation. Value
     * must be a DocumentTypeRecord object.
     */
    public static final String ARGUMENT_DOCUMENT_TYPE = "documentType";

    private List<DocumentTypeRecord> fileTypes;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Static constructor.
     * 
     * @param currentAccount : Alfresco AlfrescoAccount.
     * @return a dialogfragment to dipslay file type creation list.
     */
    public static DocumentTypesDialogFragment newInstance(AlfrescoAccount currentAccount, String fragmentTag)
    {
        DocumentTypesDialogFragment fragment = new DocumentTypesDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_ACCOUNT, currentAccount);
        args.putSerializable(ARGUMENT_FRAGMENT_TAG, fragmentTag);
        fragment.setArguments(args);
        return fragment;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AnalyticsHelper.reportScreen(getActivity(), AnalyticsManager.SCREEN_NODE_CREATE_TYPE);

        fileTypes = DocumentTypeRecordHelper.getCreationDocumentTypeList(getActivity());
        FileTypeAdapter adapter = new FileTypeAdapter(getActivity(), R.layout.row_single_line, fileTypes);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .iconRes(R.drawable.ic_application_logo).title(R.string.create_document_title)
                .adapter(adapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int position,
                                            CharSequence charSequence) {
                        AnalyticsHelper.reportOperationEvent(getActivity(),
                                AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT, AnalyticsManager.ACTION_QUICK_ACTIONS,
                                AnalyticsManager.ACTION_CREATE.concat(" " + fileTypes.get(position).mimetype), 1,
                                false);
                        Bundle b = getArguments();
                        b.putSerializable(ARGUMENT_DOCUMENT_TYPE, fileTypes.get(position));
                        EditorsDialogFragment dialogft = EditorsDialogFragment.newInstance(b);
                        dialogft.show(getFragmentManager(), EditorsDialogFragment.TAG);
                        materialDialog.dismiss();
                    }
                });

        return builder.show();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INNER CLASS
    // ///////////////////////////////////////////////////////////////////////////
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
                vh.topText.setText(item.nameString);
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
