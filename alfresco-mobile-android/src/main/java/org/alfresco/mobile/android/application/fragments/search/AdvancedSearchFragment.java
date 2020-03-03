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
package org.alfresco.mobile.android.application.fragments.search;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.view.AdvancedSearchConfigModel;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.search.DocumentFolderSearchFragment;
import org.alfresco.mobile.android.application.fragments.user.UserPickerCallback;
import org.alfresco.mobile.android.application.fragments.user.UserSearchFragment;
import org.alfresco.mobile.android.application.fragments.user.UsersFragment;
import org.alfresco.mobile.android.application.fragments.workflow.SimpleViewHolder;
import org.alfresco.mobile.android.application.providers.search.HistorySearch;
import org.alfresco.mobile.android.application.providers.search.HistorySearchManager;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment.onPickDateFragment;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.template.ViewTemplate;
import org.alfresco.mobile.android.ui.utils.UIUtils;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class AdvancedSearchFragment extends AlfrescoFragment
        implements UserPickerCallback, onPickDateFragment, AdvancedSearchFragmentTemplate
{
    public static final String TAG = AdvancedSearchFragment.class.getName();

    private static final String DATE_FROM = "dateFrom";

    private static final String DATE_TO = "dateTo";

    private static final String ARGUMENT_SITE = "site";

    private static final String ARGUMENT_FOLDER = "parentFolder";

    // ///////////////////////////////////////////////////////////////////////////
    // VARIABLES
    // ///////////////////////////////////////////////////////////////////////////
    private Map<String, Person> assignees = new HashMap<>(1);

    private Button modifiedByButton;

    private Button modificationDateFrom;

    private Button modificationDateTo;

    private GregorianCalendar modificationDateFromValue;

    private GregorianCalendar modificationDateToValue;

    private View rootView;

    private Person modifiedBy;

    private EditText editName;

    private EditText editTitle;

    private EditText editDescription;

    private EditText editLocation;

    private int searchKey;

    private Spinner spinnerMimeType;

    private Folder tmpParentFolder;

    private String mTitle;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AdvancedSearchFragment()
    {
        screenName = AnalyticsManager.SCREEN_SEARCH_ADVANCED;
        reportAtCreation = false;
    }

    protected static AdvancedSearchFragment newInstanceByTemplate(Bundle b)
    {
        AdvancedSearchFragment cbf = new AdvancedSearchFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);

        int layoutId = R.layout.app_search_document;
        if (getArguments() != null && getArguments().containsKey(ARGUMENT_SEARCH_TYPE))
        {
            searchKey = getArguments().getInt(ARGUMENT_SEARCH_TYPE);
            switch (searchKey)
            {
                case HistorySearch.TYPE_PERSON:
                    layoutId = R.layout.app_search_user;
                    screenName = AnalyticsManager.SCREEN_SEARCH_ADVANCED_USERS;
                    break;
                case HistorySearch.TYPE_FOLDER:
                    layoutId = R.layout.app_search_document;
                    screenName = AnalyticsManager.SCREEN_SEARCH_ADVANCED_FOLDERS;
                    break;
                case HistorySearch.TYPE_DOCUMENT:
                    layoutId = R.layout.app_search_document;
                    screenName = AnalyticsManager.SCREEN_SEARCH_ADVANCED_FILES;
                    break;
                default:
                    break;
            }

            AnalyticsHelper.reportScreen(getActivity(), screenName);

            // Search inside a folder
            tmpParentFolder = (Folder) getArguments().getSerializable(ARGUMENT_FOLDER);
            if (getArguments().containsKey(ViewTemplate.ARGUMENT_LABEL))
            {
                mTitle = getArguments().getString(ViewTemplate.ARGUMENT_LABEL);
            }
        }

        rootView = inflater.inflate(layoutId, container, false);

        setSession(SessionUtils.getSession(getActivity()));
        SessionUtils.checkSession(getActivity(), getSession());

        switch (searchKey)
        {
            case HistorySearch.TYPE_PERSON:
                initPersonrForm();
                break;
            case HistorySearch.TYPE_FOLDER:
            case HistorySearch.TYPE_DOCUMENT:
                initDocFolderForm();
                break;
            default:
                break;
        }

        return rootView;
    }

    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), (mTitle != null) ? mTitle : getString(R.string.search_advanced));
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void initPersonrForm()
    {
        // Retrieve editText
        editName = (EditText) rootView.findViewById(R.id.search_name);
        editTitle = (EditText) rootView.findViewById(R.id.jobTitle);
        editDescription = (EditText) rootView.findViewById(R.id.company);
        editLocation = (EditText) rootView.findViewById(R.id.location);

        // BUTTON
        Button validationButton = UIUtils.initValidation(rootView, R.string.search);
        validationButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                search();
            }
        });

        Button clear = UIUtils.initCancel(rootView, R.string.clear);
        clear.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clear();
            }
        });

        // ACCESSIBILITY
        if (AccessibilityUtils.isEnabled(getActivity()))
        {
            AccessibilityUtils.addHint(rootView.findViewById(R.id.search_name), R.string.search_name);
            AccessibilityUtils.addHint(rootView.findViewById(R.id.jobTitle), R.string.jobTitle);
            AccessibilityUtils.addHint(rootView.findViewById(R.id.company), R.string.company);
            AccessibilityUtils.addHint(rootView.findViewById(R.id.location), R.string.location);
        }
    }

    private void initDocFolderForm()
    {
        // Retrieve editText
        editName = (EditText) rootView.findViewById(R.id.metadata_prop_name);
        editTitle = (EditText) rootView.findViewById(R.id.metadata_prop_title);
        editDescription = (EditText) rootView.findViewById(R.id.metadata_prop_description);

        // Spinner Mimetype
        spinnerMimeType = (Spinner) rootView.findViewById(R.id.metadata_prop_mimetype);
        if (searchKey != HistorySearch.TYPE_FOLDER)
        {
            spinnerMimeType.setAdapter(new MimetypeAdapter(getActivity()));
        }
        else
        {
            rootView.findViewById(R.id.metadata_prop_mimetype_header).setVisibility(View.GONE);
            spinnerMimeType.setVisibility(View.GONE);
        }

        // Last Modification date
        // TO
        ImageButton ib = (ImageButton) rootView.findViewById(R.id.action_metadata_modification_date_to);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment.newInstance(DATE_TO, TAG).show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });
        modificationDateTo = (Button) rootView.findViewById(R.id.metadata_modification_date_to);
        if (modificationDateToValue != null)
        {
            modificationDateTo
                    .setText(DateFormat.getDateFormat(getActivity()).format(modificationDateToValue.getTime()));
        }
        modificationDateTo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment.newInstance(DATE_TO, TAG).show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });

        // FROM
        ib = (ImageButton) rootView.findViewById(R.id.action_metadata_modification_date_from);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment.newInstance(DATE_FROM, TAG).show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });
        modificationDateFrom = (Button) rootView.findViewById(R.id.metadata_modification_date_from);
        if (modificationDateFromValue != null)
        {
            modificationDateFrom
                    .setText(DateFormat.getDateFormat(getActivity()).format(modificationDateFromValue.getTime()));
        }
        modificationDateFrom.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment.newInstance(DATE_FROM, TAG).show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });

        ib = (ImageButton) rootView.findViewById(R.id.action_metadata_modification_date_from);
        ib.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerFragment.newInstance(DATE_FROM, TAG).show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });

        // Last Modification by
        if (getSession() instanceof CloudSession)
        {
            rootView.findViewById(R.id.modified_by_group).setVisibility(View.GONE);
        }
        else
        {
            rootView.findViewById(R.id.modified_by_group).setVisibility(View.VISIBLE);
            ib = (ImageButton) rootView.findViewById(R.id.action_metadata_prop_modified_by);
            modifiedByButton = (Button) rootView.findViewById(R.id.metadata_prop_modified_by);
            ib.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startPersonPicker();
                }
            });
            modifiedByButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startPersonPicker();
                }
            });
        }

        // BUTTON
        Button validationButton = UIUtils.initValidation(rootView, R.string.search);
        validationButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                search();
            }
        });

        Button clear = UIUtils.initCancel(rootView, R.string.clear);
        clear.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clear();
            }
        });

        // ACCESSIBILITY
        if (AccessibilityUtils.isEnabled(getActivity()))
        {
            AccessibilityUtils.addHint(rootView.findViewById(R.id.metadata_prop_name), R.string.metadata_prop_name);
            AccessibilityUtils.addHint(rootView.findViewById(R.id.metadata_prop_title), R.string.metadata_prop_title);
            AccessibilityUtils.addHint(rootView.findViewById(R.id.metadata_prop_description),
                    R.string.metadata_prop_description);
        }
    }

    private void startPersonPicker()
    {
        UserSearchFragment.with(getActivity()).fragmentTag(TAG).singleChoice(true).mode(UserSearchFragment.MODE_PICK)
                .displayAsDialog();
    }

    private void search()
    {
        String statement = createQuery();
        String description = createDescriptionQuery();
        if (statement == null)
        {
            AlfrescoNotificationManager.getInstance(getActivity())
                    .showLongToast(getActivity().getString(R.string.error_search_fields_empty));
            return;
        }

        String label = null;
        switch (searchKey)
        {
            case HistorySearch.TYPE_PERSON:
                label = AnalyticsManager.LABEL_PEOPLE;
                UsersFragment.with(getActivity()).keywords(statement).title(description).display();
                break;
            case HistorySearch.TYPE_FOLDER:
                label = AnalyticsManager.LABEL_FOLDERS;
                DocumentFolderSearchFragment.with(getActivity()).query(statement).title(description).display();
                break;
            case HistorySearch.TYPE_DOCUMENT:
                label = AnalyticsManager.LABEL_DOCUMENTS;
                DocumentFolderSearchFragment.with(getActivity()).query(statement).title(description).display();
                break;
            default:
                break;
        }
        // Save history or update
        HistorySearchManager.createHistorySearch(getActivity(), SessionUtils.getAccount(getActivity()).getId(),
                searchKey, 1, description, statement, new Date().getTime());

        AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_SEARCH,
                AnalyticsManager.ACTION_RUN_ADVANCED, label, 1, false);

    }

    private String createQuery()
    {
        String name = editName.getText().toString();
        String title = editTitle.getText().toString();
        String description = editDescription.getText().toString();
        String modifiedId = (modifiedBy != null) ? modifiedBy.getIdentifier() : "";

        switch (searchKey)
        {
            case HistorySearch.TYPE_PERSON:
                String location = editLocation.getText().toString();
                if (isEmpty(name, title, description, location)) { return null; }
                return QueryHelper.createPersonSearchQuery(name, title, description, location);
            case HistorySearch.TYPE_FOLDER:
                if (isEmpty(name, title, description, null, modifiedId, modificationDateFromValue,
                        modificationDateToValue)) { return null; }
                return QueryHelper.createQuery(false, name, title, description, -1, modifiedId,
                        modificationDateFromValue, modificationDateToValue, tmpParentFolder);
            case HistorySearch.TYPE_DOCUMENT:
                Integer mimetype = (Integer) spinnerMimeType.getSelectedItem();
                if (isEmpty(name, title, description, mimetype, modifiedId, modificationDateFromValue,
                        modificationDateToValue)) { return null; }
                return QueryHelper.createQuery(true, name, title, description, mimetype, modifiedId,
                        modificationDateFromValue, modificationDateToValue, tmpParentFolder);
            default:
                break;
        }
        return null;
    }

    private boolean isEmpty(String name, String title, String description, Integer mimetype, String modifiedId,
            GregorianCalendar modificationDateFromValue, GregorianCalendar modificationDateToValue)
    {
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(title) && TextUtils.isEmpty(description)
                && TextUtils.isEmpty(modifiedId) && modificationDateFromValue == null
                && modificationDateToValue == null)
        {
            if (mimetype == null || mimetype == R.string.mimetype_unknown)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return false;
    }

    private boolean isEmpty(String name, String title, String description, String location)
    {
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(title) && TextUtils.isEmpty(description)
                && TextUtils.isEmpty(location)) { return true; }
        return false;
    }

    private String createDescriptionQuery()
    {
        StringBuilder builder = new StringBuilder();
        String name = editName.getText().toString();
        String title = editTitle.getText().toString();
        String description = editDescription.getText().toString();
        String modifiedId = (modifiedBy != null) ? modifiedBy.getIdentifier() : "";

        switch (searchKey)
        {
            case HistorySearch.TYPE_PERSON:
                String location = editLocation.getText().toString();
                addParameter(builder, "name", name);
                addParameter(builder, "jobtitle", title);
                addParameter(builder, "organization", description);
                addParameter(builder, "location", location);
                break;
            case HistorySearch.TYPE_FOLDER:
                if (tmpParentFolder != null)
                {
                    addParameter(builder, "in", tmpParentFolder.getName());
                }
                addParameter(builder, "name", name);
                addParameter(builder, "title", title);
                addParameter(builder, "description", description);
                addParameter(builder, "modifier", modifiedId);
                addParameter(builder, "-modified", modificationDateFromValue);
                addParameter(builder, "+modified", modificationDateToValue);
                break;
            case HistorySearch.TYPE_DOCUMENT:
                Integer mimetype = (Integer) spinnerMimeType.getSelectedItem();
                if (tmpParentFolder != null)
                {
                    addParameter(builder, "in", tmpParentFolder.getName());
                }
                addParameter(builder, "name", name);
                addParameter(builder, "title", title);
                addParameter(builder, "description", description);
                if (mimetype != R.string.mimetype_unknown)
                {
                    addParameter(builder, "mimetype", getString(mimetype));
                }
                addParameter(builder, "modifier", modifiedId);
                addParameter(builder, "-modified", modificationDateFromValue);
                addParameter(builder, "+modified", modificationDateToValue);
                break;
            default:
                break;
        }
        return builder.toString();
    }

    private static void addParameter(StringBuilder builder, String key, String value)
    {
        if (TextUtils.isEmpty(value)) { return; }
        if (builder.length() != 0)
        {
            builder.append(", ");
        }

        builder.append(key);
        builder.append(":");
        if (value.contains(" "))
        {
            builder.append("\"");
        }
        builder.append(value);
        if (value.contains(" "))
        {
            builder.append("\"");
        }
    }

    private static void addParameter(StringBuilder builder, String key, GregorianCalendar calendar)
    {
        if (calendar == null) { return; }
        if (builder.length() != 0)
        {
            builder.append(", ");
        }
        builder.append(key);
        builder.append(":");
        builder.append(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
    }

    private void clear()
    {
        editName.setText("");
        editTitle.setText("");
        editDescription.setText("");
        switch (searchKey)
        {
            case HistorySearch.TYPE_PERSON:
                editLocation.setText("");
                break;
            case HistorySearch.TYPE_FOLDER:
                modificationDateFrom.setText("");
                modificationDateTo.setText("");
                modificationDateFromValue = null;
                modificationDateToValue = null;
                modifiedByButton.setText("");
                break;
            case HistorySearch.TYPE_DOCUMENT:
                spinnerMimeType.setSelection(0);
                modificationDateFrom.setText("");
                modificationDateTo.setText("");
                modificationDateFromValue = null;
                modificationDateToValue = null;
                modifiedByButton.setText("");
                break;
            default:
                break;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PERSON PICKER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPersonSelected(Map<String, Person> p)
    {
        if (p == null) { return; }
        // Only one modifier
        assignees.putAll(p);
        for (Entry<String, Person> entry : assignees.entrySet())
        {
            modifiedBy = entry.getValue();
            modifiedByButton.setText(entry.getValue().getFullName());
        }
    }

    @Override
    public Map<String, Person> retrieveSelection()
    {
        return assignees;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DATE PICKER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onDatePicked(String dateId, GregorianCalendar gregorianCalendar)
    {
        if (DATE_FROM.equalsIgnoreCase(dateId))
        {
            modificationDateFromValue = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            modificationDateFromValue.setTime(gregorianCalendar.getTime());
            modificationDateFrom
                    .setText(DateFormat.getDateFormat(getActivity()).format(modificationDateFromValue.getTime()));
        }
        else if (DATE_TO.equalsIgnoreCase(dateId))
        {
            modificationDateToValue = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            modificationDateToValue.setTime(gregorianCalendar.getTime());
            modificationDateTo
                    .setText(DateFormat.getDateFormat(getActivity()).format(modificationDateToValue.getTime()));
        }
    }

    @Override
    public void onDateClear(String dateId)
    {
        if (DATE_FROM.equalsIgnoreCase(dateId))
        {
            modificationDateFromValue = null;
            modificationDateFrom.setText("");
        }
        else if (DATE_TO.equalsIgnoreCase(dateId))
        {
            modificationDateToValue = null;
            modificationDateTo.setText("");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ADAPTER MIMETYPE
    // ///////////////////////////////////////////////////////////////////////////
    public class MimetypeAdapter extends BaseListAdapter<Integer, SimpleViewHolder>
    {
        private int px;

        public MimetypeAdapter(FragmentActivity context)
        {
            super(context, R.layout.app_header_row, MIMETYPE_GROUPS);
            this.vhClassName = SimpleViewHolder.class.getCanonicalName();
            px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                    getContext().getResources().getDisplayMetrics());
        }

        @Override
        protected void updateTopText(SimpleViewHolder vh, Integer item)
        {
            vh.topText.setVisibility(View.GONE);
        }

        @Override
        protected void updateBottomText(SimpleViewHolder vh, Integer item)
        {
            vh.bottomText.setText(getContext().getString(item));
        }

        @Override
        protected void updateIcon(SimpleViewHolder vh, Integer item)
        {
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            View v = super.getView(position, convertView, parent);
            ((TextView) v.findViewById(R.id.bottomtext)).setMinHeight(px);
            return v;
        }
    }

    private static final List<Integer> MIMETYPE_GROUPS = new ArrayList<Integer>(10)
    {
        private static final long serialVersionUID = 1L;

        {
            add(R.string.mimetype_unknown);
            add(R.string.mimetype_documents);
            add(R.string.mimetype_presentations);
            add(R.string.mimetype_spreadsheets);
            add(R.string.mimetype_text);
            add(R.string.mimetype_images);
            add(R.string.mimetype_videos);
            add(R.string.mimetype_music);
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        public static final int ICON_ID = R.drawable.ic_search_dark;

        public static final int LABEL_ID = R.string.search_advanced;

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
            viewConfigModel = new AdvancedSearchConfigModel(configuration);
        }

        @Override
        protected void retrieveCustomArgument(Map<String, Object> properties, Bundle b)
        {
            String type = JSONConverter.getString(properties, ARGUMENT_SEARCH_TYPE);
            int searchType = HistorySearch.TYPE_DOCUMENT;
            if (!TextUtils.isEmpty(type))
            {
                if (TYPE_FOLDER.equalsIgnoreCase(type))
                {
                    searchType = HistorySearch.TYPE_FOLDER;
                }
                else if (TYPE_DOCUMENT.equalsIgnoreCase(type))
                {
                    searchType = HistorySearch.TYPE_DOCUMENT;
                }
                else if (TYPE_PERSON.equalsIgnoreCase(type))
                {
                    searchType = HistorySearch.TYPE_PERSON;
                }
                b.putInt(ARGUMENT_SEARCH_TYPE, searchType);
            }
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder searchType(int searchType)
        {
            extraConfiguration.putInt(ARGUMENT_SEARCH_TYPE, searchType);
            return this;
        }

        public Builder site(Site site)
        {
            extraConfiguration.putSerializable(ARGUMENT_SITE, site);
            return this;
        }

        public Builder folder(Folder folder)
        {
            extraConfiguration.putSerializable(ARGUMENT_FOLDER, folder);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

    }
}
