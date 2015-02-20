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
package org.alfresco.mobile.android.application.ui.form.views;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.validation.ValidationRule;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class AlfrescoFieldView extends LinearLayout
{
    protected AttributeSet mAttrs;

    protected Context mContext;

    // -----------------------------------------------------------------------
    // ATTRIBUTES
    protected int mCurrentApiVersion = android.os.Build.VERSION.SDK_INT, mFocusedColor, mUnFocusedColor, mErrorColor,
            mFitScreenWidth, mGravity;

    protected float mTextSizeInSp;

    protected float mFloatTextSizeInSp;

    protected String mHintText, mContentDescription, mText, mErrorText;

    // -----------------------------------------------------------------------
    // FLAGS
    protected boolean mIsPassword = false;

    protected boolean mIsMandatory = false;

    protected boolean mIsReadOnly = false;

    // -----------------------------------------------------------------------
    // VIEWS
    protected TextView mFloatingLabel;

    protected View mEditTextUnderlineView;

    protected TextView mErrorTextView;

    // -----------------------------------------------------------------------
    // RULES
    protected List<ValidationRule> mValidationRules;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoFieldView(Context context)
    {
        super(context);
        mContext = context;
        initializeView();
    }

    public AlfrescoFieldView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        initializeView();
    }

    public AlfrescoFieldView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        mAttrs = attrs;
        initializeView();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void setMandatory(boolean isMandatory)
    {
        mIsMandatory = isMandatory;
        setupUnderline();
        mFloatingLabel.setText(mIsMandatory ? mHintText.concat(" * ") : mHintText);
    }

    public void setReadOnly(boolean isReadOnly)
    {
        mIsReadOnly = isReadOnly;
        if (isReadOnly)
        {
            mEditTextUnderlineView.setBackgroundResource(R.drawable.dotted_line);
        }
        mEditTextUnderlineView.setEnabled(!isReadOnly);
    }

    public void setValidationRules(List<ValidationRule> rules)
    {
        if (rules == null || rules.isEmpty())
        {
            mValidationRules = new ArrayList<ValidationRule>(0);
            return;
        }
        mValidationRules = rules;
    }

    // -----------------------------------------------------------------------
    // Content Description
    public void setContentDescription(String contentDescription)
    {
        mContentDescription = contentDescription;
    }

    // -----------------------------------------------------------------------
    // HINT
    public void setHint(String hintText)
    {
        mHintText = hintText;
        mFloatingLabel.setText(hintText);
    }

    public void setHintOnly(String hintText)
    {
        mHintText = hintText;
        mFloatingLabel.setText(hintText);
        mEditTextUnderlineView.setVisibility(View.GONE);
    }

    // -----------------------------------------------------------------------
    // ERRORS
    protected String hasValidationError()
    {
        if (mValidationRules == null) { return null; }
        for (ValidationRule rule : mValidationRules)
        {
            if (rule == null || getValue() == null)
            {
                continue;
            }
            if (!rule.isValid(getValue())) { return rule.getErrorMessage(); }
        }
        return null;
    }

    public void hideError()
    {
        mErrorText = null;
        setupUnderline();
        mErrorTextView.setText(null);
        ((View) mErrorTextView.getParent()).setVisibility(View.INVISIBLE);
    }

    public void setError(String errorText)
    {
        mErrorText = errorText;
        mErrorTextView.setText(mErrorText);
        mErrorTextView.setTextColor(mErrorColor);
        ((View) mErrorTextView.getParent()).setVisibility(View.VISIBLE);
        setupUnderline();
    }

    public void setText(String text)
    {
        if (text == null) { return; }
        mText = text;
    }

    public void clear()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEWS
    // ///////////////////////////////////////////////////////////////////////////
    protected abstract void initializeView();

    protected void getAttributesFromXmlAndStoreLocally()
    {
        TypedArray attributesFromXmlLayout = mContext.obtainStyledAttributes(mAttrs, R.styleable.FloatLabelEditText);
        if (attributesFromXmlLayout == null) { return; }

        mHintText = attributesFromXmlLayout.getString(R.styleable.FloatLabelEditText_hint);
        mText = attributesFromXmlLayout.getString(R.styleable.FloatLabelEditText_text);
        mGravity = attributesFromXmlLayout.getInt(R.styleable.FloatLabelEditText_gravity, Gravity.LEFT);
        mTextSizeInSp = getScaledFontSize(attributesFromXmlLayout.getDimensionPixelSize(
                R.styleable.FloatLabelEditText_textSize, 16));

        mFloatTextSizeInSp = getScaledFontSize(attributesFromXmlLayout.getDimensionPixelSize(
                R.styleable.FloatLabelEditText_floatTextSize, 16));

        mFocusedColor = attributesFromXmlLayout.getColor(R.styleable.FloatLabelEditText_textColorHintFocused,
                android.R.color.black);
        mUnFocusedColor = attributesFromXmlLayout.getColor(R.styleable.FloatLabelEditText_textColorHintUnFocused,
                android.R.color.darker_gray);
        mErrorColor = attributesFromXmlLayout.getColor(R.styleable.FloatLabelEditText_textColorHintError,
                R.color.dark_red);
        mFitScreenWidth = attributesFromXmlLayout.getInt(R.styleable.FloatLabelEditText_fitScreenWidth, 0);
        mIsPassword = (attributesFromXmlLayout.getInt(R.styleable.FloatLabelEditText_inputType, 0) == 1);
        attributesFromXmlLayout.recycle();
    }

    protected void setupFloatingLabel()
    {
        mFloatingLabel.setText(mIsMandatory ? mHintText.concat(" * ") : mHintText);
        mFloatingLabel.setTextColor(mUnFocusedColor);
        mFloatingLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, mFloatTextSizeInSp);
        mFloatingLabel.setGravity(mGravity);
        mFloatingLabel.setPadding(0, 0, 0, 0);

        if (getValue() != null)
        {
            showFloatingLabel();
        }
    }

    protected void setupUnderline()
    {
        // Enable / Disable
        if (mEditTextUnderlineView.isEnabled())
        {
            mEditTextUnderlineView.setBackgroundColor(mUnFocusedColor);
        }
        else
        {
            mEditTextUnderlineView.setBackgroundResource(R.drawable.dotted_line);
        }

        // Override if error
        if (mErrorText != null || mIsMandatory && TextUtils.isEmpty(getValue()))
        {
            mEditTextUnderlineView.setBackgroundColor(mErrorColor);
        }
    }

    protected float getScaledFontSize(float fontSizeFromAttributes)
    {
        float scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
        return fontSizeFromAttributes / scaledDensity;
    }

    protected void showFloatingLabel()
    {
        mFloatingLabel.setVisibility(VISIBLE);
        mFloatingLabel.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.floatlabel_slide_from_bottom));
    }

    protected void hideFloatingLabel()
    {
        mFloatingLabel.setVisibility(INVISIBLE);
        mFloatingLabel.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.floatlabel_slide_to_bottom));
    }

    protected OnFocusChangeListener getFocusChangeListener()
    {
        return new OnFocusChangeListener()
        {

            ValueAnimator mFocusToUnfocusAnimation, mUnfocusToFocusAnimation;

            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                ValueAnimator lColorAnimation;

                if (hasFocus)
                {
                    lColorAnimation = getFocusToUnfocusAnimation();
                }
                else
                {
                    lColorAnimation = getUnfocusToFocusAnimation();
                }

                lColorAnimation.setDuration(700);
                lColorAnimation.start();
            }

            private ValueAnimator getFocusToUnfocusAnimation()
            {
                if (mFocusToUnfocusAnimation == null)
                {
                    mFocusToUnfocusAnimation = getFocusAnimation(mUnFocusedColor, mFocusedColor);
                }
                return mFocusToUnfocusAnimation;
            }

            private ValueAnimator getUnfocusToFocusAnimation()
            {
                if (mIsMandatory)
                {
                    // Change color to flag an error
                    if (mErrorText != null || getValue() == null)
                    {
                        mUnfocusToFocusAnimation = getFocusAnimation(mFocusedColor, mErrorColor);
                    }
                    else
                    {
                        mUnfocusToFocusAnimation = getFocusAnimation(mFocusedColor, mUnFocusedColor);
                    }
                }
                else if (mUnfocusToFocusAnimation == null)
                {
                    mUnfocusToFocusAnimation = getFocusAnimation(mFocusedColor, mUnFocusedColor);
                }
                return mUnfocusToFocusAnimation;
            }
        };
    }

    protected ValueAnimator getFocusAnimation(int fromColor, int toColor)
    {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {

            @Override
            public void onAnimationUpdate(ValueAnimator animator)
            {
                if (mErrorText == null)
                {
                    mFloatingLabel.setTextColor((Integer) animator.getAnimatedValue());
                    if (mEditTextUnderlineView.isEnabled())
                    {
                        mEditTextUnderlineView.setBackgroundColor((Integer) animator.getAnimatedValue());
                    }
                }
                else
                {
                    // TODO Error
                }
            }
        });
        return colorAnimation;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ABSTRACTS
    // ///////////////////////////////////////////////////////////////////////////
    public abstract String getValue();
}
