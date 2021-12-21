package org.aksw.dcat.ap.domain.api;

import java.time.Instant;
import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDistributionCore;

public interface DcatApDistributionCore
    extends DcatDistributionCore
{
    String getTitle();
    DcatDistributionCore setTitle(String title);

//	String getAccessUrl();
//	void setAccessUrl(String accessUrl);
//
//	String getDownloadUrl();
//	void setDownloadUrl(String accessUrl);

    String getDescription();
    DcatDistributionCore setDescription(String description);

    String getMediaType();
    DcatDistributionCore setMediaType(String mediaType);

    String getFormat();
    DcatDistributionCore setFormat(String format);

    String getLicense();
    DcatDistributionCore setLicense(String licence);

    String getStatus();
    DcatDistributionCore setStatus(String status);

    Long getByteSize();
    DcatDistributionCore setByteSize(Long byteSize);

    Instant getIssued();
    DcatDistributionCore setIssued(Instant issued);

    Instant getModified();
    DcatDistributionCore setModified(Instant modified);

    String getRights();
    DcatDistributionCore setRights(String accessRights);

    Set<String> getPages();
    DcatDistributionCore setPage(Set<String> pages);

    Set<String> getConformsTo();
    DcatDistributionCore setConformsTo(Set<String> conformsTo);

    Set<String> getLanguages();
    DcatDistributionCore setLanguages(Set<String> languages);

//	/** Singleton instance of a checksum */
//	SpdxChecksum getChecksum();
}
