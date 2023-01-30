package org.aksw.dcat_suite.app.vaadin.layout;

public class DmanRoutes {
    public static final String HOME = "";
    public static final String NEW_DATA_PROJECT = "new";
    public static final String MY_PROJECTS = "projects";
    public static final String CONNECTIONS = "connections";
    public static final String CATALOGS = "catalogs";
    public static final String BROWSE = "browse";
    public static final String HISTORY = "history";

    public static final String CKAN_SEARCH = "ckanSearch";
    public static final String CKAN_IMPORT = "ckanImport";


    public static final String GROUP = "group";


    public static final String MCLIENT_DEMO = "mclient_demo";

    public static String group(String groupId) {
        return GROUP + "/" + groupId;
    }
}
