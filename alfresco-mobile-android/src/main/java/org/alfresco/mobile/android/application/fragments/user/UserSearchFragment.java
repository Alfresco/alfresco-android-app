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
package org.alfresco.mobile.android.application.fragments.user;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.search.AdvancedSearchFragment;
import org.alfresco.mobile.android.application.fragments.workflow.task.TaskDetailsFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.ui.form.picker.PersonPickerFragment.onPickAuthorityFragment;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.person.PersonsEvent;
import org.alfresco.mobile.android.async.person.PersonsRequest;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.squareup.otto.Subscribe;

/**
 * @since 1.3
 * @author Jean Marie Pascal
 */
public class UserSearchFragment extends BaseGridFragment implements ListingModeFragment
{

    public static final String TAG = UserSearchFragment.class.getName();

    private static final String ARGUMENT_FIELD_ID = "fieldId";

    private static final String ARGUMENT_KEYWORD = "keyword";

    private static final String ARGUMENT_TITLE = "queryDescription";

    private Map<String, Person> selectedItems = new HashMap<>(1);

    private int mode = MODE_LISTING;

    private Fragment fragmentPick;

    private Button validation;

    private boolean singleChoice = true;

    private String keywords;

    private String fieldId;

    private EditText searchForm;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public UserSearchFragment()
    {
        emptyListMessageId = R.string.person_not_found;
        retrieveDataOnCreation = false;
        checkSession = true;
    }

    protected static UserSearchFragment newInstanceByTemplate(Bundle b)
    {
        UserSearchFragment cbf = new UserSearchFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }

        // Retrieve session object
        checkSession();

        if (getArguments() != null && getArguments().containsKey(ARGUMENT_MODE))
        {
            fieldId = getArguments().getString(ARGUMENT_FIELD_ID);
            keywords = getArguments().getString(ARGUMENT_KEYWORD);
            title = getArguments().getString(ARGUMENT_TITLE);
            mode = getArguments().getInt(ARGUMENT_MODE);
            singleChoice = getArguments().getBoolean(ARGUMENT_SINGLE_CHOICE);
            String pickFragmentTag = getArguments().getString(ARGUMENT_FRAGMENT_TAG);
            fragmentPick = getFragmentManager().findFragmentByTag(pickFragmentTag);
            if (fragmentPick != null && fragmentPick instanceof UserPickerCallback)
            {
                selectedItems = ((UserPickerCallback) fragmentPick).retrieveSelection();
            }
            else if (fragmentPick instanceof onPickAuthorityFragment && fieldId != null)
            {
                selectedItems = ((onPickAuthorityFragment) fragmentPick).getPersonSelected(fieldId);
            }
        }

        // Create View
        setRootView(inflater.inflate(R.layout.app_pick_person, container, false));
        if (getSession() == null) { return getRootView(); }

        // Init list
        init(getRootView(), R.string.person_not_found);
        gv.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        setListShown(true);

