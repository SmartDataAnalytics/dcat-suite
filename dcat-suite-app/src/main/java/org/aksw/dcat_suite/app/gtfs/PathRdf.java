package org.aksw.dcat_suite.app.gtfs;

import java.nio.file.Path;

import org.apache.jena.rdf.model.Resource;

public interface PathRdf
    extends Path
{
    Resource getAnnotation(String qualifier);
}
