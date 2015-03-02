package org.alfresco.mobile.android.platform.mdm;

import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.extensions.MobileIronManager;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.BundleUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.RestrictionsManager;
import android.os.Build;
import android.os.Bundle;

/**
 * Created by jpascal on 26/02/2015.
 * 
 * @since 1.5
 */
public class MDMManager extends Manager implements MDMConstants
{
    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // ///////////////////////////////////////////////////////////////////////////
    protected MobileIronManager mobileIronManager;

    protected RestrictionsManager restrictionsManager;

    protected Bundle restrictions = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static MDMManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new MDMManager(context);
            }

            return (MDMManager) mInstance;
        }
    }

    protected MDMManager(Context applicationContext)
    {
        super(applicationContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasConfig()
    {
        // MobileIron enforce some configuration
        if (mobileIronManager != null) { return true; }

        // Android for Work doesn't enforce configuration
        return (restrictions != null && !restrictions.isEmpty());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void requestConfig(Activity activity, String applicationId)
    {
        // Which Provider ?
        if (MobileIronManager.getInstance(activity) != null)
        {
            // MobileIron Build
            mobileIronManager = MobileIronManager.getInstance(activity);
            mobileIronManager.requestConfig(activity, applicationId);
        }
        else if (AndroidVersion.isLollipopOrAbove())
        {
            // Android For Work
            restrictionsManager = (RestrictionsManager) activity.getSystemService(Context.RESTRICTIONS_SERVICE);
            restrictions = restrictionsManager.getApplicationRestrictions();
            if (restrictions != null && !restrictions.isEmpty())
            {
                EventBusManager.getInstance().post(new MDMEvent());
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SPECIFIC KEY
    // ///////////////////////////////////////////////////////////////////////////
    public String getAlfrescoURL()
    {
        return getConfig(MDMConstants.ALFRESCO_REPOSITORY_URL);
    }

    public String getUsername()
    {
        return getConfig(MDMConstants.ALFRESCO_USERNAME);
    }

    public String getShareURL()
    {
        return getConfig(MDMConstants.ALFRESCO_SHARE_URL);
    }

    public String getDescription()
    {
        return getConfig(MDMConstants.ALFRESCO_DISPLAY_NAME);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONFIG
    // ///////////////////////////////////////////////////////////////////////////
    public void setConfig(Bundle b)
    {
        if (b == null) { return; }
        if (mobileIronManager != null)
        {
            mobileIronManager.setConfig(b);
        }
        else if (restrictions != null)
        {
            BundleUtils.addIfNotEmpty(restrictions, b);
        }
    }

    public String getConfig(String id)
    {
        if (mobileIronManager != null)
        {
            return (String) mobileIronManager.getConfig(id);
        }
        else if (restrictions != null)
        {
            return restrictions.getString(id);
        }
        else
        {
            return "";
        }
    }
}
