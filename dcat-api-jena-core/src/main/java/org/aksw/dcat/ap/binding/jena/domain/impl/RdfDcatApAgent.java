package org.aksw.dcat.ap.binding.jena.domain.impl;

import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
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
