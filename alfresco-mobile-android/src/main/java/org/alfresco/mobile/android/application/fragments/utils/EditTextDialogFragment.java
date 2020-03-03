/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco Activiti Mobile for Android.
 *
 * Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.alfresco.mobile.android.application.fragments.utils;

import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class EditTextDialogFragment extends DialogFragment
{
    private static final String ARGUMENT_VALUE = "txtValue";

    private static final String ARGUMENT_FRAGMENT_TAG = "fragmentTag";

    private static final String ARGUMENT_FIELD_ID = "fieldId";

    private static final String ARGUMENT_HINT_ID = "hintId";

    private static final String ARGUMENT_EMPTY = "notNull";

    private static final String ARGUMENT_SINGLE_LINE = "notNull";

    protected onEditTextFragment fragmentPick;

    protected Integer fieldId;

    protected EditText et;

    protected int hintId;

    protected boolean notNull, singleLine = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public EditTextDialogFragment()
    {
        super();
    }

    public static EditTextDialogFragment newInstanceByTemplate(Bundle b)
    {
        EditTextDialogFragment cbf = new EditTextDialogFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getArguments() != null)
        {
            notNull = getArguments().getBoolean(ARGUMENT_EMPTY);
            fieldId = getArguments().getInt(ARGUMENT_FIELD_ID);
        }

        String pickFragmentTag = getArguments().getString(ARGUMENT_FRAGMENT_TAG);
        fragmentPick = ((onEditTextFragment) getFragmentManager().findFragmentByTag(pickFragmentTag));

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .cancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        dismiss();
                    }
                }).customView(R.layout.fr_form_edittext, false).positiveText(R.string.confirm)
                .callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        fragmentPick.onTextEdited(fieldId, et.getText().toString());
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog)
                    {
                        fragmentPick.onTextClear(fieldId);
                    }
                });

        if (!notNull)
        {
            builder.neutralText(R.string.clear);
        }

        return builder.show();
    }

    @Override
    public void onStart()
    {
        String value = null;
        if (getArguments() != null)
        {
            value = getArguments().getString(ARGUMENT_VALUE);
            hintId = getArguments().getInt(ARGUMENT_HINT_ID);
            notNull = getArguments().getBoolean(ARGUMENT_EMPTY);
            singleLine = getArguments().getBoolean(ARGUMENT_SINGLE_LINE);
        }
        super.onStart();
        if (getDialog() != null)
        {
            et = ((EditText) getDialog().getWindow().findViewById(R.id.text_value));
            if (singleLine)
            {
                et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT);
                et.setSingleLine(true);
                et.setMinLines(1);
                et.setMaxLines(1);
                et.setLines(1);
            }
            et.setText(value);
            if (hintId != 0)
            {
                et.setHint(hintId);
            }
            et.setSelection(et.getText().length());
            if (notNull)
            {
                et.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count)
                    {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after)
                    {
                    }

                    @Override
                    public void afterTextChanged(Editable s)
                    {
                        if (s.length() == 0)
                        {
                            ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(false);
                            // ((MaterialDialog)getDialog()).getActionButton(DialogAction.POSITIVE).setBackgroundColor(getResources().getColor(R.color.primary_background));
                        }
                        else
                        {
                            ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(true);
                            // ((MaterialDialog)getDialog()).getActionButton(DialogAction.POSITIVE).setBackgroundColor(getResources().getColor(R.color.primary));
                            // backField.setBackgroundColor(getResources().getColor(R.color.primary));
                        }
                    }
                });
            }
            UIUtils.showKeyboard(getActivity(), et);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        UIUtils.showKeyboard(getActivity(), et);
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERFACE
    // //////////////////////////////////////////////////////////////////////
    public interface onEditTextFragment
    {
        void onTextEdited(int valueId, String newValue);

        void onTextClear(int valueId);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        public Builder value(String value)
        {
            extraConfiguration.putString(ARGUMENT_VALUE, value);
            return this;
        }

        public Builder tag(String tag)
        {
            extraConfiguration.putString(ARGUMENT_FRAGMENT_TAG, tag);
            return this;
        }

        public Builder singleLine(boolean singleLine)
        {
            extraConfiguration.putBoolean(ARGUMENT_SINGLE_LINE, singleLine);
            return this;
        }

        public Builder fieldId(int fieldId)
        {
            extraConfiguration.putInt(ARGUMENT_FIELD_ID, fieldId);
            return this;
        }

        public Builder hintId(int hintId)
        {
            extraConfiguration.putInt(ARGUMENT_HINT_ID, hintId);
            return this;
        }

        public Builder notNull(boolean notNull)
        {
            extraConfiguration.putBoolean(ARGUMENT_EMPTY, notNull);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }
}
