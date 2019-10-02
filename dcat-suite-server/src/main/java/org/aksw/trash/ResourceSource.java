package org.aksw.trash;

import org.apache.jena.rdf.model.Resource;

public interface ResourceSource {
	Resource get();
	
	boolean canWrite();
	Resource set(Resource resource);
}
