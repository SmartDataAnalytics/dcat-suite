package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.time.Instant;
import java.util.Set;

import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.dcat.ap.domain.api.DcatApContactPoint;
import org.aksw.dcat.ap.domain.api.PeriodOfTime;
import org.aksw.dcat.ap.domain.api.Spatial;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.apache.jena.rdf.model.Resource;

public interface RdfDcatApDataset
    extends Resource, DcatApDataset
{
    /**
     * Creates a new blank node and views it as a DcapApDistribution
     *
     */
    @Override
    default DcatApDistribution createDistribution() {
        return getModel().createResource().as(DcatApDistribution.class);
    }


    @IriNs("dcterms")
    @Override
    RdfDcatApDataset setTitle(String title);


    @IriNs("dcterms")
    @Override
    RdfDcatApDataset setDescription(String description);

    @IriNs("dcat")
    Set<String> getKeywords();

    @Iri("dcat:theme")
    @IriType
    Set<String> getThemes();

    @IriNs("dcterms")
    @Override
    RdfDcatApDataset setIdentifier(String id);

    @IriNs("adms")
    RdfDcatApDataset alternateIdentifier(String id);

    @IriNs("dcterms")
    @Override
    RdfDcatApDataset setIssued(Instant instant);
//    {
//        return create(this, DCTerms.issued, NodeMappers.from(Calendar.class))
//                .convert(null);
//    }

    @IriNs("dcterms")
    @Override
    RdfDcatApDataset setModified(Instant instant);
//    {
//        return create(this, DCTerms.modified, NodeMappers.from(Calendar.class))
//                .convert(null);
//    }

    @IriNs("owl")
    @Override
    RdfDcatApDataset setVersionInfo(String versionInfo);


    @IriNs("adms")
    @Override
    String getVersionNotes();

    @Iri("dcterms:language")
    @Override
    Set<String> getLanguages();

    @IriNs("dcat")
    @IriType
    @Override
    RdfDcatApDataset setLandingPage(String landingPage);

    @IriNs("dcterms")
    @IriType
    @Override
    RdfDcatApDataset setAccrualPeriodicity(String periodicity);

    @IriNs("dcterms")
    @IriType
    @Override
    Set<String> getConformsTo();


    @IriNs("dcterms")
    @IriType
    @Override
    RdfDcatApDataset setAccessRights(String accessRights);


    @Iri("foaf:page")
    @IriType
    @Override
    Set<String> getPages();

    @IriNs("dcterms")
    @IriType
    @Override
    RdfDcatApDataset setProvenance(String iri);

    @IriNs("dcterms")
    @IriType
    @Override
    RdfDcatApDataset setType(String type);

    @Iri("dcterms:hasVersion")
    @IriType
    @Override
    Set<String> getHasVersions();

    @Iri("dcterms:isVersionOf")
    @IriType
    @Override
    Set<String> getIsVersionOf();

    @Iri("dcterms:source")
    @IriType
    @Override
    Set<String> getSources();

    @Iri("adms:sample")
    @IriType
    @Override
    Set<String> getSamples();

    @IriNs("dcat")
    @Override
    RdfDcatApDataset setSpatial(Spatial spatial);

    @IriNs("dcat")
    @Override
    RdfDcatApDataset setTemporal(PeriodOfTime spatial);

    @IriNs("dcat")
    @Override
    RdfDcatApDataset setPublisher(DcatApAgent publisher);

    @IriNs("dcat")
    @Override
    RdfDcatApDataset setContactPoint(DcatApContactPoint contactPoint);
}
