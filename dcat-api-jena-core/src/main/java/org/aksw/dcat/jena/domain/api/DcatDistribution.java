package org.aksw.dcat.jena.domain.api;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface DcatDistribution
	extends DcatEntity
{
	// TODO Move to a subclass of datasetResource
	Set<Resource> getAccessURLs();

	Set<Resource> getDownloadURLs();
	
	// Assumes that getDownloadURLs returns a set view
	default void setDownloadURL(Resource r) {
		getDownloadURLs().clear();
		getDownloadURLs().add(r);
	}

	default void setAccessURL(Resource r) {
		getAccessURLs().clear();
		getAccessURLs().add(r);
	}
}
