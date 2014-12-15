package org.alfresco.mobile.android.api.model.config;

import java.util.List;
/**
 *  Base type for Group Configuration.
 *
 * @author Jean Marie Pascal
 *
 */
public interface GroupConfig<T> extends ItemConfig
{
    /**
     * Returns a list of GroupConfig or ItemConfig objects.
     * 
     * @return
     */
    List<T> getItems();
}
