package org.aksw.dcat_suite.mgmt.domain;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RdfsLabel
    extends Resource
{
    @Iri("rdfs:label")
    String getLabel();
    RdfsLabel setLabel(String label);
}
