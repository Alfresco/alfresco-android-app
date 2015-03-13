package org.alfresco.mobile.android.api.model.config;

import java.util.ArrayList;
import java.util.List;

public interface ConfigConstants
{
    // ///////////////////////////////////////////////////////////////////////////
    // VERSION
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // DATA DICTIONNARY
    // ///////////////////////////////////////////////////////////////////////////
    String DATA_DICTIONARY = "Data Dictionary";

    String CONFIG_APPLICATION_FOLDER_PATH = "Mobile";

    String CONFIG_FILENAME = "configuration.json";

    String CONFIG_LOCALIZATION_FOLDER_PATH = "Messages/";

    String CONFIG_LOCALIZATION_FILENAME = "strings.properties";

    String CONFIG_LOCALIZATION_FILENAME_PATTERN = "strings_%s.properties";

    @SuppressWarnings("serial")
    List<String> DATA_DICTIONNARY_LIST = new ArrayList<String>(8)
    {
        {
            add("Data Dictionary");// UK,JA
            add("Dictionnaire de données");// FR
            add("Datenverzeichnis");// DE
            add("Diccionario de datos");// ES
            add("Dizionario dei dati");// IT
            add("Dataordbok");// Nb NO
            add("Gegevenswoordenboek");// NL
            add("Dicionário de dados");// PT
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // BETA MODEL
    // ///////////////////////////////////////////////////////////////////////////
    String DATA_DICTIONNARY_MOBILE_PATH = "Mobile/configuration.json";

    String CATEGORY_ROOTMENU = "rootMenu";

    String MENU_ACTIVITIES = "com.alfresco.activities";

    String MENU_REPOSITORY = "com.alfresco.repository";

    String MENU_SITES = "com.alfresco.sites";

    String MENU_TASKS = "com.alfresco.tasks";

    String MENU_FAVORITES = "com.alfresco.favorites";

    String MENU_SEARCH = "com.alfresco.search";

    String MENU_LOCAL_FILES = "com.alfresco.localFiles";

    String MENU_NOTIFICATIONS = "com.alfresco.notifications";

    String MENU_SHARED = "com.alfresco.repository.shared";

    String MENU_MYFILES = "com.alfresco.repository.userhome";

    String PROP_VISIBILE = "visible";

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW MODEL 1.0
    // ///////////////////////////////////////////////////////////////////////////
    String VIEW_MODEL_ACTIVITIES = "org.alfresco.client.view.activities";

    String VIEW_MODEL_PEOPLE = "org.alfresco.client.view.people";

    String VIEW_MODEL_PERSON_PROFILE = "org.alfresco.client.view.person-profile";

    String VIEW_MODEL_LOCAL_FILES = "org.alfresco.client.view.local-files";

    String VIEW_MODEL_SEARCH = "org.alfresco.client.view.search";

    String VIEW_MODEL_SEARCH_ADVANCED = "org.alfresco.client.view.search-advanced";

    String VIEW_MODEL_SITE_BROWSER = "org.alfresco.client.view.site-browser";

    String VIEW_MODEL_SITES = "org.alfresco.client.view.sites";

    String VIEW_MODEL_NODE_DETAILS = "org.alfresco.client.view.node-details";

    String VIEW_MODEL_PREVIEW_CAROUSEL = "org.alfresco.client.view.preview-carousel";

    String VIEW_MODEL_REPOSITORY = "org.alfresco.client.view.repository";

    String VIEW_MODEL_REPOSITORY_SEARCH = "org.alfresco.client.view.repository-search";

    String VIEW_MODEL_TASKS = "org.alfresco.client.view.tasks";

    String VIEW_MODEL_FAVORITES = "org.alfresco.client.view.favorites";

    String VIEW_MODEL_SYNC = "org.alfresco.client.view.sync";

    // ///////////////////////////////////////////////////////////////////////////
    // DEFAULT VIEW
    // ///////////////////////////////////////////////////////////////////////////

    String VIEW_ROOT_NAVIGATION_MENU = "rootNavigationMenu";

    String VIEW_NODE_PROPERTIES = "view-properties";

    String VIEW_EDIT_PROPERTIES = "edit-properties";

    // ///////////////////////////////////////////////////////////////////////////
    // PARSING
    // ///////////////////////////////////////////////////////////////////////////
    // GENERAL
    String ID_VALUE = "id";

    String TYPE_VALUE = "type";

    String LABEL_ID_VALUE = "label-id";

    String DESCRIPTION_ID_VALUE = "description-id";

    String ICON_ID_VALUE = "icon-id";

    String FILTERS_VALUE = "filters";

    // PROFILES
    String PROFILES_VALUE = "profiles";

    String DEFAULT_VALUE = "default";

    // CONFIG INFO
    String SCHEMA_VERSION_VALUE = "schema-version";

    String CONFIG_VERSION_VALUE = "config-version";

    // VIEWS
    String ITEMS_VALUE = "items";

    String ITEM_TYPE_VALUE = "item-type";

    String VIEW_VALUE = "view";

    String VIEWS_VALUE = "views";

    String PARAMS_VALUE = "params";

    String VISIBILITY_VALUE = "visibility";

    String VISIBLE_VALUE = "visible";

    // EVALUATORS
    String EVALUATOR = "evaluator";

    String MATCH_ALL_VALUE = "match-all";

    String MATCH_ANY_VALUE = "match-any";

    String NEGATE_SYMBOL = "!";

    String CLOUD_VALUE = "cloud";

    String ONPREMISE_VALUE = "onpremise";

    String SITE_SHORTNAME_VALUE = "siteShortName";

    String USERNAME_VALUE = "userName";

    String KEYWORDS_VALUE = "keywords";

    // EVALUATOR REPOSITORY VERSION
    String OPERATOR_VALUE = "operator";

    String SESSION_VALUE = "session";

    String EDITION_VALUE = "edition";

    String MAJORVERSION_VALUE = "majorVersion";

    String MINORVERSION_VALUE = "minorVersion";

    String MAINTENANCEVERSION_VALUE = "maintenanceVersion";

    String TYPE_NAME_VALUE = "typeName";

    String ASPECT_NAME_VALUE = "aspectName";

    String PROPERTY_NAME_VALUE = "propertyName";

    String PROPERTYVALUE_VALUE = "propertyValue";

    String USERS_VALUE = "users";

    // FORMS
    String PARAMS_FORMS = "forms";

    String FIELD_GROUPS_VALUE = "field-groups";

    String FIELDS_VALUE = "fields";

    String FIELD_VALUE = "field";

    String CONTROL_TYPE_VALUE = "control-type";

    String CONTROL_PARAMS_VALUE = "control-params";

    String MODEL_ID_VALUE = "model-id";

    String LAYOUT_VALUE = "layout";

    // REPOSITORY
    String SHARE_URL_VALUE = "share-url";

    String CMIS_URL_VALUE = "cmis-url";

    // CREATION
    String MIME_TYPES_VALUE = "mime-types";

    String DOCUMENT_TYPES_VALUE = "document-types";

    String FOLDER_TYPES_VALUE = "folder-types";

    String ROOTVIEW_ID_VALUE = "root-view-id";

    String FOLDER_TYPE_ID = "folderTypeId";

    // VALIDATION
    String VALIDATION_VALUE = "validation";

    String VALIDATION_RULES_VALUE = "validation-rules";

    String ERROR_ID_VALUE = "error-id";

    String MIN_VALUE = "min";

    String MAX_VALUE = "max";

    String PATTERN_VALUE = "pattern";

    // FIELDS
    String MIN_DATE_VALUE = "minDate";

    String MAX_DATE_VALUE = "maxDate";

    String SHOW_TIME_VALUE = "showTime";

    String SHOW_MULTIPLE_LINES_VALUE = "showMultipleLines";

    String READ_ONLY_VALUE = "readOnly";

    String SECRET_VALUE = "secret";

    String AUTHORITY_VALUE = "authority";

    String ALLOW_MULTIPLE_SELECTION_VALUE = "allowMultipleSelection";

    String OUTPUT_VALUE = "outputValue";

    // /////////////////////////////////////////////////
    // VIEW ENUM
    // /////////////////////////////////////////////////
    enum ViewConfigType
    {
        VIEW_ID("view-id"), VIEW_GROUP_ID("view-group-id"), VIEW("view");

        /** The value associated to an enum. */
        private final String value;

        /**
         * Instantiates a new property type.
         * 
         * @param v the value of the enum.
         */
        ViewConfigType(String v)
        {
            value = v;
        }

        /**
         * Value.
         * 
         * @return the string
         */
        public String value()
        {
            return value;
        }

        /**
         * From value.
         * 
         * @param v the value of the enum.
         * @return the property type
         */
        public static ViewConfigType fromValue(String v)
        {
            for (ViewConfigType c : ViewConfigType.values())
            {
                if (c.value.equalsIgnoreCase(v)) { return c; }
            }
            return null;
        }
    }

    // /////////////////////////////////////////////////
    // FIELDTYPE ENUM
    // /////////////////////////////////////////////////
    enum FieldConfigType
    {
        FIELD_ID("field-id"), FIELD_GROUP_ID("field-group-id"), FIELD_GROUP("field-group"), FIELD("field");

        /** The value associated to an enum. */
        private final String value;

        FieldConfigType(String v)
        {
            value = v;
        }

        public String value()
        {
            return value;
        }

        public static FieldConfigType fromValue(String v)
        {
            for (FieldConfigType c : FieldConfigType.values())
            {
                if (c.value.equalsIgnoreCase(v)) { return c; }
            }
            return null;
        }
    }

    // /////////////////////////////////////////////////
    // VALIDATION ENUM
    // /////////////////////////////////////////////////
    enum ValidationConfigType
    {
        VALIDATION_RULE_ID("validation-rule-id"), VALIDATION_RULE("validation-rule");

        /** The value associated to an enum. */
        private final String value;

        ValidationConfigType(String v)
        {
            value = v;
        }

        public String value()
        {
            return value;
        }

        public static ValidationConfigType fromValue(String v)
        {
            for (ValidationConfigType c : ValidationConfigType.values())
            {
                if (c.value.equalsIgnoreCase(v)) { return c; }
            }
            return null;
        }
    }

}
