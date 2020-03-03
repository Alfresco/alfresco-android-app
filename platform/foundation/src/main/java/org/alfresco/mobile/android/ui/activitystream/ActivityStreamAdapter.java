/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.ui.activitystream;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.CloudConstant;
import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.ActivityEntry;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesCaptionViewHolder;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

/**
 * Provides access to activity entries and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class ActivityStreamAdapter extends BaseListAdapter<ActivityEntry, TwoLinesCaptionViewHolder>
{
    protected List<ActivityEntry> selectedItems;

    protected RenditionManager renditionManager;

    protected WeakReference<FragmentActivity> activityRef;

    protected List<ActivityEntry> selectedOptionItems = new ArrayList<>();

    public ActivityStreamAdapter(Fragment fr, int textViewResourceId,
            List<ActivityEntry> listItems, List<ActivityEntry> selectedItems)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.vhClassName = TwoLinesCaptionViewHolder.class.getCanonicalName();
        this.selectedItems = selectedItems;
        this.activityRef = new WeakReference<>(fr.getActivity());
    }

    @Override
    protected void updateTopText(TwoLinesCaptionViewHolder vh, ActivityEntry item)
    {
        vh.topText.setText(getUser(item));
        vh.bottomText.setText(Html.fromHtml(getActivityTypeMessage(item)));
        HolderUtils.makeMultiLine(vh.bottomText, 3);
    }

    @Override
    protected void updateBottomText(TwoLinesCaptionViewHolder vh, ActivityEntry item)
    {
        String s = "";
        if (item.getCreatedAt() != null)
        {
            s = formatDate(getContext(), item.getCreatedAt().getTime());
        }
        vh.topTextRight.setText(s);

        if (selectedItems != null && selectedItems.contains(item))
        {
            ((ViewGroup) vh.icon.getParent()).setBackgroundResource(R.drawable.list_longpressed_holo);
        }
        else
        {
            UIUtils.setBackground(((ViewGroup) vh.icon.getParent()), null);
        }
    }

    @Override
    protected void updateIcon(TwoLinesCaptionViewHolder vh, ActivityEntry item)
    {
        getCreatorAvatar(vh, item);
        AccessibilityUtils.addContentDescription(vh.icon,
                String.format(getContext().getString(R.string.contact_card), getUser(item)));
    }

    private void getCreatorAvatar(TwoLinesCaptionViewHolder vh, ActivityEntry item)
    {
        String type = item.getType();
        String tmp = null;
        vh.icon.setVisibility(View.VISIBLE);

        if (type.startsWith(PREFIX_FILE))
        {
            RenditionManager.with(activityRef.get()).loadAvatar(item.getCreatedBy()).placeHolder(R.drawable.ic_person_light)
                    .into(vh.icon);
        }
        else if (type.startsWith(PREFIX_GROUP))
        {
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(getFileDrawableId(item)));
        }
        else if (type.startsWith(PREFIX_USER))
        {
            tmp = getData(item, CloudConstant.MEMEBERUSERNAME_VALUE);
            if (tmp.isEmpty())
            {
                tmp = getData(item, CloudConstant.MEMEBERPERSONID_VALUE);
            }
            RenditionManager.with(activityRef.get()).loadAvatar(tmp).placeHolder(R.drawable.ic_person_light).into(vh.icon);
        }
        else if (type.startsWith(PREFIX_SUBSCRIPTION))
        {
            tmp = getData(item, CloudConstant.FOLLOWERUSERNAME_VALUE);
            if (tmp.isEmpty())
            {
                tmp = item.getCreatedBy();
            }
            RenditionManager.with(activityRef.get()).loadAvatar(tmp).placeHolder(R.drawable.ic_person_light).into(vh.icon);
        }
        else
        {
            RenditionManager.with(activityRef.get()).loadAvatar(item.getCreatedBy())
                    .placeHolder(R.drawable.ic_person_light).into(vh.icon);
        }
    }

    private int getFileDrawableId(ActivityEntry item)
    {
        int drawable = R.drawable.ic_menu_notif;
        String s = item.getType();

        if (s.startsWith(PREFIX_FILE))
        {
            drawable = MimeTypeManager.getInstance(getContext()).getIcon(getData(item, OnPremiseConstant.TITLE_VALUE));
        }
        else
        {
            for (Entry<String, Integer> icon : EVENT_ICON.entrySet())
            {
                if (s.startsWith(icon.getKey()))
                {
                    drawable = icon.getValue();
                    break;
                }
            }
        }
        return drawable;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TYPE
    // ///////////////////////////////////////////////////////////////////////////
    public static final String PREFIX_LINK = "org.alfresco.links.link";

    public static final String PREFIX_EVENT = "org.alfresco.calendar.event";

    public static final String PREFIX_WIKI = "org.alfresco.wiki.page";

    public static final String PREFIX_FILE = "org.alfresco.documentlibrary.file";

    public static final String PREFIX_USER = "org.alfresco.site.user";

    public static final String PREFIX_DATALIST = "org.alfresco.datalists.list";

    public static final String PREFIX_DISCUSSIONS = "org.alfresco.discussions";

    public static final String PREFIX_FOLDER = "org.alfresco.documentlibrary.folder";

    public static final String PREFIX_COMMENT = "org.alfresco.comments.comment";

    public static final String PREFIX_BLOG = "org.alfresco.blog";

    public static final String PREFIX_SUBSCRIPTION = "org.alfresco.subscriptions";

    public static final String PREFIX_GROUP = "org.alfresco.site.group";

    @SuppressWarnings("serial")
    private static final Map<String, Integer> EVENT_ICON = new HashMap<String, Integer>()
    {
        {
            put(PREFIX_LINK, R.drawable.ic_menu_share);
            put(PREFIX_EVENT, R.drawable.ic_menu_today);
            put(PREFIX_WIKI, R.drawable.ic_menu_notif);
            put(PREFIX_USER, R.drawable.ic_person_light);
            put(PREFIX_DATALIST, R.drawable.ic_menu_notif);
            put(PREFIX_DISCUSSIONS, R.drawable.ic_action_dialog);
            put(PREFIX_FOLDER, R.drawable.ic_menu_archive);
            put(PREFIX_COMMENT, R.drawable.ic_action_dialog);
            put(PREFIX_BLOG, R.drawable.ic_menu_notif);
            put(PREFIX_SUBSCRIPTION, R.drawable.ic_menu_notif);
            put(PREFIX_GROUP, R.drawable.ic_menu_notif);
        }
    };

    //
    private static final String ARGUMENT_TITLE = "{0}";

    private static final String ARGUMENT_USER_PROFILE = "{1}";

    private static final String ARGUMENT_CUSTOM = "{2}";

    private static final String ARGUMENT_SITE_LINK = "{4}";

    private static final String ARGUMENT_SUBSCRIBER = "{5}";

    private static final String ARGUMENT_STATUS = "{6}";

    private String getUser(ActivityEntry item)
    {
        String s = item.getType();
        String username = item.getCreatedBy();
        if (MAP_ACTIVITY_TYPE.get(s) != null)
        {
            s = getContext().getString(MAP_ACTIVITY_TYPE.get(item.getType()));

            if (s.contains(ARGUMENT_CUSTOM))
            {
                username = getData(item, OnPremiseConstant.MEMEBERFIRSTNAME_VALUE) + " "
                        + getData(item, OnPremiseConstant.MEMBERLASTNAME_VALUE);
            }
            else
            {
                username = getData(item, OnPremiseConstant.FIRSTNAME_VALUE) + " "
                        + getData(item, OnPremiseConstant.LASTNAME_VALUE);
            }
        }
        return username;
    }

    private static final String MANAGER = "SiteManager";

    private static final String COLLABORATOR = "SiteCollaborator";

    private static final String CONSUMER = "SiteConsumer";

    private static final String CONTRIBUTOR = "SiteContributor";

    private String getRoleDisplayName(String role)
    {
        if (role == null) { return ""; }
        if (MANAGER.equals(role)) { return getContext().getString(R.string.activity_role_SiteManager); }
        if (COLLABORATOR.equals(role)) { return getContext().getString(R.string.activity_role_SiteCollaborator); }
        if (CONSUMER.equals(role)) { return getContext().getString(R.string.activity_role_SiteConsumer); }
        if (CONTRIBUTOR.equals(role)) { return getContext().getString(R.string.activity_role_SiteContributor); }
        return getContext().getString(R.string.activity_role_None);
    }

    private String getActivityTypeMessage(ActivityEntry item)
    {
        String s = item.getType();
        if (MAP_ACTIVITY_TYPE.get(s) != null)
        {
            s = getContext().getResources().getString(MAP_ACTIVITY_TYPE.get(item.getType()));

            if (s.contains(ARGUMENT_CUSTOM))
            {
                s = s.replace(ARGUMENT_CUSTOM, getRoleDisplayName(getData(item, OnPremiseConstant.ROLE_VALUE)));
                s = s.replace(ARGUMENT_USER_PROFILE, "<b>" + getData(item, OnPremiseConstant.MEMEBERFIRSTNAME_VALUE)
                        + " " + getData(item, OnPremiseConstant.MEMBERLASTNAME_VALUE) + "</b>");
            }
            else
            {
                s = s.replace(ARGUMENT_USER_PROFILE, "<b>" + getData(item, OnPremiseConstant.FIRSTNAME_VALUE) + " "
                        + getData(item, OnPremiseConstant.LASTNAME_VALUE) + "</b>");
            }

            if (s.contains(ARGUMENT_TITLE))
            {
                s = s.replace(ARGUMENT_TITLE, "<b>" + getData(item, OnPremiseConstant.TITLE_VALUE) + "</b>");
            }

            if (s.contains(ARGUMENT_SITE_LINK))
            {
                s = s.replace(ARGUMENT_SITE_LINK, item.getSiteShortName() != null ? item.getSiteShortName() : "");
            }

            if (s.contains(ARGUMENT_STATUS))
            {
                s = s.replace(ARGUMENT_STATUS, getData(item, OnPremiseConstant.STATUS_VALUE));
            }

            if (s.contains(ARGUMENT_SUBSCRIBER))
            {
                s = s.replace(ARGUMENT_SUBSCRIBER, "<b>" + getData(item, OnPremiseConstant.USERFIRSTNAME_VALUE) + " "
                        + getData(item, OnPremiseConstant.USERLASTNAME_VALUE) + "</b>");
            }
        }
        return s;
    }

    private String getData(ActivityEntry entry, String key)
    {
        String value = "";
        if (entry == null) { return value; }

        value = entry.getData(key);
        if (value == null)
        {
            value = "";
        }
        return value;
    }

    @SuppressWarnings("serial")
    private static final Map<String, Integer> MAP_ACTIVITY_TYPE = new HashMap<String, Integer>()
    {
        {
            put("org.alfresco.blog.post-created", R.string.org_alfresco_blog_post_created);
            put("org.alfresco.blog.post-updated", R.string.org_alfresco_blog_post_updated);
            put("org.alfresco.blog.post-deleted", R.string.org_alfresco_blog_post_deleted);
            put("org.alfresco.comments.comment-created", R.string.org_alfresco_comments_comment_created);
            put("org.alfresco.comments.comment-updated", R.string.org_alfresco_comments_comment_updated);
            put("org.alfresco.comments.comment-deleted", R.string.org_alfresco_comments_comment_deleted);
            put("org.alfresco.discussions.post-created", R.string.org_alfresco_discussions_post_created);
            put("org.alfresco.discussions.post-updated", R.string.org_alfresco_discussions_post_updated);
            put("org.alfresco.discussions.post-deleted", R.string.org_alfresco_discussions_post_deleted);
            put("org.alfresco.discussions.reply-created", R.string.org_alfresco_discussions_reply_created);
            put("org.alfresco.discussions.reply-updated", R.string.org_alfresco_discussions_reply_updated);
            put("org.alfresco.calendar.event-created", R.string.org_alfresco_calendar_event_created);
            put("org.alfresco.calendar.event-updated", R.string.org_alfresco_calendar_event_updated);
            put("org.alfresco.calendar.event-deleted", R.string.org_alfresco_calendar_event_deleted);
            put("org.alfresco.documentlibrary.file-added", R.string.org_alfresco_documentlibrary_file_added);
            put("org.alfresco.documentlibrary.files-added", R.string.org_alfresco_documentlibrary_files_added);
            put("org.alfresco.documentlibrary.file-created", R.string.org_alfresco_documentlibrary_file_created);
            put("org.alfresco.documentlibrary.file-deleted", R.string.org_alfresco_documentlibrary_file_deleted);
            put("org.alfresco.documentlibrary.files-deleted", R.string.org_alfresco_documentlibrary_files_deleted);
            put("org.alfresco.documentlibrary.file-updated", R.string.org_alfresco_documentlibrary_file_updated);
            put("org.alfresco.documentlibrary.files-updated", R.string.org_alfresco_documentlibrary_files_updated);
            put("org.alfresco.documentlibrary.folder-added", R.string.org_alfresco_documentlibrary_folder_added);
            put("org.alfresco.documentlibrary.folder-deleted", R.string.org_alfresco_documentlibrary_folders_deleted);
            put("org.alfresco.documentlibrary.folders-added", R.string.org_alfresco_documentlibrary_folder_added);
            put("org.alfresco.documentlibrary.folders-deleted", R.string.org_alfresco_documentlibrary_folders_deleted);
            put("org.alfresco.documentlibrary.google-docs-checkout",
                    R.string.org_alfresco_documentlibrary_google_docs_checkout);
            put("org.alfresco.documentlibrary.google-docs-checkin",
                    R.string.org_alfresco_documentlibrary_google_docs_checkin);
            put("org.alfresco.documentlibrary.inline-edit", R.string.org_alfresco_documentlibrary_inline_edit);
            put("org.alfresco.documentlibrary.file-liked", R.string.org_alfresco_documentlibrary_file_liked);
            put("org.alfresco.documentlibrary.folder-liked", R.string.org_alfresco_documentlibrary_folder_liked);
            put("org.alfresco.wiki.page-created", R.string.org_alfresco_wiki_page_created);
            put("org.alfresco.wiki.page-edited", R.string.org_alfresco_wiki_page_edited);
            put("org.alfresco.wiki.page-renamed", R.string.org_alfresco_wiki_page_renamed);
            put("org.alfresco.wiki.page-deleted", R.string.org_alfresco_wiki_page_deleted);
            put("org.alfresco.site.group-added", R.string.org_alfresco_site_group_added);
            put("org.alfresco.site.group-removed", R.string.org_alfresco_site_group_removed);
            put("org.alfresco.site.group-role_changed", R.string.org_alfresco_site_group_role_changed);
            put("org.alfresco.site.user-joined", R.string.org_alfresco_site_user_joined);
            put("org.alfresco.site.user-left", R.string.org_alfresco_site_user_left);
            put("org.alfresco.site.user-role-changed", R.string.org_alfresco_site_user_role_changed);
            put("org.alfresco.links.link-created", R.string.org_alfresco_links_link_created);
            put("org.alfresco.links.link-updated", R.string.org_alfresco_links_link_updated);
            put("org.alfresco.links.link-deleted", R.string.org_alfresco_links_link_deleted);
            put("org.alfresco.datalists.list-created", R.string.org_alfresco_datalists_list_created);
            put("org.alfresco.datalists.list-updated", R.string.org_alfresco_datalists_list_updated);
            put("org.alfresco.datalists.list-deleted", R.string.org_alfresco_datalists_list_deleted);
            put("org.alfresco.subscriptions.followed", R.string.org_alfresco_subscriptions_followed);
            put("org.alfresco.subscriptions.subscribed", R.string.org_alfresco_subscriptions_subscribed);
            put("org.alfresco.profile.status-changed", R.string.org_alfresco_profile_status_changed);
            put("org.alfresco.documentlibrary.file-previewed", R.string.org_alfresco_documentlibrary_file_previewed);
            put("org.alfresco.documentlibrary.file-downloaded", R.string.org_alfresco_documentlibrary_file_downloaded);
        }
    };
}
