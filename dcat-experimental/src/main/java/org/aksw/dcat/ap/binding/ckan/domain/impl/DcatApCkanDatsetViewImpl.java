package org.aksw.dcat.ap.binding.ckan.domain.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.dcat.ap.domain.api.DcatApContactPoint;
import org.aksw.dcat.ap.domain.api.DcatApDataset;
import org.aksw.dcat.ap.domain.api.DcatApDistribution;
import org.aksw.dcat.ap.domain.api.PeriodOfTime;
import org.aksw.dcat.ap.domain.api.Spatial;
import org.aksw.dcat.ap.domain.api.View;
import org.aksw.dcat.util.view.LazyCollection;
import org.aksw.dcat.util.view.SetFromConversion;
import org.aksw.dcat.util.view.SingleValuedAccessorImpl;

import com.google.common.base.Converter;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;

public class DcatApCkanDatsetViewImpl
	extends DcatApCkanDatasetViewBase
	implements DcatApDataset
{
	public DcatApCkanDatsetViewImpl(CkanDataset ckanDataset, Personality<CkanDataset, View> personalities) {
		super(ckanDataset, personalities);
	}

	@Override public String getTitle() { return ckanDataset.getTitle(); }
	@Override public void setTitle(String title) { ckanDataset.setTitle(title); }

	@Override public String getDescription() { return ckanDataset.getNotes(); }
	@Override public void setDescription(String description) { ckanDataset.setNotes(description); }

	@Override
	public Set<String> getTags() {
		//return ckanDataset.getTags();
		return null;
	}

	@Override
	public Set<String> getThemes() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getExtraField(String key) {
		return null;
	}
	
	public static void setExtraField(String key, String value) {
	}
	
	public static Instant toInstant(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		Instant result = calendar.toInstant();
		return result;
	}

	
	@Override
	public String getIdentifier() {
		return getExtraField("extra:identifier");	
	}
	
	@Override
	public void setIdentifier(String identifier) {
		setExtraField("extra:identifier", identifier);
	}

	@Override public String getAlternateIdentifier() {
		return getExtraField("extra:alternate_identifier");
	}

	@Override public void setAlternateIdentifier(String alternateIdentifier) {
		setExtraField("extra:alternate_identifier", alternateIdentifier);
	}
	
	@Override
	public Instant getIssued() {
		return toInstant(ckanDataset.getMetadataCreated());
	}

	@Override
	public void setIssued(Instant issued) {
		ckanDataset.setMetadataCreated(Timestamp.from(issued));
	}

	@Override
	public Instant getModified() {
		return toInstant(ckanDataset.getMetadataModified());
	}

	@Override
	public void setModified(Instant modified) {
		ckanDataset.setMetadataModified(Timestamp.from(modified));		
	}

	@Override
	public String getVersionInfo() {
		return ckanDataset.getVersion();
	}

	@Override
	public void setVersionInfo(String versionInfo) {
		ckanDataset.setVersion(versionInfo);
	}

	@Override
	public String getVersionNotes() {
		return getExtraField("extra:version_notes");
	}

	@Override
	public void setVersionNotes(String versionNotes) {
		setExtraField("extra:version_notes", versionNotes);
	}

	@Override
	public Set<String> getLanguages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLanguages(Set<String> languages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLandingPage() {
		return ckanDataset.getUrl();
	}

	@Override
	public void setLandingPage(String landingPage) {
		ckanDataset.setUrl(landingPage);
	}

	@Override
	public String getAccrualPeriodicity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAccuralPeriodicity(String accrualPeriodicity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getConformsTo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConformsTo(Set<String> conformsTo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAccessRights() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAccessRights(String accessRights) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getPages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPage(Set<String> pages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProvenance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProvenance(String provenance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setType(String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getHasVersions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasVersions(Set<String> hasVersions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getIsVersionOf() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIsVersionOf(Set<String> isVersionOf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getSources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSources(Set<String> sources) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getSamples() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSamples(Set<String> sources) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Spatial getSpatial() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSpatial(Spatial spatial) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PeriodOfTime getTemporal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTemporal(PeriodOfTime temporal) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DcatApAgent getPublisher() {
		// TODO the whole abstraction should be based on property source
		PropertySource source = new PropertySourceCkanDataset(ckanDataset);
		return new DcatApAgentFromPropertySource("extra:publisher_", source);
		//return new DcatApCkanAgentView("extra:publisher_", source);
	}

	@Override
	public void setPublisher(DcatApAgent publisher) {
		// The get method already returns a live view - so
		// what would it mean to set a publisher?
		// (.) copy the attribute values from the given publisher?
		
	}

//	@Override
//	public String getContactPoint() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setContactPoint(String contactPoint) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public Set<DcatApDistribution> getDistributions() {
		Converter<DcatApDistribution, CkanResource> converter = new Converter<DcatApDistribution, CkanResource>() {
			@Override
			protected CkanResource doForward(DcatApDistribution a) {
				return ((DcatApCkanDistributionViewBase)a).getEntity();
			}

			@Override
			protected DcatApDistribution doBackward(CkanResource b) {
				return CkanPersonalities.resourcePersonalities.getImplementation(DcatApDistribution.class).wrap(b);
			}
		};
		
		Set<DcatApDistribution> result = new SetFromConversion<>(
				new LazyCollection<>(
						new SingleValuedAccessorImpl<>(ckanDataset::getResources, ckanDataset::setResources),
						ArrayList::new, true),
				converter);
				
		return result;
	}

	@Override
	public void setDistributions(Set<DcatApDistribution> distributions) {

		
		//ckanDataset.di
//		new LazyCollection<>(new SingleValuedAccessorImpl<>(dcatDataset::get, setter), ctor, setNullOnEmpty)
		
		//for()
	}

	@Override
	public DcatApDistribution createDistribution() {
		CkanResource ckanResource = new CkanResource();
		
		DcatApDistribution result = new DcatApDistributionViewImpl(ckanResource, CkanPersonalities.resourcePersonalities);
		return result;
	}

	@Override
	public DcatApContactPoint getContactPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContactPoint(DcatApContactPoint contactPoint) {
		// TODO Auto-generated method stub
		
	}

}
