/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.ui.widget;

import org.alfresco.mobile.android.application.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Simplest custom view possible, using CircularProgressDrawable
 */
public class CircularIndeterminateProgressView extends View
{

    private CircularProgressDrawable mDrawable;

    public CircularIndeterminateProgressView(Context context)
    {
        this(context, null);
    }

    public CircularIndeterminateProgressView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public CircularIndeterminateProgressView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mDrawable = new CircularProgressDrawable(getContext().getResources().getColor(R.color.alfresco_dbp_blue), 25);
        mDrawable.setCallback(this);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility)
    {
        super.onVisibilityChanged(changedView, visibility);
        if (mDrawable == null) { return; }
        if (visibility == VISIBLE)
        {
            mDrawable.start();
        }
        else
        {
            mDrawable.stop();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawable.setBounds(0, 0, w, h);
    }

    @Override
    public void draw(Canvas canvas)
    {
        super.draw(canvas);
        mDrawable.draw(canvas);
    }

    @Override
    protected boolean verifyDrawable(Drawable who)
    {
        return who == mDrawable || super.verifyDrawable(who);
    }
}