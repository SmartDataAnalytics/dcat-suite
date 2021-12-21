package org.aksw.dcat.ap.domain.accessors;

import java.time.Instant;
import java.util.Set;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.dcat.ap.domain.api.DcatApContactPoint;
import org.aksw.dcat.ap.domain.api.DcatApDatasetCore;
import org.aksw.dcat.ap.domain.api.PeriodOfTime;
import org.aksw.dcat.ap.domain.api.Spatial;

public interface DcatApDatasetAccessor
    extends DcatApDatasetCore
{
    SingleValuedAccessor<String> title();
    SingleValuedAccessor<String> description();

    // TODO Probably these should be live set views
    // the setter could be interpreted as 'replace all values'
    SingleValuedAccessor<Set<String>> keywords();
    SingleValuedAccessor<Set<String>> themes();

    SingleValuedAccessor<String> identifier();
    SingleValuedAccessor<String> alternateIdentifier();

    SingleValuedAccessor<Instant> issued();
    SingleValuedAccessor<Instant> modified();

    SingleValuedAccessor<String> versionInfo();
    SingleValuedAccessor<String> versionNotes();

    SingleValuedAccessor<Set<String>> languages();

    SingleValuedAccessor<String> landingPage();

    SingleValuedAccessor<String> accrualPeriodicity();

    SingleValuedAccessor<Set<String>> conformsTo();

    SingleValuedAccessor<String> accessRights();

    SingleValuedAccessor<Set<String>> pages();
    SingleValuedAccessor<String> provenance();

    SingleValuedAccessor<String> type();

    SingleValuedAccessor<Set<String>> hasVersions();
    SingleValuedAccessor<Set<String>> isVersionOf();
    SingleValuedAccessor<Set<String>> sources();
    SingleValuedAccessor<Set<String>> samples();

    SingleValuedAccessor<Spatial> spatial();
    SingleValuedAccessor<PeriodOfTime> temporal();

    SingleValuedAccessor<DcatApAgent> publisher();
    SingleValuedAccessor<DcatApContactPoint> contactPoint();

    //SingleValuedAccessor<Collection<DcatApDistributionCore>> distributions();


    @Override default String getTitle() { return title().get(); }
    @Override default DcatApDatasetAccessor setTitle(String title) { title().set(title); return this; }

    @Override default String getDescription() { return description().get(); }
    @Override default DcatApDatasetAccessor setDescription(String description) { description().set(description); return this; }

    @Override default Set<String> getKeywords() { return keywords().get(); }

    @Override default Set<String> getThemes() { return themes().get(); }

    @Override default String getIdentifier() { return identifier().get(); }
    @Override default DcatApDatasetAccessor setIdentifier(String identifier) { identifier().set(identifier); return this; }


    @Override default String getAlternateIdentifier() { return alternateIdentifier().get(); }
    @Override default DcatApDatasetAccessor setAlternateIdentifier(String alternateIdentifier) { alternateIdentifier().set(alternateIdentifier); return this; }


    @Override default Instant getIssued() { return issued().get(); }
    @Override default DcatApDatasetAccessor setIssued(Instant issued) { issued().set(issued); return this; }

    @Override default Instant getModified() { return issued().get(); }
    @Override default DcatApDatasetAccessor setModified(Instant modified) { modified().set(modified); return this; }

    @Override default String getVersionInfo() { return versionInfo().get(); }
    @Override default DcatApDatasetAccessor setVersionInfo(String versionInfo) { versionInfo().set(versionInfo); return this; }

    @Override default String getVersionNotes() { return versionNotes().get(); }
    @Override default DcatApDatasetAccessor setVersionNotes(String versionNotes) { versionNotes().set(versionNotes); return this; }

    @Override default Set<String> getLanguages() { return languages().get(); }
    @Override default DcatApDatasetAccessor setLanguages(Set<String> languages) { languages().set(languages); return this; }


    @Override default String getLandingPage() {	return landingPage().get();}
    @Override default DcatApDatasetAccessor setLandingPage(String landingPage) { landingPage().set(landingPage); return this; }

    @Override default String getAccrualPeriodicity() { return accrualPeriodicity().get(); }
    @Override default DcatApDatasetAccessor setAccrualPeriodicity(String accrualPeriodicity) { accrualPeriodicity().set(accrualPeriodicity); return this; }

    @Override default Set<String> getConformsTo() { return conformsTo().get(); }
    @Override default DcatApDatasetAccessor setConformsTo(Set<String> conformsTo) { conformsTo().set(conformsTo); return this; }

    @Override default String getAccessRights() { return accessRights().get(); }
    @Override default DcatApDatasetAccessor setAccessRights(String accessRights) { accessRights().set(accessRights); return this; }

    @Override default Set<String> getPages() { return pages().get(); }
    @Override default DcatApDatasetAccessor setPage(Set<String> pages) { pages().set(pages); return this; }

    @Override default String getProvenance() { return provenance().get(); }
    @Override default DcatApDatasetAccessor setProvenance(String provenance) { provenance().set(provenance); return this; }

    @Override default String getType() { return type().get(); }
    @Override default DcatApDatasetAccessor setType(String type) { type().set(type); return this; }

    @Override default Set<String> getHasVersions() { return hasVersions().get(); }
    @Override default DcatApDatasetAccessor setHasVersions(Set<String> hasVersions) { hasVersions().set(hasVersions); return this; }

    @Override default Set<String> getIsVersionOf() { return isVersionOf().get(); }
    @Override default DcatApDatasetAccessor setIsVersionOf(Set<String> isVersionOf) { isVersionOf().set(isVersionOf); return this; }

    @Override default Set<String> getSources() { return sources().get(); }
    @Override default DcatApDatasetAccessor setSources(Set<String> sources) { sources().set(sources); return this; }

    @Override default Set<String> getSamples() { return samples().get(); }
    @Override default DcatApDatasetAccessor setSamples(Set<String> samples) { samples().set(samples); return this; }

    @Override default Spatial getSpatial() { return spatial().get(); }
    @Override default DcatApDatasetAccessor setSpatial(Spatial spatial) { spatial().set(spatial); return this; }

    @Override default PeriodOfTime getTemporal() { return temporal().get(); }
    @Override default DcatApDatasetAccessor setTemporal(PeriodOfTime temporal) { temporal().set(temporal); return this; }

    @Override default DcatApAgent getPublisher() { return publisher().get(); }
    @Override default DcatApDatasetAccessor setPublisher(DcatApAgent publisher) { publisher().set(publisher); return this; }

    @Override default DcatApContactPoint getContactPoint() { return contactPoint().get(); }
    @Override default DcatApDatasetAccessor setContactPoint(DcatApContactPoint contactPoint) { contactPoint().set(contactPoint); return this; }

//	@Override default Collection<? extends DcatApDistributionCore> getDistributions() { return distributions().get(); }
//	@Override default void setDistributions(Collection<? extends DcatApDistributionCore> distributions) { distributions().set(distributions); }
}
