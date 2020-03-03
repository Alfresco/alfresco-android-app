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
package org.alfresco.mobile.android.application.ui.form.picker;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment.onPickDateFragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.widget.TimePicker;

/**
 * @author jpascal
 */
public class TimePickerFragment extends DialogFragment implements OnTimeSetListener
{
    public static final String TAG = TimePickerFragment.class.getName();

    private static final String ARGUMENT_FRAGMENT_TAG = "fragmentTag";

    private static final String ARGUMENT_DATE_ID = "dateId";

    private static final String ARGUMENT_DATE = "date";

    private OnTimeSetListener mListener;

    private boolean isCancelled = false, clearValue = false;

    private GregorianCalendar calendar;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // //////////////////////////////////////////////////////////////////////
    public static TimePickerFragment newInstance(String dateId, String fragmentTag)
    {
        TimePickerFragment bf = new TimePickerFragment();
        Bundle b = new Bundle();
        b.putString(ARGUMENT_DATE_ID, dateId);
        b.putString(ARGUMENT_FRAGMENT_TAG, fragmentTag);
        bf.setArguments(b);
        return bf;
    }

    public static TimePickerFragment newInstance(String dateId, String fragmentTag, GregorianCalendar calendar)
    {
        TimePickerFragment bf = new TimePickerFragment();
        Bundle b = new Bundle();
        b.putString(ARGUMENT_DATE_ID, dateId);
        b.putString(ARGUMENT_FRAGMENT_TAG, fragmentTag);
        b.putSerializable(ARGUMENT_DATE, calendar);
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
        this.mListener = this;
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
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        if (getArguments() != null && getArguments().containsKey(ARGUMENT_DATE))
        {
            calendar = (GregorianCalendar) getArguments().get(ARGUMENT_DATE);
        }

        final TimePickerDialog picker = new TimePickerDialog(getActivity(), mListener, hourOfDay, minute, true);

        picker.setButton(DialogInterface.BUTTON_POSITIVE, getActivity().getString(android.R.string.ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dismiss();
                    }
                });
        picker.setButton(DialogInterface.BUTTON_NEUTRAL, getActivity().getString(R.string.clear),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        clearValue = true;
                        dismiss();
                    }
                });
        picker.setButton(DialogInterface.BUTTON_NEGATIVE, getActivity().getString(R.string.cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        isCancelled = true;
                        dismiss();
                    }
                });
        return picker;

    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        if (getArguments() == null || !getArguments().containsKey(ARGUMENT_FRAGMENT_TAG)) { return; }

        String pickFragmentTag = getArguments().getString(ARGUMENT_FRAGMENT_TAG);
        String dateId = getArguments().getString(ARGUMENT_DATE_ID);
        onPickTimeFragment fragmentPick = ((onPickTimeFragment) getFragmentManager().findFragmentByTag(pickFragmentTag));
        if (fragmentPick == null || isCancelled) { return; }

        if (clearValue)
        {
            fragmentPick.onTimeClear(dateId);
        }
        else
        {
            if (calendar != null && fragmentPick instanceof onPickDateFragment)
            {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                ((onPickDateFragment) fragmentPick).onDatePicked(dateId, calendar);
            }
            else
            {
                fragmentPick.onTimePicked(dateId, hourOfDay, minute);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERFACE
    // //////////////////////////////////////////////////////////////////////
    public interface onPickTimeFragment
    {
        void onTimePicked(String dateId, int hourOfDay, int minute);

        void onTimeClear(String dateId);
    }
}