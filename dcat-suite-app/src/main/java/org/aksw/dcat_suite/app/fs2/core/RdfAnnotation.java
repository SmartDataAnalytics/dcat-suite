package org.aksw.dcat_suite.app.fs2.core;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RdfAnnotation
    extends Resource
{
    @IriType
    @IriNs("eg")
    String getAnnotationTarget();
    RdfAnnotation setAnnotationTarget(String target);
}
