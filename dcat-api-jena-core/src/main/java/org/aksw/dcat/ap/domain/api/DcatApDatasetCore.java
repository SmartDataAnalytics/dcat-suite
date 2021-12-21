package org.aksw.dcat.ap.domain.api;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDatasetCore;

public interface DcatApDatasetCore
    extends DcatDatasetCore
{
    /** Factory method for distributions - does not add them to the dataset */
    DcatApDistributionCore createDistribution();


    String getTitle();
    DcatApDatasetCore setTitle(String title);

    String getDescription();
    DcatApDatasetCore setDescription(String description);

    Set<String> getKeywords();
    //void setTags(Set<String tags>);


    // In pratice a mix of URIs and strings :/ - but should be skos:Concepts
    //
    Set<String> getThemes();

    String getIdentifier();
    DcatApDatasetCore setIdentifier(String identifier);

    String getAlternateIdentifier();
    DcatApDatasetCore setAlternateIdentifier(String alternateIdentifier);

    Instant getIssued();
    DcatApDatasetCore setIssued(Instant issued);

    Instant getModified();
    DcatApDatasetCore setModified(Instant modified);

    String getVersionInfo();
    DcatApDatasetCore setVersionInfo(String versionInfo);

    String getVersionNotes();
    DcatApDatasetCore setVersionNotes(String versionNotes);

    /** Language systems denoted by URIs */
    Set<String> getLanguages();
    DcatApDatasetCore setLanguages(Set<String> languages);

    String getLandingPage();
    DcatApDatasetCore setLandingPage(String landingPage);

    /** Frequency of updates denoted by an URI */
    String getAccrualPeriodicity();
    DcatApDatasetCore setAccrualPeriodicity(String accrualPeriodicity);


    Set<String> getConformsTo();
    DcatApDatasetCore setConformsTo(Set<String> conformsTo);

    String getAccessRights();
    DcatApDatasetCore setAccessRights(String accessRights);

    Set<String> getPages();
    DcatApDatasetCore setPage(Set<String> pages);

    String getProvenance();
    DcatApDatasetCore setProvenance(String provenance);

    String getType();
    DcatApDatasetCore setType(String type);

    /** Set of URIs to other datasets */
    Set<String> getHasVersions();
    DcatApDatasetCore setHasVersions(Set<String> hasVersions);

    /** Set of URIs to other datasets */
    Set<String> getIsVersionOf();
    DcatApDatasetCore setIsVersionOf(Set<String> isVersionOf);

    /** Set of URIs to other datasets */
    Set<String> getSources();
    DcatApDatasetCore setSources(Set<String> sources);

    /** Set of URIs to distributions */
    Set<String> getSamples();
    DcatApDatasetCore setSamples(Set<String> sources);

    Spatial getSpatial();
    DcatApDatasetCore setSpatial(Spatial spatial);

    PeriodOfTime getTemporal();
    DcatApDatasetCore setTemporal(PeriodOfTime temporal);

    DcatApAgent getPublisher();
    DcatApDatasetCore setPublisher(DcatApAgent publisher);

    DcatApContactPoint getContactPoint();
    DcatApDatasetCore setContactPoint(DcatApContactPoint contactPoint);

//	Set<? extends DcatApDistribution<M>> getDistributions();
//	void setDistributions(Set<? extends DcatApDistribution<M>> distributions);

    Collection<? extends DcatApDistributionCore> getDistributions();
    //void setDistributions(Collection<? extends DcatApDistributionCore> distributions);
}
