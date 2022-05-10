package org.aksw.dcat_suite.app.model.api;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

public interface LifeCycleEntityMgr<T extends LifeCycleEntity>
    extends HasBasePath
{
    // Dataset getDataset();

    T get(String name) throws IOException;
    Stream<Resource> list() throws IOException;
}
