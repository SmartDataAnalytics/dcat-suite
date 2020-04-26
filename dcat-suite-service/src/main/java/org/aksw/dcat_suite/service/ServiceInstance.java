package org.aksw.dcat_suite.service;

import com.google.common.util.concurrent.Service;

public interface ServiceInstance {
    Service getService();
    ServiceConfig getConfig();
}
