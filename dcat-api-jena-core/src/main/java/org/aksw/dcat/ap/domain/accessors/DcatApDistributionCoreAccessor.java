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
    @Override default DcatApDistributionCoreAccessor setTitle(String title) { title().set(title); return this; }

    @Override default Set<String> getAccessUrls() { return accessUrls().get(); }
    //@Override default void setAccessUrls(Collection<String> accessUrls) { accessUrls().set(accessUrls); }

    @Override default Set<String> getDownloadUrls() { return downloadUrls().get(); }
    //@Override default void setDownloadUrls(Collection<String> downloadUrls) { downloadUrls().set(downloadUrls); }

    @Override default String getDescription() { return description().get(); }
    @Override default DcatApDistributionCoreAccessor setDescription(String description) { description().set(description); return this; }

    @Override default String getMediaType() { return mediaType().get(); }
    @Override default DcatApDistributionCoreAccessor setMediaType(String mediaType) { mediaType().set(mediaType); return this; }

    @Override default String getFormat() { return format().get(); }
    @Override default DcatApDistributionCoreAccessor setFormat(String format) { format().set(format); return this; }

    @Override default String getLicense() { return license().get(); }
    @Override default DcatApDistributionCoreAccessor setLicense(String license) { license().set(license); return this; }

    @Override default String getStatus() { return status().get(); }
    @Override default DcatApDistributionCoreAccessor setStatus(String status) { status().set(status); return this; }

    @Override default Long getByteSize() { return byteSize().get(); }
    @Override default DcatApDistributionCoreAccessor setByteSize(Long byteSize) { byteSize().set(byteSize); return this; }

    @Override default Instant getIssued() { return issued().get(); }
    @Override default DcatApDistributionCoreAccessor setIssued(Instant issued) { issued().set(issued); return this; }

    @Override default Instant getModified() { return modified().get(); }
    @Override default DcatApDistributionCoreAccessor setModified(Instant modified) { modified().set(modified); return this; }

    @Override default String getRights() { return rights().get(); }
    @Override default DcatApDistributionCoreAccessor setRights(String rights) { rights().set(rights); return this; }

    @Override default Set<String> getPages() { return pages().get(); }
    @Override default DcatApDistributionCoreAccessor setPage(Set<String> pages) { pages().set(pages); return this; }

    @Override default Set<String> getConformsTo() { return conformsTo().get(); }
    @Override default DcatApDistributionCoreAccessor setConformsTo(Set<String> conformsTo) { conformsTo().set(conformsTo); return this; }

    @Override default Set<String> getLanguages() { return languages().get(); }
    @Override default DcatApDistributionCoreAccessor setLanguages(Set<String> languages) { languages().set(languages); return this; }
}