        if (keywords != null && !keywords.isEmpty())
        {
            search(keywords);
            hide(R.id.search_form_group);
        }
        else
        {
            // Init form search
            searchForm = (EditText) viewById(R.id.search_query);
            searchForm.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

            searchForm.setOnEditorActionListener(new OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (event != null
                            && (event.getAction() == KeyEvent.ACTION_DOWN)
                            && ((actionId == EditorInfo.IME_ACTION_SEARCH) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))
                    {
                        if (searchForm.getText().length() > 0)
                        {
                            keywords = searchForm.getText().toString();
                            search(keywords);
                        }
                        else
                        {
                            AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(
                                    getString(R.string.search_form_hint));
                        }
                        return true;
                    }
                    return false;
                }
            });

            // Speech to Text
            Boolean hasTextToSpeech = ActionUtils.hasSpeechToText(getActivity());
            ImageButton speechToText = (ImageButton) viewById(R.id.search_microphone);
            if (hasTextToSpeech)
            {
                speechToText.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        speechToText();
                    }
                });
            }
            else
            {
                hide(R.id.search_microphone);
            }
        }

        if (getMode() == MODE_PICK)
        {
            show(R.id.validation_panel);
            validation = UIUtils.initValidation(getRootView(), R.string.done);
            updatePickButton();
            validation.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onSelect(selectedItems);
                    if (getDialog() != null)
                    {
                        getDialog().dismiss();
                    }
                    else
                    {
                        getFragmentManager().popBackStack();
                    }
                }
            });

            Button cancel = UIUtils.initCancel(getRootView(), R.string.cancel);
            cancel.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (getDialog() != null)
                    {
                        getDialog().dismiss();
                    }
                    else
                    {
                        getFragmentManager().popBackStack();
                    }
                }
            });
        }
        else
        {
            hide(R.id.validation_panel);
        }

        return getRootView();
    }

    @Override
    public void onStart()
    {
        if (getDialog() != null)
        {
            if (fragmentPick instanceof TaskDetailsFragment)
            {
                getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_reassign);
                getDialog().setTitle(R.string.task_reassign_long);
            }
            else if (fragmentPick instanceof AdvancedSearchFragment)
            {
                getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_person_light);
                getDialog().setTitle(R.string.metadata_modified_by);
            }
            else
            {
                getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_person_light);
                getDialog().setTitle(R.string.search_title);
            }
        }
        else
        {
            if (title != null)
            {
                UIUtils.displayTitle(getActivity(), String.format(getString(R.string.search_title), title));
            }
            else if (keywords != null)
            {
                UIUtils.displayTitle(getActivity(), String.format(getString(R.string.search_title), keywords));
            }

        }
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getRootView().setVisibility(View.VISIBLE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        else
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case RequestCode.TEXT_TO_SPEECH:
            {
                if (resultCode == FragmentActivity.RESULT_OK && data != null)
                {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    searchForm.setText(text.get(0));
                    search(text.get(0));
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        emptyImageView.setImageResource(R.drawable.ic_empty_search_people);
        firstEmptyMessage.setText(R.string.people_search_empty_title);
        secondEmptyMessage.setVisibility(View.VISIBLE);
        secondEmptyMessage.setText(R.string.people_search_empty_description);
    }

    // //////////////////////////////////////////////////////////////////////
    // LOADERS
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new UserAdapter(this, R.layout.row_two_lines_caption_divider, new ArrayList<Person>(0), selectedItems);
    }

    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new PersonsRequest.Builder(null, keywords).setListingContext(listingContext);
    }

    @Subscribe
    public void onResult(PersonsEvent event)
    {
        displayData(event);
    }

    // //////////////////////////////////////////////////////////////////////
    // Public Method
    // //////////////////////////////////////////////////////////////////////
    protected void search(String keywords)
    {
        performRequest(onCreateOperationRequest(originListing));
    }

    private void speechToText()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());

        try
        {
            startActivityForResult(intent, RequestCode.TEXT_TO_SPEECH);
        }
        catch (ActivityNotFoundException a)
        {
            AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.file_editor_error_speech);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        Person item = (Person) l.getItemAtPosition(position);

        if (mode == MODE_PICK && !singleChoice)
        {
            l.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        }
        else if (mode == MODE_PICK && !singleChoice)
        {
            l.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        }
        else if (mode == MODE_LISTING && DisplayUtils.hasCentralPane(getActivity()))
        {
            l.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        }

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.containsKey(item.getIdentifier());
            if (mode == MODE_PICK && !singleChoice)
            {
                selectedItems.remove(item.getIdentifier());
            }
            else
            {
                selectedItems.clear();
            }
        }
        l.setItemChecked(position, true);
        v.setSelected(true);

        selectedItems.put(item.getIdentifier(), item);

        if (hideDetails)
        {
            if (mode == MODE_PICK)
            {
                selectedItems.remove(item.getIdentifier());
                updatePickButton();
            }
            else if (mode == MODE_LISTING && DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
                selectedItems.clear();
            }
        }
        else
        {
            if (mode == MODE_LISTING)
            {
                // Show properties
                UserProfileFragment.with(getActivity()).personId(item.getIdentifier()).display();
            }
            else if (mode == MODE_PICK)
            {
                validation.setEnabled(true);
                updatePickButton();
            }
        }
    }

    protected void updatePickButton()
    {
        validation.setEnabled(!selectedItems.isEmpty());
        validation.setText(String.format(
                MessageFormat.format(getString(R.string.picker_assign_person), selectedItems.size()),
                selectedItems.size()));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTENERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public int getMode()
    {
        return mode;
    }

    public void onSelect(Map<String, Person> selectedItems)
    {
        if (fragmentPick == null) { return; }
        if (fragmentPick instanceof UserPickerCallback)
        {
            ((UserPickerCallback) fragmentPick).onPersonSelected(selectedItems);
        }
        else if (fieldId != null && fragmentPick instanceof onPickAuthorityFragment)
        {
            ((onPickAuthorityFragment) fragmentPick).onPersonSelected(fieldId, selectedItems);
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder fieldId(String fieldId)
        {
            extraConfiguration.putString(ARGUMENT_FIELD_ID, fieldId);
            return this;
        }

        public Builder keywords(String keywords)
        {
            extraConfiguration.putString(ARGUMENT_KEYWORD, keywords);
            return this;
        }

        public Builder singleChoice(Boolean singleChoice)
        {
            extraConfiguration.putBoolean(ARGUMENT_SINGLE_CHOICE, singleChoice);
            return this;
        }

        public Builder fragmentTag(String fragmentTag)
        {
            extraConfiguration.putString(ARGUMENT_FRAGMENT_TAG, fragmentTag);
            return this;
        }

        public Builder title(String title)
        {
            extraConfiguration.putString(ARGUMENT_TITLE, title);
            return this;
        }
    }
}
