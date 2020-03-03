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
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.SingleLineViewHolder;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

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

    private List<ResolveInfo> list;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
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

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AnalyticsHelper.reportScreen(getActivity(), AnalyticsManager.SCREEN_NODE_CREATE_EDITOR);

        int title = R.string.create_document_editor_title;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.sdk_list, null);
        ListView lv = (ListView) v.findViewById(R.id.listView);

        final DocumentTypeRecord documentType = (DocumentTypeRecord) getArguments().get(ARGUMENT_DOCUMENT_TYPE);

        // ACTION_VIEW seems to be the only Public Intent to open and
        // 'eventually' edit a document.
        // ACTION_EDIT doesn't work
        Intent intent = new Intent(Intent.ACTION_VIEW);

        File myFile = new File("/sdcard/test" + documentType.extension);
        Uri data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            data = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", myFile);
        } else {
            data = Uri.fromFile(myFile);
        }

        intent.setDataAndType(data, documentType.mimetype);
        final PackageManager mgr = getActivity().getPackageManager();
        list = mgr.queryIntentActivities(intent, 0);
        Collections.sort(list, new EditorComparator(getActivity(), true));

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .iconRes(R.drawable.ic_application_logo);

        if (list.isEmpty())
        {
            // If there's no 3rd party application able to create, we display a
            // warning message.
            return builder.title(R.string.create_document_editor_not_available)
                    .content(R.string.create_document_editor_not_available_description)
                    .positiveText(R.string.open_play_store).callback(new MaterialDialog.ButtonCallback()
                    {
                        @Override
                        public void onPositive(MaterialDialog dialog)
                        {
                            ActionUtils.actionDisplayPlayStore(getActivity());
                        }
                    }).show();
        }
        else
        {
            return builder.title(title).adapter(new EditorAdapter(getActivity(), R.layout.row_single_line, list),
                    new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int position,
                                                CharSequence charSequence) {
                            Bundle b = getArguments();
                            b.putParcelable(ARGUMENT_EDITOR, list.get(position));
                            DocumentPropertiesDialogFragment dialogft = DocumentPropertiesDialogFragment.newInstance(b);
                            dialogft.show(getFragmentManager(), DocumentPropertiesDialogFragment.TAG);

                            materialDialog.dismiss();
                        }
                    }).show();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TOOLS
    // ///////////////////////////////////////////////////////////////////////////
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

    // ///////////////////////////////////////////////////////////////////////////
    // INNER CLASS
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Inner class responsible to manage the list of Editors.
     */
    private static class EditorAdapter extends BaseListAdapter<ResolveInfo, SingleLineViewHolder>
    {

        public EditorAdapter(FragmentActivity context, int textViewResourceId, List<ResolveInfo> listItems)
        {
            super(context, textViewResourceId, listItems);
            this.vhClassName = SingleLineViewHolder.class.getCanonicalName();
        }

        @Override
        protected void updateTopText(SingleLineViewHolder vh, ResolveInfo item)
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
        protected void updateBottomText(SingleLineViewHolder vh, ResolveInfo item)
        {
        }

        @Override
        protected void updateIcon(SingleLineViewHolder vh, ResolveInfo item)
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
