package org.alfresco.mobile.android.application.fragments.browser;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.browser.local.LocalFileBrowserFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.StorageManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class UploadChooseDialogFragment extends DialogFragment
{
    public static final String TAG = "UploadChooseDialogFragment";

    public static UploadChooseDialogFragment newInstance(Account currentAccount)
    {
        UploadChooseDialogFragment fragment = new UploadChooseDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("account", currentAccount);
        fragment.setArguments(args);
        return fragment;
    }

    private Account currentAccount;

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        currentAccount = (Account) getArguments().get("account");
        
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.upload_from)
                .setPositiveButton(R.string.upload_download_folder, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        File f = StorageManager.getDownloadFolder(getActivity(), currentAccount.getUrl(),
                                currentAccount.getUsername());
                        
                        BaseFragment frag = LocalFileBrowserFragment.newInstance(f, LocalFileBrowserFragment.MODE_PICK_FILE);
                        frag.setSession(SessionUtils.getSession(getActivity()));
                        frag.show(getFragmentManager(), LocalFileBrowserFragment.TAG);
                    }
                }).setNegativeButton(R.string.upload_third_application, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        ActionManager.actionPickFile(getFragmentManager().findFragmentByTag(ChildrenBrowserFragment.TAG),
                                IntentIntegrator.REQUESTCODE_FILEPICKER);
                    }
                }).create();
    }

}
