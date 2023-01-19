package org.aksw.dcat_suite.app.share.file;

import java.util.Map;

import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.apache.tomcat.util.http.parser.Authorization;

public interface FileShare {
    @KeyIri("http://example.org/entity")
    String getTarget();

    @KeyIri("http://example.org/userId")
    Map<String, Authorization> getAuthorizedUsers();

}
