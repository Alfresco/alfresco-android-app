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
package org.alfresco.mobile.android.application.fragments.workflow;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.alfresco.mobile.android.platform.utils.AndroidVersion;

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

    private static final String ARGUMENT_FRAGMENT_TAG = "fragmentTag";

    private static final String ARGUMENT_DATE_ID = "dateId";

    private OnDateSetListener mListener;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // //////////////////////////////////////////////////////////////////////
    public static DatePickerFragment newInstance(int dateId, String fragmentTag)
    {
        DatePickerFragment bf = new DatePickerFragment();
        Bundle b = new Bundle();
        b.putInt(ARGUMENT_DATE_ID, dateId);
        b.putString(ARGUMENT_FRAGMENT_TAG, fragmentTag);
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
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

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    private OnDateSetListener getConstructorListener()
    {
        return AndroidVersion.isJBOrAbove() ? null : mListener;
    }

    public void onDateSet(DatePicker view, int year, int month, int day)
    {

        if (getArguments() != null && getArguments().containsKey(ARGUMENT_FRAGMENT_TAG))
        {
            String pickFragmentTag = getArguments().getString(ARGUMENT_FRAGMENT_TAG);
            int dateId = getArguments().getInt(ARGUMENT_DATE_ID);
            onPickDateFragment fragmentPick = ((onPickDateFragment) getFragmentManager().findFragmentByTag(
                    pickFragmentTag));
            if (fragmentPick != null)
            {
                fragmentPick.onDatePicked(dateId, new GregorianCalendar(year, month, day, 0, 0, 0));
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERFACE
    // //////////////////////////////////////////////////////////////////////
    public interface onPickDateFragment
    {
        void onDatePicked(int dateId, GregorianCalendar gregorianCalendar);
    }

}