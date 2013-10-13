/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.properties;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.operations.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.PropertyManager;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MetadataFragment extends BaseFragment
{
    public static final String TAG = "MetadataFragment";

    public static final String ARGUMENT_NODE = "node";

    public static final String ARGUMENT_NODE_PARENT = "nodeParent";

    protected static final String ARGUMENT_ISFAVORITE = "isFavorite";

    protected Folder parentNode;

    protected Node node;

    protected boolean isRestrictable = false;

    public static Bundle createBundleArgs(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    public static Bundle createBundleArgs(Node node, Folder parentNode)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        args.putSerializable(ARGUMENT_NODE_PARENT, parentNode);
        return args;
    }

    public static MetadataFragment newInstance(Node node, Folder parentNode)
    {
        MetadataFragment bf = new MetadataFragment();
        bf.setArguments(createBundleArgs(node, parentNode));
        return bf;
    }

    public static MetadataFragment newInstance(Node n)
    {
        MetadataFragment bf = new MetadataFragment();
        bf.setArguments(createBundleArgs(n));
        return bf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        if (!getArguments().containsKey(ARGUMENT_ISFAVORITE) && !(node instanceof NodeSyncPlaceHolder))
        {
            SessionUtils.checkSession(getActivity(), alfSession);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);
        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }
        alfSession = SessionUtils.getSession(getActivity());

        node = (Node) getArguments().get(ARGUMENT_NODE);
        parentNode = (Folder) getArguments().get(ARGUMENT_NODE_PARENT);

        ScrollView sv = new ScrollView(getActivity());
        sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        sv.setPadding(5, 5, 5, 0);

        if (!getArguments().containsKey(ARGUMENT_ISFAVORITE) && !(node instanceof NodeSyncPlaceHolder))
        {
            if (alfSession == null) { return sv; }
        }

        isRestrictable = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);

        LinearLayout v = new LinearLayout(getActivity());
        v.setOrientation(LinearLayout.VERTICAL);
        v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        v.setGravity(Gravity.CENTER);

        ViewGroup grouprootview = (ViewGroup) inflater.inflate(R.layout.app_properties, v);
        grouprootview = (ViewGroup) grouprootview.findViewById(R.id.metadata);

        // Description
        Integer generalPropertyTitle = null;
        TextView tv = (TextView) v.findViewById(R.id.description);
        List<String> filter = new ArrayList<String>();
        if (node.getDescription() != null && node.getDescription().length() > 0)
        {
            v.findViewById(R.id.description_group).setVisibility(View.VISIBLE);
            ((TextView) v.findViewById(R.id.description_title)).setText(R.string.metadata_general);
            tv.setText(node.getDescription());
            generalPropertyTitle = -1;
            ((TextView) v.findViewById(R.id.prop_name_value)).setText(node.getName());
            filter.add(ContentModel.PROP_NAME);
        }
        else
        {
            v.findViewById(R.id.description_group).setVisibility(View.GONE);
            generalPropertyTitle = R.string.metadata_general;
        }

        // ASPECTS
        ViewGroup generalGroup = createAspectPanel(inflater, grouprootview, node, ContentModel.ASPECT_GENERAL, false,
                generalPropertyTitle, filter);
        addPathProperty(generalGroup, inflater);
        View vr = addPropertyLine(generalGroup, inflater, node, R.string.metadata_prop_creator, PropertyIds.CREATED_BY,
                null, false);

        // SAMSUNG Specific
        RenditionManager renditionManager = ApplicationManager.getInstance(getActivity()).getRenditionManager(
                getActivity());

        createAspectPanel(inflater, grouprootview, node, ContentModel.ASPECT_GEOGRAPHIC);
        createAspectPanel(inflater, grouprootview, node, ContentModel.ASPECT_EXIF);
        createAspectPanel(inflater, grouprootview, node, ContentModel.ASPECT_AUDIO);
        createAspectPanel(inflater, grouprootview, node, ContentModel.ASPECT_RESTRICTABLE);

        sv.addView(v);

        return sv;
    }

    protected View addPropertyLine(ViewGroup generalGroup, LayoutInflater inflater, Node node, int propertyLabel,
            String propertyId, OnClickListener listener, boolean isUnderline)
    {
        View vr = inflater.inflate(R.layout.sdk_property_row, null);
        TextView tv = (TextView) vr.findViewById(R.id.propertyName);
        tv.setText(propertyLabel);
        tv = (TextView) vr.findViewById(R.id.propertyValue);
        tv.setText((String) node.getPropertyValue(propertyId));
        tv.setClickable(true);
        tv.setFocusable(true);
        if (isUnderline)
        {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
        tv.setTag(node);
        tv.setOnClickListener(listener);
        generalGroup.addView(vr);
        return vr;
    }

    protected void addPathProperty(ViewGroup generalGroup, LayoutInflater inflater)
    {
        // Add Path
        if (parentNode != null || node.isFolder())
        {
            Node tmpNode = (parentNode != null) ? parentNode : node;
            addPropertyLine(generalGroup, inflater, tmpNode, R.string.metadata_prop_path, PropertyIds.PATH,
                    new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (getActivity() instanceof BaseActivity)
                            {
                                ((BaseActivity) getActivity()).addBrowserFragment((String) ((Folder) v.getTag())
                                        .getPropertyValue(PropertyIds.PATH));
                            }
                        }
                    }, true);
        }
    }

    protected ViewGroup createAspectPanel(LayoutInflater inflater, ViewGroup parentview, Node node, String aspect,
            boolean check, Integer overrideAspectTitle, List<String> filters)
    {
        ViewGroup groupview = null;
        if (!check || node.hasAspect(aspect))
        {
            View v = null;
            TextView tv = null;

            ViewGroup grouprootview = (ViewGroup) inflater.inflate(R.layout.sdk_property_title, null);
            tv = (TextView) grouprootview.findViewById(R.id.title);
            if (overrideAspectTitle == null)
            {
                tv.setText(PropertyManager.getAspectLabel(aspect));
            }
            else if (overrideAspectTitle == -1)
            {
                tv.setVisibility(View.GONE);
            }
            else
            {
                tv.setText(overrideAspectTitle);
            }

            groupview = (ViewGroup) grouprootview.findViewById(R.id.group_panel);
            for (Entry<String, Integer> map : PropertyManager.getPropertyLabel(aspect).entrySet())
            {
                if (node.getProperty(map.getKey()) != null && node.getProperty(map.getKey()).getValue() != null
                        && !filters.contains(map.getKey()))
                {
                    v = inflater.inflate(R.layout.sdk_property_row, null);
                    tv = (TextView) v.findViewById(R.id.propertyName);
                    tv.setText(map.getValue());
                    tv = (TextView) v.findViewById(R.id.propertyValue);
                    if (PropertyType.DATETIME.equals(node.getProperty(map.getKey()).getType()))
                    {
                        tv.setText(DateFormat.getMediumDateFormat(getActivity()).format(
                                ((GregorianCalendar) node.getProperty(map.getKey()).getValue()).getTime())
                                + " "
                                + DateFormat.getTimeFormat(getActivity()).format(
                                        ((GregorianCalendar) node.getProperty(map.getKey()).getValue()).getTime()));
                    }
                    else
                    {
                        tv.setText(node.getProperty(map.getKey()).getValue().toString());
                        if (isRestrictable && ContentModel.PROP_OFFLINE_EXPIRES_AFTER.equals(map.getKey()))
                        {
                            BigInteger expires = node.getProperty(map.getKey()).getValue();
                            if (expires.longValue() / 3600000 > 0)
                            {
                                tv.setText(String.format("%d", expires.longValue() / 3600000));
                            }
                            else
                            {
                                tv.setText(String.format("%s", expires.floatValue() / 3600000));
                            }
                        }
                    }
                    groupview.addView(v);
                }
            }
            parentview.addView(grouprootview);
        }
        return groupview;
    }

    protected void createAspectPanel(LayoutInflater inflater, ViewGroup parentview, Node node, String aspect)
    {
        createAspectPanel(inflater, parentview, node, aspect, true, null, new ArrayList<String>(0));
    }

    public Folder getParentNode()
    {
        return parentNode;
    }
}
