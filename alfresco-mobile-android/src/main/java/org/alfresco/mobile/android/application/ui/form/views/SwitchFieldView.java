package org.alfresco.mobile.android.application.ui.form.views;

import org.alfresco.mobile.android.application.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

@TargetApi(11)
public class SwitchFieldView extends AlfrescoFieldView
{
    private TextView mTextView;

    private Switch mSwitch;

    // -----------------------------------------------------------------------
    // default constructors

    public SwitchFieldView(Context context)
    {
        super(context);
    }

    public SwitchFieldView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SwitchFieldView(Context context, AttributeSet attrs, int defStyle)
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

    public void setHint(String hintText)
    {
        mHintText = hintText;
        mFloatingLabel.setText(hintText);
        setupEditTextView();
    }

    public void setError(String errorText)
    {
        mErrorText = errorText;
        mErrorTextView.setText(mErrorText);
        mErrorTextView.setTextColor(getResources().getColor(R.color.field_error));
        ((View) mErrorTextView.getParent()).setVisibility(View.VISIBLE);
    }

    public void hideError()
    {
        mErrorText = null;
        setupUnderline();
        mErrorTextView.setText(null);
        ((View) mErrorTextView.getParent()).setVisibility(View.INVISIBLE);
    }

    public void setText(String text)
    {
        if (text == null) { return; }
        mText = text;
        mTextView.setText(text);
    }

    public void setEnable(boolean enabled)
    {
        mTextView.setEnabled(enabled);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setChecked(boolean checked)
    {
        mSwitch.setChecked(checked);
    }

    public boolean isChecked()
    {
        return mSwitch.isChecked();
    }

    // -----------------------------------------------------------------------
    // private helpers
    protected void initializeView()
    {

        if (mContext == null) { return; }

        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.form_field_switch, this, true);

        mFloatingLabel = (TextView) findViewById(R.id.floating_label_hint);
        mTextView = (TextView) findViewById(R.id.floating_label_edit_text);
        mErrorTextView = (TextView) findViewById(R.id.floating_label_error);
        mEditTextUnderlineView = findViewById(R.id.floating_label_underline);
        mSwitch = (Switch) findViewById(R.id.floating_label_switch);

        getAttributesFromXmlAndStoreLocally();
        setupEditTextView();
        setupFloatingLabel();
        setupUnderline();
    }

    private void setupEditTextView()
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

        if (mCurrentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
            mTextView.setOnFocusChangeListener(getFocusChangeListener());
        }
    }

    private Editable getEditTextString()
    {
        return mTextView.getEditableText();
    }

    @Override
    public String getValue()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
