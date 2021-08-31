package org.aksw.dcat_suite.mgmt.domain;

import java.util.List;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

@RdfType("eg:DataProject")
@ResourceView
public interface DataProject
    extends RdfsLabel
{
    @Iri("rdfs:member")
    <T extends Resource> List<T> getDatasets(Class<T> clz);

    @Iri("rdfs:member")
    List<DcatDataset> getDcatDatasets();

}
