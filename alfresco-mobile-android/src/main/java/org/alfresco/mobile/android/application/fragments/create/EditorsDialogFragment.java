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
package org.alfresco.mobile.android.application.fragments.create;

import static org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment.ARGUMENT_DOCUMENT_TYPE;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.platform.data.DocumentTypeRecord;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This Fragment is responsible to display the list of editors able to create
 * the document type selected previously. <br/>
 * This fragment works "like" the "createChooser" Android Intent method.
 * 
 * @author Jean Marie Pascal
 */
public class EditorsDialogFragment extends DialogFragment
{
    /** Public Fragment TAG. */
    public static final String TAG = "FileTypePropertiesDialogFragment";

    /**
     * Used for retrieving default storage folder. Value must be a ResolveInfo
     * object.
     */
    public static final String ARGUMENT_EDITOR = "editor";

    /**
     * @param b : must contains ARGUMENT_DOCUMENT_TYPE key/value
     * @return Dialog fragment that lists application able to create the
     *         ARGUMENT_DOCUMENT_TYPE value.
     */
    public static EditorsDialogFragment newInstance(Bundle b)
    {
        EditorsDialogFragment fr = new EditorsDialogFragment();
        fr.setArguments(b);
        return fr;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int title = R.string.create_document_editor_title;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.sdk_list, null);
        ListView lv = (ListView) v.findViewById(R.id.listView);

        final DocumentTypeRecord documentType = (DocumentTypeRecord) getArguments().get(ARGUMENT_DOCUMENT_TYPE);

        // ACTION_VIEW seems to be the only Public Intent to open and
        // 'eventually' edit a document.
        // ACTION_EDIT doesn't work
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File("/sdcard/test" + documentType.extension)), documentType.mimetype);
        final PackageManager mgr = getActivity().getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent, 0);
        Collections.sort(list, new EditorComparator(getActivity(), true));

        if (list.isEmpty())
        {
            // If there's no 3rd party application able to create, we display a
            // warning message.
            lv.setVisibility(View.GONE);
            v.findViewById(R.id.empty).setVisibility(View.VISIBLE);
            ((TextView) v.findViewById(R.id.empty_text))
                    .setText(R.string.create_document_editor_not_available_description);
            title = R.string.create_document_editor_not_available;

            return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v)
                    .setPositiveButton(R.string.open_play_store, new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            ActionUtils.actionDisplayPlayStore(getActivity());
                        }
                    }).create();
        }
        else
        {
            lv.setAdapter(new EditorAdapter(getActivity(), R.layout.sdk_list_row, list));

            lv.setOnItemClickListener(new OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> l, View v, int position, long id)
                {
                    Bundle b = getArguments();
                    b.putParcelable(ARGUMENT_EDITOR, (ResolveInfo) l.getItemAtPosition(position));
                    DocumentPropertiesDialogFragment dialogft = DocumentPropertiesDialogFragment.newInstance(b);
                    dialogft.show(getFragmentManager(), DocumentPropertiesDialogFragment.TAG);

                    dismiss();
                }
            });
            return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v).create();
        }

    }

    private static String getLabelString(Context context, ResolveInfo item)
    {
        if (item.activityInfo.labelRes != 0)
        {
            return (String) item.activityInfo.loadLabel(context.getPackageManager());
        }
        else
        {
            return (String) item.activityInfo.applicationInfo.loadLabel(context.getPackageManager());
        }
    }

    /**
     * Inner class responsible to manage the list of Editors.
     */
    private static class EditorAdapter extends BaseListAdapter<ResolveInfo, GenericViewHolder>
    {

        public EditorAdapter(Activity context, int textViewResourceId, List<ResolveInfo> listItems)
        {
            super(context, textViewResourceId, listItems);
        }

        @Override
        protected void updateTopText(GenericViewHolder vh, ResolveInfo item)
        {
            if (item.activityInfo.labelRes != 0)
            {
                vh.topText.setText(item.activityInfo.loadLabel(getContext().getPackageManager()));
            }
            else
            {
                vh.topText.setText(item.activityInfo.applicationInfo.loadLabel(getContext().getPackageManager()));
            }
        }

        @Override
        protected void updateBottomText(GenericViewHolder vh, ResolveInfo item)
        {
            vh.bottomText.setVisibility(View.GONE);
        }

        @Override
        protected void updateIcon(GenericViewHolder vh, ResolveInfo item)
        {
            if (item.activityInfo.icon != 0)
            {
                vh.icon.setImageDrawable(item.activityInfo.loadIcon(getContext().getPackageManager()));
            }
            else
            {
                vh.icon.setImageDrawable(item.activityInfo.applicationInfo.loadIcon(getContext().getPackageManager()));
            }
        }
    }

    private static class EditorComparator implements Serializable, Comparator<ResolveInfo>
    {
        private static final long serialVersionUID = 1L;

        private boolean asc;

        private WeakReference<Context> contextRef;

        public EditorComparator(Context context, boolean asc)
        {
            super();
            this.asc = asc;
            this.contextRef = new WeakReference<Context>(context.getApplicationContext());
        }

        public int compare(ResolveInfo infoA, ResolveInfo infoB)
        {
            if (infoA == null || infoB == null) { return 0; }

            int b = 0;
            b = getLabelString(contextRef.get(), infoA).compareToIgnoreCase(getLabelString(contextRef.get(), infoB));
            if (asc)
            {
                return b;
            }
            else
            {
                return -b;
            }
        }
    }
}
