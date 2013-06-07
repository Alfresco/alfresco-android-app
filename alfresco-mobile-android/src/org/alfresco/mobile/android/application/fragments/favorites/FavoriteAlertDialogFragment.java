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
package org.alfresco.mobile.android.application.fragments.favorites;

import org.alfresco.mobile.android.application.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

public class FavoriteAlertDialogFragment extends DialogFragment
{
    public static final String TAG = FavoriteAlertDialogFragment.class.getName();

    private onFavoriteChangeListener onFavoriteChangeListener;

    public FavoriteAlertDialogFragment()
    {
    }

    public static FavoriteAlertDialogFragment newInstance(onFavoriteChangeListener listener)
    {
        FavoriteAlertDialogFragment frag = new FavoriteAlertDialogFragment();
        frag.setOnFavoriteChangeListener(listener);
        return frag;
    }

    private void setOnFavoriteChangeListener(onFavoriteChangeListener listener)
    {
        this.onFavoriteChangeListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int titleId = R.string.favorites_deactivate;
        int iconId = R.drawable.ic_alfresco_logo;
        int positiveId = android.R.string.yes;
        int messageId = R.string.favorites_deactivate_description;
        int negativeId = android.R.string.no;

        Builder builder = new AlertDialog.Builder(getActivity()).setIcon(iconId).setTitle(titleId)
                .setMessage(Html.fromHtml(getString(messageId))).setCancelable(false)
                .setPositiveButton(positiveId, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        if (onFavoriteChangeListener != null)
                        {
                            onFavoriteChangeListener.onPositive();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton(negativeId, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        if (onFavoriteChangeListener != null)
                        {
                            onFavoriteChangeListener.onNegative();
                        }
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public interface onFavoriteChangeListener
    {
        public void onPositive();

        public void onNegative();
    }

    @Override
    public void onResume()
    {
        if (getDialog() != null)
        {
            TextView messageText = (TextView) getDialog().findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            getDialog().show();
        }
        super.onResume();
    }
}