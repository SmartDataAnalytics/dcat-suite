package org.aksw.trash;

import java.io.InputStream;

import org.apache.jena.rdf.model.Resource;

public interface RdfEntity<T extends Resource> {
	T getMetadata();
	InputStream getContent();
}