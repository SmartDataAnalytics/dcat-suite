package org.aksw.dcat.jena.domain.api;

import java.util.Collection;

public interface DcatDistributionCore {

	String getFormat();
	void setFormat(String format);

	Collection<String> getAccessStrs();
	Collection<String> getDownloadStrs();
}
