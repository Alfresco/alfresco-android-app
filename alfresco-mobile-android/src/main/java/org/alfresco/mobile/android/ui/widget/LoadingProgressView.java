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
package org.alfresco.mobile.android.ui.widget;

import org.alfresco.mobile.android.foundation.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;

public class LoadingProgressView extends ImageView
{

    private Animation staggered;

    public LoadingProgressView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setAnimation(attrs);
    }

    public LoadingProgressView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setAnimation(attrs);
    }

    public LoadingProgressView(Context context)
    {
        super(context);
    }

    private void setAnimation(AttributeSet attrs)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingProgressView);
        int frameCount = a.getInt(R.styleable.LoadingProgressView_frameCount, 12);
        int duration = a.getInt(R.styleable.LoadingProgressView_duration, 1000);
        a.recycle();

        setAnimation(frameCount, duration);
    }

    public void setAnimation(final int frameCount, final int duration)
    {
        Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.progress_anim);
        a.setDuration(duration);
        a.setInterpolator(new Interpolator()
        {

            @Override
            public float getInterpolation(float input)
            {
                return (float) Math.floor(input * frameCount) / frameCount;
            }
        });
        staggered = a;
        // startAnimation(a);
    }

    @Override
    public void setVisibility(int visibility)
    {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE)
            startAnimation(staggered);
        else
            clearAnimation();

    }
}