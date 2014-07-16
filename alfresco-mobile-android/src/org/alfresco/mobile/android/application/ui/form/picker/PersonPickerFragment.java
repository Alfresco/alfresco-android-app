package org.alfresco.mobile.android.application.ui.form.picker;

import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;

public class PersonPickerFragment
{
    // //////////////////////////////////////////////////////////////////////
    // INTERFACE
    // //////////////////////////////////////////////////////////////////////
    public interface onPickAuthorityFragment
    {
        void onPersonSelected(String fieldId, Map<String, Person> p);

        void onPersonClear(String fieldId);
        
        Map<String, Person> getPersonSelected(String fieldId);
    }
    
}
