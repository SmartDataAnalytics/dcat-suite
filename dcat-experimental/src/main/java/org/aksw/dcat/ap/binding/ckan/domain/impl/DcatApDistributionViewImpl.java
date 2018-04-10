package org.aksw.dcat.ap.binding.ckan.domain.impl;

import java.time.Instant;
import java.util.Set;

import org.aksw.dcat.ap.domain.api.DcatApDistribution;
import org.aksw.dcat.ap.domain.api.View;

import eu.trentorise.opendata.jackan.model.CkanResource;

public class DcatApDistributionViewImpl
	extends DcatApCkanDistributionViewBase
	implements DcatApDistribution
{
	
	public DcatApDistributionViewImpl(CkanResource ckanResource, Personality<CkanResource, View> personalities) {
		super(ckanResource, personalities);
	}

	@Override
	public String getTitle() {
		return ckanResource.getName();
	}

	@Override
	public void setTitle(String title) {
		ckanResource.setName(title);
	}

	@Override
	public String getAccessUrl() {
		return ckanResource.getUrl();
		//CollectionckanResource.getOthers()
	}

	@Override
	public void setAccessUrl(String accessUrl) {
		ckanResource.setUrl(accessUrl);
	}

	@Override
	public String getDownloadUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDownloadUrl(String accessUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescription() {
		return ckanResource.getDescription();
	}

	@Override
	public void setDescription(String description) {
		ckanResource.setDescription(description);
	}

	@Override
	public String getMediaType() {
		return ckanResource.getMimetype();
	}

	@Override
	public void setMediaType(String mediaType) {
		ckanResource.setMimetype(mediaType);
	}

	@Override
	public String getFormat() {
		return ckanResource.getFormat();
	}

	@Override
	public void setFormat(String format) {
		ckanResource.setFormat(format);
	}

	@Override
	public String getLicense() {
		return null;
		//ckanResource.getr
	}

	@Override
	public void setLicense(String licence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatus(String status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long getByteSize() {
		String str = ckanResource.getSize();
		Long result = str == null ? null : Long.parseLong(str);
		return result;
	}

	@Override
	public void setByteSize(Long byteSize) {
		ckanResource.setSize(Long.toString(byteSize));
	}

	@Override
	public Instant getIssued() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIssued(Instant issued) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Instant getModified() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setModified(Instant modified) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getRights() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRights(String accessRights) {
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
	public Set<String> getConformsTo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConformsTo(Set<String> conformsTo) {
		// TODO Auto-generated method stub
		
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

}
