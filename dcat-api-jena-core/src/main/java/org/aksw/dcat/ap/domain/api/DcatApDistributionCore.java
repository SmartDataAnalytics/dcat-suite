package org.aksw.dcat.ap.domain.api;

import java.time.Instant;
import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDistributionCore;

public interface DcatApDistributionCore
	extends DcatDistributionCore
{
	String getTitle();
	void setTitle(String title);
	
//	String getAccessUrl();
//	void setAccessUrl(String accessUrl);
//	
//	String getDownloadUrl();
//	void setDownloadUrl(String accessUrl);
	
	String getDescription();
	void setDescription(String description);
	
	String getMediaType();
	void setMediaType(String mediaType);
	
	String getFormat();
	void setFormat(String format);
	
	String getLicense();
	void setLicense(String licence);
	
	String getStatus();
	void setStatus(String status);
	
	Long getByteSize();
	void setByteSize(Long byteSize);

	Instant getIssued();
	void setIssued(Instant issued);

	Instant getModified();
	void setModified(Instant modified);

	String getRights();
	void setRights(String accessRights);

	Set<String> getPages();
	void setPage(Set<String> pages);

	Set<String> getConformsTo();
	void setConformsTo(Set<String> conformsTo);
	
	Set<String> getLanguages();
	void setLanguages(Set<String> languages);
	
//	/** Singleton instance of a checksum */
//	SpdxChecksum getChecksum();
}
