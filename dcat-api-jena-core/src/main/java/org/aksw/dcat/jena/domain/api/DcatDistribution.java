package org.aksw.dcat.jena.domain.api;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface DcatDistribution
	extends DcatEntity
{
	// TODO Move to a subgrass of datasetResource
	Set<Resource> getAccessURLs();
}
