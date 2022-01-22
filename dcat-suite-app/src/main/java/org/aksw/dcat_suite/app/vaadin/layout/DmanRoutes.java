package org.aksw.dcat_suite.app.vaadin.layout;

public class DmanRoutes {
    public static final String HOME = "home";
    public static final String NEW_DATA_PROJECT = "new";
    public static final String CONNECTIONS = "connections";
    public static final String BROWSE = "browse";


    public static final String GROUP = "group";


    public static String group(String groupId) {
        return GROUP + "/" + groupId;
    }
}
