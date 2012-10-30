package org.alfresco.mobile.android.application.fragments;

import org.alfresco.mobile.android.application.R;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import android.app.DialogFragment;
 
public class WaitingDialogFragment extends DialogFragment
{
    public static final String TAG = "WaitingDialogFragment";
    
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.wait_title);
        dialog.setMessage(getString(R.string.wait_message));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }
}
