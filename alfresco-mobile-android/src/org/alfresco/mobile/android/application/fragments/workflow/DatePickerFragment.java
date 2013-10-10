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
package org.alfresco.mobile.android.application.fragments.workflow;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.alfresco.mobile.android.application.commons.utils.AndroidVersion;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

/**
 * @author jpascal
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
{
    public static final String TAG = DatePickerFragment.class.getName();

    private OnDateSetListener mListener;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.mListener = (OnDateSetListener) this;
    }

    @Override
    public void onDetach()
    {
        this.mListener = null;
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog picker = new DatePickerDialog(getActivity(), getConstructorListener(), year, month, day);

        if (AndroidVersion.isJBOrAbove())
        {
            picker.setButton(DialogInterface.BUTTON_POSITIVE, getActivity().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            DatePicker dp = picker.getDatePicker();
                            mListener.onDateSet(dp, dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                        }
                    });
            picker.setButton(DialogInterface.BUTTON_NEGATIVE, getActivity().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                        }
                    });
        }
        return picker;

    }

    private OnDateSetListener getConstructorListener()
    {
        return AndroidVersion.isJBOrAbove() ? null : mListener;
    }

    public void onDateSet(DatePicker view, int year, int month, int day)
    {
        CreateTaskFragment sf = (CreateTaskFragment) getFragmentManager().findFragmentByTag(
                CreateTaskFragment.TAG);
        sf.setDueAt(new GregorianCalendar(year, month, day));
    }
}