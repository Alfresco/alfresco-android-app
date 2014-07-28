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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

@TargetApi(11)
public class EditTextFieldView extends AlfrescoFieldView
{
    private EditText mEditTextView;
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public EditTextFieldView(Context context)
    {
        super(context);
    }

    public EditTextFieldView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public EditTextFieldView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public EditText getEditText()
    {
        return mEditTextView;
    }

    public String getText()
    {
        if (getEditTextString() != null && getEditTextString().toString() != null
                && getEditTextString().toString().length() > 0) { return getEditTextString().toString(); }
        return "";
    }

    public String getValue()
    {
        String value = getText();
        if (TextUtils.isEmpty(value) || TextUtils.isEmpty(value.trim())) { return null; }
        return getText().trim();
    }

    public void setHint(String hintText)
    {
        super.setHint(hintText);
        setupEditTextView();
    }

    public void setHintOnly(String hintText)
    {
        super.setHintOnly(hintText);
        setupEditTextView();
    }

    public void setMandatory(boolean isMandatory)
    {
        super.setMandatory(isMandatory);
        setupEditTextView();
    }

    public void setText(String text)
    {
        super.setText(text);
        mEditTextView.setText(text);
    }
    
    public void clear()
    {
        mText = null;
        mEditTextView.setText(null);
    }
    
    public void enablePassword(boolean isPassword){
        mIsPassword = true;
        mEditTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    public void setReadOnly(boolean isReadOnly)
    {
        super.setReadOnly(isReadOnly);
        mEditTextView.setEnabled(!isReadOnly);
    }

    public void setMultiLine(boolean multiLine)
    {
        if (multiLine)
        {
            LinearLayout.LayoutParams params = new LayoutParams(
                    ((LinearLayout) mEditTextView.getParent()).getLayoutParams().width,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ((LinearLayout) mEditTextView.getParent()).setLayoutParams(params);
            mEditTextView.setMaxLines(5);
            mEditTextView.setLines(1);
            mEditTextView.setSingleLine(false);
        }
        else
        {
            mEditTextView.setMaxLines(1);
            mEditTextView.setLines(1);
            mEditTextView.setSingleLine(true);
        }
    }

    // -----------------------------------------------------------------------
    protected void initializeView()
    {
        if (mContext == null) { return; }

        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.form_field_edittext, this, true);

        mFloatingLabel = (TextView) findViewById(R.id.floating_label_hint);
        mEditTextView = (EditText) findViewById(R.id.floating_label_edit_text);
        mEditTextUnderlineView = (View) findViewById(R.id.floating_label_underline);
        mErrorTextView = (TextView) findViewById(R.id.floating_label_error);

        getAttributesFromXmlAndStoreLocally();
        setupEditTextView();
        setupUnderline();
        setupFloatingLabel();
    }

    // private helpers
    private void setupEditTextView()
    {
        if (mIsPassword)
        {
            mEditTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mEditTextView.setTypeface(Typeface.DEFAULT);
        }

        mEditTextView.setHint(mIsMandatory ? mHintText.concat(" " + getContext().getString(R.string.field_required))
                : mHintText);
        mEditTextView.setHintTextColor(mUnFocusedColor);
        mEditTextView.setText(mText);
        mEditTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSizeInSp);
        mEditTextView.addTextChangedListener(getTextWatcher());
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
            mEditTextView.setOnFocusChangeListener(getFocusChangeListener());
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

                if (hasValidationError() != null)
                {
                    setError(hasValidationError());
                }
                else
                {
                    hideError();
                    mEditTextUnderlineView.setBackgroundColor(mFocusedColor);
                }
            }
        };
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private Editable getEditTextString()
    {
        if (mEditTextView == null) { return null; }
        return mEditTextView.getText();
    }

}
