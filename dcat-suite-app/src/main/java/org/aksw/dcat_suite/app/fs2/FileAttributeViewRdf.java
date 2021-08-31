package org.aksw.dcat_suite.app.fs2;

import java.nio.file.attribute.FileAttributeView;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;

public interface FileAttributeViewRdf
    extends FileAttributeView, RdfEntityInfo
{
    @Override
    default String name() {
        return FileAttributeViewRdf.class.getName();
    }
}
