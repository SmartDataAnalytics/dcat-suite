package org.aksw.dcat_suite.service;

public interface ServiceConfig {
    //Map<String, String> getEnv();
    ServiceConfig setProperty(String key, String value);
    ServiceConfig unsetProperty(String key);
    String getProperty(String key);

}
