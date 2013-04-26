/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.browser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.browser.local.LocalFileBrowserFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
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

public class UploadChooseDialogFragment extends DialogFragment
{
    public static final String TAG = UploadChooseDialogFragment.class.getName();

    private Account currentAccount;

    private String fragmentTag;
    
    private static final String PARAM_ACCOUNT = "account";
    private static final String PARAM_FRAGMENT_TAG = "fragmentTag";


    public static UploadChooseDialogFragment newInstance(Account currentAccount)
    {
        UploadChooseDialogFragment fragment = new UploadChooseDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_ACCOUNT, currentAccount);
        args.putSerializable(PARAM_FRAGMENT_TAG, ChildrenBrowserFragment.TAG);
        fragment.setArguments(args);
        return fragment;
    }

    public static UploadChooseDialogFragment newInstance(Account currentAccount, String fragmentTag)
    {
        UploadChooseDialogFragment fragment = new UploadChooseDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_ACCOUNT, currentAccount);
        args.putSerializable(PARAM_FRAGMENT_TAG, fragmentTag);
        fragment.setArguments(args);
        return fragment;
    }

    private static final List<Integer> DOWNLOAD_FROM_LIST = new ArrayList<Integer>(4)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.string.upload_photos);
            add(R.string.upload_videos);
            add(R.string.upload_files);
            add(R.string.upload_third_application);
        }
    };

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int title = R.string.upload_title;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.sdk_list, null);
        ListView lv = (ListView) v.findViewById(R.id.listView);

        lv.setAdapter(new UploaderAdapter(getActivity(), R.layout.app_list_row, DOWNLOAD_FROM_LIST));

        lv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id)
            {
                Integer itemId = (Integer) l.getItemAtPosition(position);
                
                currentAccount = (Account) getArguments().getSerializable(PARAM_ACCOUNT);
                fragmentTag = getArguments().getString(PARAM_FRAGMENT_TAG);
         
                switch (itemId)
                {
                    case R.string.upload_photos:
                    case R.string.upload_videos:
                    case R.string.upload_files:
                        File f = StorageManager.getDownloadFolder(getActivity(), currentAccount.getUrl(),
                                currentAccount.getUsername());
                        BaseFragment frag = LocalFileBrowserFragment.newInstance(f, LocalFileBrowserFragment.MODE_PICK_FILE,
                                fragmentTag);
                        frag.setSession(SessionUtils.getSession(getActivity()));
                        frag.show(getFragmentManager(), LocalFileBrowserFragment.TAG);

                        break;
                    default:
                        ActionManager.actionPickFile(getFragmentManager().findFragmentByTag(fragmentTag),
                                IntentIntegrator.REQUESTCODE_FILEPICKER);
                        break;
                }
                dismiss();
            }
        });
        return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v).create();

    }

    /**
     * Inner class responsible to manage the list of Editors.
     */
    private static class UploaderAdapter extends BaseListAdapter<Integer, GenericViewHolder>
    {

        public UploaderAdapter(Activity context, int textViewResourceId, List<Integer> listItems)
        {
            super(context, textViewResourceId, listItems);
        }

        @Override
        protected void updateTopText(GenericViewHolder vh, Integer item)
        {
            vh.topText.setText(item);
        }

        @Override
        protected void updateBottomText(GenericViewHolder vh, Integer item)
        {
            vh.bottomText.setVisibility(View.GONE);
        }

        @Override
        protected void updateIcon(GenericViewHolder vh, Integer item)
        {
            vh.choose.setVisibility(View.VISIBLE);
            vh.icon.setVisibility(View.GONE);

            int iconId = R.drawable.ic_share;
            switch (item)
            {
                case R.string.upload_photos:
                    iconId = R.drawable.mime_img;
                    break;
                case R.string.upload_videos:
                    iconId = R.drawable.mime_video;
                    break;
                case R.string.upload_files:
                    iconId = R.drawable.mime_folder;
                    break;
                default:
                    break;
            }
            vh.choose.setImageDrawable(getContext().getResources().getDrawable(iconId));
        }
    }
}
