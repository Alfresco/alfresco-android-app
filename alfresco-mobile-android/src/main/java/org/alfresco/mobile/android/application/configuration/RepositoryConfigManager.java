package org.alfresco.mobile.android.application.configuration;

import org.alfresco.mobile.android.api.model.config.RepositoryConfig;
import org.alfresco.mobile.android.api.services.ConfigService;

import android.app.Activity;
import android.text.TextUtils;
import android.view.ViewGroup;

/**
 * Created by jpascal on 23/01/2015.
 */
public class RepositoryConfigManager extends BaseConfigManager {

    private static final String TAG = RepositoryConfigManager.class.getName();

    private RepositoryConfig repoConfig;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public RepositoryConfigManager(Activity activity, ConfigService configService, ViewGroup vRoot)
    {
        super(activity, configService);
        if (configService != null && configService.getRepositoryConfig() != null)
        {
            repoConfig = configService.getRepositoryConfig();
        }
        this.vRoot = vRoot;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLCI METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasShareUrl(){
        return !TextUtils.isEmpty(repoConfig.getShareURL());
    }

    public String getShareUrl(){
        return repoConfig.getShareURL();
    }

}
