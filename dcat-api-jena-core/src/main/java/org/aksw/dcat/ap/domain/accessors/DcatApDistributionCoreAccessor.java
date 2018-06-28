package org.aksw.dcat.ap.domain.accessors;

import java.time.Instant;
import java.util.Set;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.dcat.ap.domain.api.DcatApDistributionCore;

public interface DcatApDistributionCoreAccessor
	extends DcatApDistributionCore
{
	SingleValuedAccessor<String> title();
	SingleValuedAccessor<String> description();
	SingleValuedAccessor<Set<String>> accessUrls();
	SingleValuedAccessor<Set<String>> downloadUrls();	
	SingleValuedAccessor<String> mediaType();
	SingleValuedAccessor<String> format();
	SingleValuedAccessor<String> license();
	SingleValuedAccessor<String> status();
	SingleValuedAccessor<Long> byteSize();
	SingleValuedAccessor<Instant> issued();
	SingleValuedAccessor<Instant> modified();
	SingleValuedAccessor<String> rights();
	SingleValuedAccessor<Set<String>> pages();
	SingleValuedAccessor<Set<String>> conformsTo();
	SingleValuedAccessor<Set<String>> languages();

	@Override default String getTitle() { return title().get(); }
	@Override default void setTitle(String title) { title().set(title); }

	@Override default Set<String> getAccessURLs() { return accessUrls().get(); }
	//@Override default void setAccessUrls(Collection<String> accessUrls) { accessUrls().set(accessUrls); }

	@Override default Set<String> getDownloadURLs() { return downloadUrls().get(); }
	//@Override default void setDownloadUrls(Collection<String> downloadUrls) { downloadUrls().set(downloadUrls); }

	@Override default String getDescription() { return description().get(); }
	@Override default void setDescription(String description) { description().set(description); }

	@Override default String getMediaType() { return mediaType().get(); }
	@Override default void setMediaType(String mediaType) { mediaType().set(mediaType); }

	@Override default String getFormat() { return format().get(); }
	@Override default void setFormat(String format) { format().set(format); }

	@Override default String getLicense() { return license().get(); }
	@Override default void setLicense(String license) { license().set(license); }

	@Override default String getStatus() { return status().get(); }
	@Override default void setStatus(String status) { status().set(status); }

	@Override default Long getByteSize() { return byteSize().get(); }
	@Override default void setByteSize(Long byteSize) { byteSize().set(byteSize); }

	@Override default Instant getIssued() { return issued().get(); }
	@Override default void setIssued(Instant issued) { issued().set(issued); }

	@Override default Instant getModified() { return modified().get(); }
	@Override default void setModified(Instant modified) { modified().set(modified); }

	@Override default String getRights() { return rights().get(); }
	@Override default void setRights(String rights) { rights().set(rights); }

	@Override default Set<String> getPages() { return pages().get(); }
	@Override default void setPage(Set<String> pages) { pages().set(pages); }

	@Override default Set<String> getConformsTo() { return conformsTo().get(); }
	@Override default void setConformsTo(Set<String> conformsTo) { conformsTo().set(conformsTo); }

	@Override default Set<String> getLanguages() { return languages().get(); }
	@Override default void setLanguages(Set<String> languages) { languages().set(languages); }
}
