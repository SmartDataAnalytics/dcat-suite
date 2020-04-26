package org.aksw.dcat_suite.service;

public interface ServiceMgr {
    ServiceInstance findService(String name);

    ServiceCreator getServiceCreator(String type);

    //createService(String name);
}
