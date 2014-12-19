package org.alfresco.mobile.android.application.ui.form.views;

import org.alfresco.mobile.android.application.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@TargetApi(11)
public class PickerFieldView extends AlfrescoFieldView
{
    private TextView mTextView;

    private LinearLayout mSpinner;

    private ImageView mExpand;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public PickerFieldView(Context context)
    {
        super(context);
    }

    public PickerFieldView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PickerFieldView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    // -----------------------------------------------------------------------
    // public interface

    public TextView getTextView()
    {
        return mTextView;
    }

    public String getText()
    {
        if (getEditTextString() != null && getEditTextString().toString() != null
                && getEditTextString().toString().length() > 0) { return getEditTextString().toString(); }
        return "";
    }

    public void setOnClickListener(OnClickListener onClickListener)
    {
        mSpinner.setOnClickListener(onClickListener);
    }

    public void setHint(String hintText)
    {
        super.setHint(hintText);
        setupTextView();
    }

    public void setText(String text)
    {
        super.setText(text);
        mTextView.setText(text);
    }

    public void setReadOnly(boolean isReadOnly)
    {
        super.setReadOnly(isReadOnly);
        mTextView.setEnabled(!isReadOnly);

        // Hide expand if read only.
        if (isReadOnly)
        {
            mExpand.setVisibility(View.GONE);
        }
        else
        {
            mExpand.setVisibility(View.VISIBLE);
        }
    }

    // -----------------------------------------------------------------------
    // private helpers

    protected void initializeView()
    {
        if (mContext == null) { return; }

        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.form_field_picker, this, true);

        mFloatingLabel = (TextView) findViewById(R.id.floating_label_hint);
        mTextView = (TextView) findViewById(R.id.floating_label_edit_text);
        mEditTextUnderlineView = (View) findViewById(R.id.floating_label_underline);
        mErrorTextView = (TextView) findViewById(R.id.floating_label_error);
        mSpinner = (LinearLayout) findViewById(R.id.floating_spinner);
        mExpand = (ImageView) findViewById(R.id.floating_label_expand);

        getAttributesFromXmlAndStoreLocally();
        setupTextView();
        setupUnderline();
        setupFloatingLabel();
    }

    private void setupTextView()
    {
        if (mIsPassword)
        {
            mTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mTextView.setTypeface(Typeface.DEFAULT);
        }

        mTextView.setHint(mHintText);
        mTextView.setHintTextColor(mUnFocusedColor);
        mTextView.setText(mText);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSizeInSp);
        mTextView.addTextChangedListener(getTextWatcher());
        if (mEditTextUnderlineView.isEnabled())
        {
            mEditTextUnderlineView.setBackgroundColor(mUnFocusedColor);
        }
        else
        {
            mEditTextUnderlineView.setBackgroundResource(R.drawable.dotted_line);
        }

        if (mCurrentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
            mTextView.setOnFocusChangeListener(getFocusChangeListener());
        }
    }

    private TextWatcher getTextWatcher()
    {
        return new TextWatcher()
        {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0 && mFloatingLabel.getVisibility() == INVISIBLE)
                {
                    showFloatingLabel();
                }
                else if (s.length() == 0 && mFloatingLabel.getVisibility() == VISIBLE)
                {
                    hideFloatingLabel();
                }
            }
        };
    }

    protected Editable getEditTextString()
    {
        return mTextView.getEditableText();
    }

    @Override
    public String getValue()
    {
        String value = getText();
        if (TextUtils.isEmpty(value) || TextUtils.isEmpty(value.trim())) { return null; }
        return getText().trim();
    }
}
