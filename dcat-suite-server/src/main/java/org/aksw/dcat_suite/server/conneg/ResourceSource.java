package org.aksw.dcat_suite.server.conneg;

import org.apache.jena.rdf.model.Resource;

public interface ResourceSource {
	Resource get();
	
	boolean canWrite();
	Resource set(Resource resource);
}
