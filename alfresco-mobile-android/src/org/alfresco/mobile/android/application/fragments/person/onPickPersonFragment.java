package org.alfresco.mobile.android.application.fragments.person;

import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;

public interface onPickPersonFragment
{
    void onSelect(Map<String, Person> p);
    Map<String, Person> retrieveSelection();
}
