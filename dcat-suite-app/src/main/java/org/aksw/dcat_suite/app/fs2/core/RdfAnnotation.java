package org.aksw.dcat_suite.app.fs2.core;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
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
