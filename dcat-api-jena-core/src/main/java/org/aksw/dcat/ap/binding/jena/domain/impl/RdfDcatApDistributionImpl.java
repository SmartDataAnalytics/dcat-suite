package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.time.Instant;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;

public interface RdfDcatApDistributionImpl
    extends DcatApDistribution
{
    @IriNs("dcterms")
    @Override
    RdfDcatApDistributionImpl setTitle(String title);

    @IriNs("dcterms")
    @Override
    RdfDcatApDistributionImpl setDescription(String description);

    @Iri("dcat:accessURL")
    @IriType
    @Override
    Set<String> getAccessUrls();

    @Iri("dcat:downloadURL")
    @IriType
    @Override
    Set<String> getDownloadUrls();

    @IriNs("dcat")
    @Override
    RdfDcatApDistributionImpl setMediaType(String mediaType);

    @IriNs("dcterms")
    @Override
    RdfDcatApDistributionImpl setFormat(String format);

    @IriNs("dcterms")
    @IriType
    @Override
    RdfDcatApDistributionImpl setLicense(String iri);

    @IriNs("adms")
    @IriType
    @Override
    RdfDcatApDistributionImpl setStatus(String iri);

    @IriNs("dcat")
    @Override
    RdfDcatApDistributionImpl setByteSize(Long byteSize);

    @IriNs("dcterms")
    @Override
    RdfDcatApDistributionImpl setIssued(Instant timestamp);
    // return create(this, DCTerms.issued, NodeMappers.from(Instant.class));


    @IriNs("dcterms")
    @Override
    RdfDcatApDistributionImpl setModified(Instant timestamp);
    // return create(this, DCTerms.modified, NodeMappers.from(Instant.class));


    @IriNs("dcterms")
    @IriType
    @Override
    RdfDcatApDistributionImpl setRights(String iri);

    @IriNs("foaf")
    @IriType
    @Override
    Set<String> getPages();

    @IriNs("dcterms")
    @IriType
    @Override
    Set<String> getConformsTo();

    @IriNs("dcterms")
    @IriType
    @Override
    Set<String> getLanguages();
}
