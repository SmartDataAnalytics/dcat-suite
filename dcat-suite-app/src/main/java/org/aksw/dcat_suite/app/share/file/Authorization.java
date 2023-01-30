package org.aksw.dcat_suite.app.share.file;

import org.apache.jena.datatypes.xsd.impl.XSDTimeType;
import org.apache.jena.rdf.model.Resource;

public interface Authorization {
    Resource getEntity();

    String getUserId();
    XSDTimeType getCreationDate();
    Boolean isWriteAllowed();
}
