package org.aksw.dcat.ap.domain.accessors;

import java.time.Instant;
import java.util.Set;

import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.dcat.ap.domain.api.DcatApContactPoint;
import org.aksw.dcat.ap.domain.api.DcatApDataset;
import org.aksw.dcat.ap.domain.api.DcatApDistribution;
import org.aksw.dcat.ap.domain.api.PeriodOfTime;
import org.aksw.dcat.ap.domain.api.Spatial;
import org.aksw.dcat.util.view.SingleValuedAccessor;

public interface DcatApDatasetAccessor
	extends DcatApDataset
{
	SingleValuedAccessor<String> title();
	SingleValuedAccessor<String> description();
	
	// TODO Probably these should be live set views
	// the setter could be interpreted as 'replace all values'
	SingleValuedAccessor<Set<String>> tags();
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

	SingleValuedAccessor<Set<DcatApDistribution>> distributions();

	
	@Override default String getTitle() { return title().get(); }
	@Override default void setTitle(String title) { title().set(title); }

	@Override default String getDescription() { return description().get(); }
	@Override default void setDescription(String description) { description().set(description); }

	@Override default Set<String> getTags() { return tags().get(); }

	@Override default Set<String> getThemes() { return themes().get(); }

	@Override default String getIdentifier() { return identifier().get(); }
	@Override default void setIdentifier(String identifier) { identifier().set(identifier); }


	@Override default String getAlternateIdentifier() { return alternateIdentifier().get(); }
	@Override default void setAlternateIdentifier(String alternateIdentifier) { alternateIdentifier().set(alternateIdentifier); }


	@Override default Instant getIssued() { return issued().get(); }
	@Override default void setIssued(Instant issued) { issued().set(issued); }

	@Override default Instant getModified() { return issued().get(); }
	@Override default void setModified(Instant modified) { modified().set(modified); }

	@Override default String getVersionInfo() { return versionInfo().get(); }
	@Override default void setVersionInfo(String versionInfo) { versionInfo().set(versionInfo); }

	@Override default String getVersionNotes() { return versionNotes().get(); }
	@Override default void setVersionNotes(String versionNotes) { versionNotes().set(versionNotes); }

	@Override default Set<String> getLanguages() { return languages().get(); }
	@Override default void setLanguages(Set<String> languages) { languages().set(languages); }


	@Override default String getLandingPage() {	return landingPage().get();}
	@Override default public void setLandingPage(String landingPage) { landingPage().set(landingPage); }

	@Override default String getAccrualPeriodicity() { return accrualPeriodicity().get(); }
	@Override default void setAccuralPeriodicity(String accrualPeriodicity) { accrualPeriodicity().set(accrualPeriodicity); }

	@Override default Set<String> getConformsTo() { return conformsTo().get(); }
	@Override default void setConformsTo(Set<String> conformsTo) { conformsTo().set(conformsTo); }

	@Override default String getAccessRights() { return accessRights().get(); }
	@Override default void setAccessRights(String accessRights) { accessRights().set(accessRights); }

	@Override default Set<String> getPages() { return pages().get(); }
	@Override default void setPage(Set<String> pages) { pages().set(pages); }
	
	@Override default String getProvenance() { return provenance().get(); }
	@Override default void setProvenance(String provenance) { provenance().set(provenance); }

	@Override default String getType() { return type().get(); }
	@Override default void setType(String type) { type().set(type); }

	@Override default Set<String> getHasVersions() { return hasVersions().get(); }
	@Override default void setHasVersions(Set<String> hasVersions) { hasVersions().set(hasVersions); }

	@Override default Set<String> getIsVersionOf() { return isVersionOf().get(); }
	@Override default void setIsVersionOf(Set<String> isVersionOf) { isVersionOf().set(isVersionOf); }

	@Override default Set<String> getSources() { return sources().get(); }
	@Override default void setSources(Set<String> sources) { sources().set(sources); }

	@Override default Set<String> getSamples() { return samples().get(); }
	@Override default void setSamples(Set<String> samples) { samples().set(samples); }

	@Override default Spatial getSpatial() { return spatial().get(); }
	@Override default void setSpatial(Spatial spatial) { spatial().set(spatial); }

	@Override default PeriodOfTime getTemporal() { return temporal().get(); }
	@Override default void setTemporal(PeriodOfTime temporal) { temporal().set(temporal); }

	@Override default DcatApAgent getPublisher() { return publisher().get(); }
	@Override default void setPublisher(DcatApAgent publisher) { publisher().set(publisher); }

	@Override default DcatApContactPoint getContactPoint() { return contactPoint().get(); }
	@Override default void setContactPoint(DcatApContactPoint contactPoint) { contactPoint().set(contactPoint); }

	@Override default Set<DcatApDistribution> getDistributions() { return distributions().get(); }
	@Override default void setDistributions(Set<DcatApDistribution> distributions) { distributions().set(distributions); }
}
