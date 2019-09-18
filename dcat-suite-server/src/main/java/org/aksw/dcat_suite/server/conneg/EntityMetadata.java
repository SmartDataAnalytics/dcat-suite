package org.aksw.dcat_suite.server.conneg;

import java.util.Collection;

import org.apache.jena.rdf.model.Resource;

public interface EntityMetadata {
	Resource getUnionView();

	Collection<ResourceSource> getSources();
}
