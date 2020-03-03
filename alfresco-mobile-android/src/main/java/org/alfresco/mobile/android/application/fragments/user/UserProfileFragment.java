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

import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Company;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.view.UserProfileConfigModel;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.person.PersonEvent;
import org.alfresco.mobile.android.async.person.PersonRequest;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.person.PersonProfileTemplate;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import com.squareup.otto.Subscribe;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

/**
 * @since 1.3
 * @author jm.pascal
 */
public class UserProfileFragment extends AlfrescoFragment implements OnMenuItemClickListener, PersonProfileTemplate
{
    public static final String TAG = UserProfileFragment.class.getName();

    public static final String ARGUMENT_ACCOUNTID = "accountId";

    private Long accountId;

    private Person person;

    private String userName;

    private AlfrescoSession session;

    private int titleId = R.string.user_profile;

    private boolean displayContactDetails = false, displayCompanyDetails = false;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public UserProfileFragment()
    {
        screenName = AnalyticsManager.PREFIX_USER.concat(AnalyticsManager.SCREEN_USER_DETAILS);
    }

    protected static UserProfileFragment newInstanceByTemplate(Bundle b)
    {
        UserProfileFragment cbf = new UserProfileFragment();
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

        if (getArguments() != null)
        {
            userName = getArguments().getString(ARGUMENT_USERNAME);
            if (getArguments().containsKey(ARGUMENT_ACCOUNTID))
            {
                accountId = getArguments().getLong(ARGUMENT_ACCOUNTID);
            }
        }

        session = SessionUtils.getSession(getActivity());
        if (accountId != null && accountId != -1)
        {
            session = SessionManager.getInstance(getActivity()).getSession(accountId);
        }

        setSession(session);
        SessionUtils.checkSession(getActivity(), getSession());

        // Create View
        setRootView(inflater.inflate(R.layout.app_user_profile, container, false));
        if (getSession() == null) { return getRootView(); }

        if (TextUtils.isEmpty(userName))
        {
            userName = getSession().getPersonIdentifier();
            titleId = R.string.my_profile;
        }

        // Icon
        RenditionManager.with(getActivity()).loadAvatar(userName).placeHolder(R.drawable.ic_person_light)
                .into((ImageView) viewById(R.id.preview));
        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        setSession(session);
        SessionUtils.checkSession(getActivity(), getSession());
        super.onActivityCreated(savedInstanceState);

        hide(R.id.profile_details);
        show(R.id.progressbar);
        if (accountId == null)
        {
            Operator.with(getActivity(), getAccount())
                    .load(new PersonRequest.Builder(userName).setAccountId(getAccount().getId()));
        }
        else
        {
            Operator.with(getActivity(), AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId))
                    .load(new PersonRequest.Builder(userName).setAccountId(accountId));
        }

    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_person_light);
            getDialog().setTitle(titleId);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), titleId);
        }
    }

    @Override
    public void onResume()
    {
        getActivity().invalidateOptionsMenu();

        super.onResume();
    }

    // //////////////////////////////////////////////////////////////////////
    // LOADERS
    // //////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(PersonEvent event)
    {
        if (event.hasException)
        {
            hide(R.id.progressbar);
            show(R.id.empty);
            if (event.exception instanceof AlfrescoServiceException
                    && event.exception.getMessage().contains("not found"))
            {
                ((TextView) viewById(R.id.empty_text)).setText(R.string.empty_users);
            }
            else
            {
                CloudExceptionUtils.handleCloudException(getActivity(), event.exception, false);
            }
        }
        else
        {
            person = event.data;
            refresh();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Public Method
    // //////////////////////////////////////////////////////////////////////
    private static final int MODE_SKYPE = 0;

    private static final int MODE_EMAIL = 1;

    private static final int MODE_CALL = 2;

    private static final int MODE_LOCATION = 4;

    private void getMenu(Menu menu, int mode)
    {
        MenuItem mi = null;
        switch (mode)
        {
            case MODE_CALL:
                if (person.getTelephoneNumber() != null && !person.getTelephoneNumber().isEmpty())
                {
                    mi = menu.add(Menu.NONE, R.id.menu_user_phone, Menu.FIRST + 6, person.getTelephoneNumber());
                    mi.setIcon(R.drawable.ic_call);
                    mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }

                if (person.getMobileNumber() != null && !person.getMobileNumber().isEmpty())
                {
                    mi = menu.add(Menu.NONE, R.id.menu_user_mobile, Menu.FIRST + 7, person.getMobileNumber());
                    mi.setIcon(R.drawable.ic_call);
                    mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }

                if (person.getCompany() != null && person.getCompany().getTelephoneNumber() != null
                        && !person.getCompany().getTelephoneNumber().isEmpty())
                {
                    mi = menu.add(Menu.NONE, R.id.menu_user_company_phone, Menu.FIRST + 5,
                            person.getCompany().getTelephoneNumber());
                    mi.setIcon(R.drawable.ic_call);
                    mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }
                break;
            case MODE_SKYPE:
                mi = menu.add(Menu.NONE, R.id.menu_user_chat, Menu.FIRST, R.string.start_chat);
                mi.setIcon(R.drawable.ic_im);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                mi = menu.add(Menu.NONE, R.id.menu_user_call, Menu.FIRST + 1, R.string.start_call);
                mi.setIcon(R.drawable.ic_call);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                mi = menu.add(Menu.NONE, R.id.menu_user_videocall, Menu.FIRST + 2, R.string.start_video_call);
                mi.setIcon(R.drawable.ic_videos);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            case MODE_EMAIL:
                if (person.getEmail() != null && !person.getEmail().isEmpty())
                {
                    mi = menu.add(Menu.NONE, R.id.menu_user_email, Menu.FIRST + 3, person.getEmail());
                    mi.setIcon(R.drawable.ic_send_mail);
                    mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }

                if (person.getCompany() != null && person.getCompany().getEmail() != null
                        && !person.getCompany().getEmail().isEmpty())
                {
                    mi = menu.add(Menu.NONE, R.id.menu_user_company_email, Menu.FIRST + 4,
                            person.getCompany().getEmail());
                    mi.setIcon(R.drawable.ic_send_mail);
                    mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }
                break;

            default:
                break;
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        String actionName = null, actionLabel = null;
        switch (item.getItemId())
        {
            case R.id.menu_user_chat:
                onMenuItemClick = true;
                actionSkype(getActivity(), ACTION_CHAT, person.getSkypeId());
                actionName = AnalyticsManager.ACTION_SKYPE;
                actionLabel = AnalyticsManager.LABEL_CHAT;
                break;
            case R.id.menu_user_call:
                actionSkype(getActivity(), ACTION_CALL, person.getSkypeId());
                onMenuItemClick = true;
                actionName = AnalyticsManager.ACTION_SKYPE;
                actionLabel = AnalyticsManager.LABEL_CALL;
                break;
            case R.id.menu_user_videocall:
                actionSkype(getActivity(), ACTION_VIDEO_CALL, person.getSkypeId());
                onMenuItemClick = true;
                actionName = AnalyticsManager.ACTION_SKYPE;
                actionLabel = AnalyticsManager.LABEL_VIDEOCALL;
                break;
            case R.id.menu_user_email:
                actionEmail(getActivity(), person.getEmail(), null, null);
                onMenuItemClick = true;
                actionName = AnalyticsManager.ACTION_EMAIL;
                actionLabel = AnalyticsManager.LABEL_USER;
                break;
            case R.id.menu_user_company_email:
                actionEmail(getActivity(), person.getCompany().getEmail(), null, null);
                onMenuItemClick = true;
                actionName = AnalyticsManager.ACTION_EMAIL;
                actionLabel = AnalyticsManager.LABEL_COMPANY;
                break;
            case R.id.menu_user_company_phone:
                actionCall(getActivity(), person.getCompany().getTelephoneNumber());
                onMenuItemClick = true;
                actionName = AnalyticsManager.ACTION_CALL;
                actionLabel = AnalyticsManager.LABEL_COMPANY;
                break;
            case R.id.menu_user_phone:
                actionCall(getActivity(), person.getTelephoneNumber());
                onMenuItemClick = true;
                actionName = AnalyticsManager.ACTION_CALL;
                actionLabel = AnalyticsManager.LABEL_PHONE;
                break;
            case R.id.menu_user_mobile:
                actionCall(getActivity(), person.getMobileNumber());
                onMenuItemClick = true;
                actionName = AnalyticsManager.ACTION_CALL;
                actionLabel = AnalyticsManager.LABEL_MOBILE;
                break;
            default:
                onMenuItemClick = false;
                break;
        }

        if (onMenuItemClick)
        {
            AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_USER, actionName, actionLabel,
                    1, false);
        }

        return onMenuItemClick;
    }

    // //////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////
    private void refresh()
    {
        if (person != null)
        {
            show(R.id.profile_details);
            hide(R.id.progressbar);
            display();
        }
        getActivity().invalidateOptionsMenu();
        AccessibilityUtils.sendAccessibilityEvent(getActivity());
    }

    private void display()
    {
        TextView tv = null;
        ImageView bIm = null;

        // HEADER
        tv = (TextView) viewById(R.id.name);
        tv.setText(person.getFullName());

        // JOB TITLE
        tv = (TextView) viewById(R.id.jobTitle);
        if (person.getJobTitle() != null && !person.getJobTitle().isEmpty() && person.getCompany() != null
                && person.getCompany().getName() != null && !person.getCompany().getName().isEmpty())
        {
            tv.setText(String.format(getString(R.string.work_at), person.getJobTitle(), person.getCompany().getName()));
        }
        else if (person.getJobTitle() != null)
        {
            tv.setText(person.getJobTitle());
        }
        else
        {
            tv.setText(person.getCompany().getName());
        }

        // Location
        tv = (TextView) viewById(R.id.location);
        tv.setText(person.getLocation());

        // Summary
        displayOrHide(R.id.description, person.getSummary(), R.id.summary_group);

        // Email
        displayOrHide(R.id.email_value, person.getEmail(), R.id.email_group);

        // Telephone
        displayOrHide(R.id.telephone_value, person.getTelephoneNumber(), R.id.telephone_group);

        // Mobile
        displayOrHide(R.id.mobile_value, person.getMobileNumber(), R.id.mobile_group);

        if (person.getTelephoneNumber() != null || person.getMobileNumber() != null
                || person.getCompany().getTelephoneNumber() != null)
        {
            bIm = (ImageView) viewById(R.id.action_call);
            bIm.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    PopupMenu popup = new PopupMenu(getActivity(), v);
                    getMenu(popup.getMenu(), MODE_CALL);
                    popup.setOnMenuItemClickListener(UserProfileFragment.this);
                    popup.show();
                }
            });
        }
        else
        {
            hide(R.id.action_call);
        }

        // SKype
        if (person.getSkypeId() != null && !person.getSkypeId().isEmpty())
        {
            displayGroup();
            tv = (TextView) viewById(R.id.skypeId_value);
            tv.setText(person.getSkypeId());
            bIm = (ImageView) viewById(R.id.action_skype);
            bIm.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    PopupMenu popup = new PopupMenu(getActivity(), v);
                    getMenu(popup.getMenu(), MODE_SKYPE);
                    popup.setOnMenuItemClickListener(UserProfileFragment.this);
                    popup.show();
                }
            });
        }
        else
        {
            hide(R.id.action_skype);
            hide(R.id.skypeId_group);
        }

        // IM
        if (person.getInstantMessageId() != null && !person.getInstantMessageId().isEmpty())
        {
            displayGroup();
            tv = (TextView) viewById(R.id.instantMessagingId_value);
            tv.setText(person.getInstantMessageId());
        }
        else
        {
            hide(R.id.instantMessagingId_group);
        }

        // Google
        if (person.getGoogleId() != null && !person.getGoogleId().isEmpty())
        {
            displayGroup();
            tv = (TextView) viewById(R.id.googleId_value);
            tv.setText(person.getGoogleId());
            bIm = (ImageView) viewById(R.id.action_im);
            bIm.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    actionStartIm(getActivity(), person.getGoogleId());
                }
            });
        }
        else
        {
            hide(R.id.action_im);
            hide(R.id.googleId_group);
        }

        if (!displayContactDetails)
        {
            hide(R.id.contactInfo_group);
            displayContactDetails = false;
        }

        // Company
        Company cp = person.getCompany();
        displayCompanyOrHide(R.id.companyName_value, cp.getName(), R.id.companyName_group);
        displayCompanyOrHide(R.id.companyAdress1_value, cp.getAddress1(), R.id.companyAdress1_group);
        displayCompanyOrHide(R.id.companyAdress2_value, cp.getAddress2(), R.id.companyAdress2_group);
        displayCompanyOrHide(R.id.companyAdress3_value, cp.getAddress3(), R.id.companyAdress3_group);
        displayCompanyOrHide(R.id.companyPostcode_value, cp.getPostCode(), R.id.companyPostcode_group);
        displayCompanyOrHide(R.id.companyTelephone_value, cp.getTelephoneNumber(), R.id.companyTelephone_group);
        displayCompanyOrHide(R.id.companyFax_value, cp.getFaxNumber(), R.id.companyFax_group);
        displayCompanyOrHide(R.id.companyEmail_value, cp.getEmail(), R.id.companyEmail_group);

        // Add Contact
        bIm = (ImageView) viewById(R.id.action_addcontact);
        bIm.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                actionAddContact(getActivity(), person);
            }
        });

        // Add Contact
        if (person.getEmail() != null || person.getCompany().getEmail() != null)
        {
            bIm = (ImageView) viewById(R.id.action_email);
            bIm.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    PopupMenu popup = new PopupMenu(getActivity(), v);
                    getMenu(popup.getMenu(), MODE_EMAIL);
                    popup.setOnMenuItemClickListener(UserProfileFragment.this);
                    popup.show();
                }
            });
        }
        else
        {
            hide(R.id.action_email);
        }

        // Geolocalisation
        if (person.getCompany().getFullAddress() != null)
        {
            displayGroup();
            bIm = (ImageView) viewById(R.id.action_geolocation);
            bIm.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    actionGeolocalisation(getActivity(), person.getCompany().getFullAddress(),
                            person.getCompany().getName());
                }
            });
        }
        else
        {
            hide(R.id.action_geolocation);
        }

        if (!displayCompanyDetails)
        {
            hide(R.id.company_group);
        }
    }

    private void displayOrHide(int viewId, String value, int groupId)
    {
        displayOrHide(viewId, value, groupId, false);
    }

    private void displayOrHide(int viewId, String value, int groupId, boolean isCompany)
    {
        // Summary
        if (viewById(viewId) != null && value != null && !value.isEmpty())
        {
            if (isCompany)
            {
                displayCompanyGroup();
            }
            else
            {
                displayGroup();
            }
            TextView tv = (TextView) viewById(viewId);
            tv.setText(value);
        }
        else
        {
            hide(groupId);
        }
    }

    private void displayCompanyOrHide(int viewId, String value, int groupId)
    {
        displayOrHide(viewId, value, groupId, true);
    }

    private void displayGroup()
    {
        if (!displayContactDetails)
        {
            displayContactDetails = true;
        }
    }

    private void displayCompanyGroup()
    {
        if (!displayCompanyDetails)
        {
            displayCompanyDetails = true;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionGeolocalisation(FragmentActivity a, String location, String Title)
    {
        try
        {
            final String uri = "geo:0,0?q=" + location.trim().replaceAll(" ", "+").replaceAll(",", "") + " (" + Title
                    + ")";
            a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        }
        catch (ActivityNotFoundException e)
        {

        }
    }

    public static void actionAddContact(FragmentActivity activity, Person member)
    {
        if (member != null)
        {
            final Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

            // ABOUT Information
            intent.putExtra(ContactsContract.Intents.Insert.NAME, member.getLastName() + " " + member.getFirstName());

            // JOB TITLE
            intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, member.getJobTitle());

            // CONTACT PHONE
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, member.getTelephoneNumber());
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY, true);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MAIN);
            intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, member.getMobileNumber());
            intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

            // CONTACT EMAIL
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, member.getEmail());
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK);

            // CONTACT IM
            if (member.getSkypeId() != null && member.getSkypeId().length() > 0)
            {
                intent.putExtra(ContactsContract.Intents.Insert.IM_HANDLE, member.getSkypeId());
                intent.putExtra(ContactsContract.Intents.Insert.IM_PROTOCOL,
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE);
            }
            if (member.getGoogleId() != null && member.getGoogleId().length() > 0)
            {
                intent.putExtra(ContactsContract.Intents.Insert.IM_HANDLE, member.getGoogleId());
                intent.putExtra(ContactsContract.Intents.Insert.IM_PROTOCOL,
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK);
            }
            else if (member.getInstantMessageId() != null && member.getInstantMessageId().length() > 0)
            {
                intent.putExtra(ContactsContract.Intents.Insert.IM_HANDLE, member.getInstantMessageId());
            }

            // COMPANY DETAILS
            intent.putExtra(ContactsContract.Intents.Insert.COMPANY, member.getCompany().getName());

            intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, member.getCompany().getEmail());
            intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_OTHER);

            intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, member.getCompany().getTelephoneNumber());
            intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try
            {
                if (intent.resolveActivity(activity.getPackageManager()) == null)
                {
                    AlfrescoNotificationManager.getInstance(activity).showAlertCrouton((FragmentActivity) activity,
                            activity.getString(R.string.feature_disable));
                    return;
                }

                activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.contact_add)));

                AnalyticsHelper.reportOperationEvent(activity, AnalyticsManager.CATEGORY_USER,
                        AnalyticsManager.ACTION_ADD_CONTACT, AnalyticsManager.LABEL_CONTACT, 1, false);
            }
            catch (ActivityNotFoundException e)
            {

            }
        }
    }

    public static void actionEmail(FragmentActivity activity, String email, String subject, String content)
    {
        if (subject == null) subject = "";
        if (content == null) content = "";

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);

        try
        {
            if (emailIntent.resolveActivity(activity.getPackageManager()) == null)
            {
                AlfrescoNotificationManager.getInstance(activity).showAlertCrouton((FragmentActivity) activity,
                        activity.getString(R.string.feature_disable));
                return;
            }
            activity.startActivity(Intent.createChooser(emailIntent, "Select email application."));
        }
        catch (ActivityNotFoundException e)
        {

        }
    }

    public static void actionCall(FragmentActivity activity, String number)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
            if (intent.resolveActivity(activity.getPackageManager()) == null)
            {
                AlfrescoNotificationManager.getInstance(activity).showAlertCrouton((FragmentActivity) activity,
                        activity.getString(R.string.feature_disable));
                return;
            }
            activity.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {

        }
    }

    public static void actionStartIm(FragmentActivity activity, String personId)
    {
        Uri imUri = new Uri.Builder().scheme("imto").authority("gtalk").appendPath(personId).build();
        Intent intent = new Intent(Intent.ACTION_SENDTO, imUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try
        {
            if (intent.resolveActivity(activity.getPackageManager()) == null)
            {
                AlfrescoNotificationManager.getInstance(activity).showAlertCrouton((FragmentActivity) activity,
                        activity.getString(R.string.feature_disable));
                return;
            }
            activity.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {

        }
    }

    public static void actionSendSMS(FragmentActivity activity, String number)
    {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("address", number);
        sendIntent.setType("vnd.android-dir/mms-sms");
        sendIntent.putExtra("sms_body", "");

        try
        {
            if (sendIntent.resolveActivity(activity.getPackageManager()) == null)
            {
                AlfrescoNotificationManager.getInstance(activity).showAlertCrouton((FragmentActivity) activity,
                        activity.getString(R.string.feature_disable));
                return;
            }
            activity.startActivity(Intent.createChooser(sendIntent, "Select SMS application."));
        }
        catch (ActivityNotFoundException e)
        {

        }
    }

    public static final int ACTION_CALL = 0;

    public static final int ACTION_CHAT = 1;

    public static final int ACTION_VIDEO_CALL = 2;

    public void actionSkype(FragmentActivity activity, int skypeAction, String personId)
    {
        // Make sure the Skype for Android client is installed
        if (!isSkypeClientInstalled(activity))
        {
            goToMarket(activity);
            return;
        }

        String mySkypeUri = "skype:";
        switch (skypeAction)
        {
            case ACTION_CALL:
                mySkypeUri += personId + "?call";
                break;
            case ACTION_CHAT:
                mySkypeUri += personId + "?chat";
                break;
            case ACTION_VIDEO_CALL:
                mySkypeUri += personId + "?call&video=true";
                break;
            default:
                break;
        }

        // Create the Intent from our Skype URI
        Uri skypeUri = Uri.parse(mySkypeUri);
        Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);

        // Restrict the Intent to being handled by the Skype for Android client
        // only
        myIntent.setComponent(new ComponentName("com.skype.raider", "com.skype.raider.Main"));
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Initiate the Intent. It should never fail since we've already
        // established the
        // presence of its handler (although there is an extremely minute window
        // where that
        // handler can go away...)
        if (myIntent.resolveActivity(activity.getPackageManager()) == null)
        {
            AlfrescoNotificationManager.getInstance(activity).showAlertCrouton((FragmentActivity) activity,
                    activity.getString(R.string.feature_disable));
            return;
        }
        activity.startActivity(myIntent);
    }

    /**
     * Determine whether the Skype for Android client is installed on this
     * device.
     */
    public boolean isSkypeClientInstalled(Context myContext)
    {
        PackageManager myPackageMgr = myContext.getPackageManager();
        try
        {
            myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            return (false);
        }
        return (true);
    }

    /**
     * Install the Skype client through the market: URI scheme.
     */
    public void goToMarket(Context myContext)
    {
        try
        {
            Uri marketUri = Uri.parse("market://details?type=com.skype.raider");
            Intent myIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (myIntent.resolveActivity(myContext.getPackageManager()) == null)
            {
                AlfrescoNotificationManager.getInstance(myContext).showAlertCrouton((FragmentActivity) myContext,
                        myContext.getString(R.string.feature_disable));
                return;
            }
            myContext.startActivity(myIntent);
        }
        catch (ActivityNotFoundException e)
        {

        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
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
            viewConfigModel = new UserProfileConfigModel(configuration);
            templateArguments = new String[] { UserProfileConfigModel.ARGUMENT_USERNAME };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder personId(String personIdentifier)
        {
            extraConfiguration.putString(ARGUMENT_USERNAME, personIdentifier);
            return this;
        }

        public Builder accountId(Long accountId)
        {
            extraConfiguration.putLong(ARGUMENT_ACCOUNTID, accountId);
            return this;
        }
    }
}
