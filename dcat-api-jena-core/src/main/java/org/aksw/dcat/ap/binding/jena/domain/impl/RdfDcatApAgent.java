package org.aksw.dcat.ap.binding.jena.domain.impl;

import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.apache.jena.rdf.model.Resource;

public interface RdfDcatApAgent
    extends Resource, DcatApAgent
{
    @IriNs("foaf")
    String getName();

    @IriNs("foaf")
    String getMbox();

    @IriNs("foaf")
    @IriType
    String homepage();

    @IriNs("dcterms")
    public String type();
}
