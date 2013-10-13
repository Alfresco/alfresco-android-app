/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.extension.samsung.pen;

import org.alfresco.mobile.android.application.commons.utils.UIUtils;
import org.alfresco.mobile.android.application.extension.samsung.impl.R;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SNotePagesDialogFragment extends DialogFragment
{
    public static final String TAG = SNotePagesDialogFragment.class.getName();

    private int totalPages = 1;

    private int currentPageNumber = 1;

    private int originPageNumber = 1;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public SNotePagesDialogFragment()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onStart()
    {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.mime_pages);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        getDialog().setTitle(R.string.editor_pages_move);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.snote_pages, container, false);

        int width = (int) Math
                .round(UIUtils.getScreenDimension(getActivity())[0]
                        * (Float.parseFloat(getResources().getString(android.R.dimen.dialog_min_width_major).replace(
                                "%", "")) * 0.01));
        v.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));

        final SeekBar seekbar = ((SeekBar) v.findViewById(R.id.seekbar_pages));
        final TextView tv = ((TextView) v.findViewById(R.id.pages_number));
        tv.setText(String.valueOf(originPageNumber+1) + " / "+ String.valueOf(totalPages));

        seekbar.setMax(totalPages-1);
        seekbar.setProgress(originPageNumber);
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                currentPageNumber = progress+1;
                tv.setText(String.valueOf(currentPageNumber) + " / "+ String.valueOf(totalPages));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });

        final Button validate = (Button) v.findViewById(R.id.create_document);
        final Button cancel = (Button) v.findViewById(R.id.cancel);

        validate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ((SNoteEditorActivity) getActivity()).movePage(currentPageNumber-1);
                dismiss();
            }
        });

        cancel.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                SNotePagesDialogFragment.this.dismiss();
            }
        });

        return v;
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
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Avoid background stretching
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // SETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public void setInfo(int pageIndexById, int pageCount)
    {
        this.originPageNumber = pageIndexById;
        this.totalPages = pageCount;
    }

}
