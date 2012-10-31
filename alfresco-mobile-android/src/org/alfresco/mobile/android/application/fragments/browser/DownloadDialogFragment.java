package org.alfresco.mobile.android.application.fragments.browser;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.DownloadTask;
import org.alfresco.mobile.android.api.asynchronous.DownloadTask.DownloadTaskListener;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.EmailUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.manager.ActionManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class DownloadDialogFragment extends DialogFragment implements DownloadTaskListener
{
    public static final String ARGUMENT_DOCUMENT = "document";

    public static final String ARGUMENT_ACTION = "action";

    public static final int ACTION_OPEN = 1;

    public static final int ACTION_EMAIL = 2;

    public static final int ACTION_UNDEFINED = 0;

    public static final String TAG = "DownloadDialogFragment";

    private ProgressDialog dialog;

    private Document doc;

    private long totalSize;

    private DownloadTask dlt;

    private ContentFile contentFile;

    private int action = ACTION_UNDEFINED;

    public static DownloadDialogFragment newInstance()
    {
        DownloadDialogFragment d = new DownloadDialogFragment();
        return d;
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        setRetainInstance(true);
        if (contentFile != null)
        {
            executeAction();
        }

        if (getArguments().containsKey(ARGUMENT_DOCUMENT))
        {
            doc = getArguments().getParcelable(ARGUMENT_DOCUMENT);
        }
        else
        {
            return null;
        }

        if (getArguments().containsKey(ARGUMENT_ACTION))
        {
            action = getArguments().getInt(ARGUMENT_ACTION);
        }

        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getString(R.string.download) + " : " + doc.getName());
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage(getString(R.string.download_progress));
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        dialog.setProgress(0);
        dialog.setMax(100);

        if (dlt == null)
        {
            totalSize = doc.getContentStreamLength();
            dlt = new DownloadTask(SessionUtils.getSession(getActivity()), doc, getDownloadFile());
            dlt.setDl(this);
            dlt.execute();
        }
        else
        {
            dialog.show();
        }

        return dialog;
    }

    private File getDownloadFile()
    {
        File folder = StorageManager.getDownloadFolder(getActivity(), SessionUtils.getAccount(getActivity()).getUrl()
                + "", SessionUtils.getAccount(getActivity()).getUsername());
        File tmpFile = new File(folder, doc.getName());
        IOUtils.ensureOrCreatePathAndFile(tmpFile);
        return tmpFile;
    }

    @Override
    public void onPreExecute()
    {
    }

    @Override
    public void onProgressUpdate(Integer... values)
    {
        int percent = Math.round(((float) values[0] / totalSize) * 100);
        dialog.setProgress(percent);
    }

    @Override
    public void onPostExecute(ContentFile results)
    {
        contentFile = results;
        if (getActivity() != null)
        {
            executeAction();
        }
    }

    private void executeAction()
    {
        if (contentFile != null && contentFile.getFile() != null)
        {
            switch (action)
            {
                case ACTION_OPEN:
                    Log.d(TAG, getActivity().toString());
                    Log.d(TAG, contentFile + "");

                    MessengerManager.showToast(getActivity(), getActivity().getText(R.string.download_complete)
                            + contentFile.getFileName());
                    ActionManager.openIn(getFragmentManager().findFragmentByTag(DetailsFragment.TAG),
                            contentFile.getFile(), doc.getContentStreamMimeType(), PublicIntent.REQUESTCODE_SAVE_BACK);
                    break;

                case ACTION_EMAIL:
                    EmailUtils.createMailWithAttachment(getActivity(), contentFile.getFileName(), getFragmentManager()
                            .findFragmentByTag(DetailsFragment.TAG).getActivity().getString(R.string.email_content),
                            Uri.fromFile(contentFile.getFile()));
                    break;

                case ACTION_UNDEFINED:
                    break;
            }
        }
        else
        {
            MessengerManager.showToast(getActivity(), getActivity().getText(R.string.download_complete_error)
                    .toString());
        }
        dismiss();
    }

    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance())
        {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        if (dlt != null)
        {
            dlt.cancel(false);
        }
    }
}
