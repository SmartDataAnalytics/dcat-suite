package org.aksw.dcat_suite.server.conneg;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

import org.apache.jena.rdf.model.Resource;

public interface ResourceStore {	
	RdfHttpResourceFile getResource(String uri);	
	RdfHttpEntityFile allocateEntity(String uri, Resource description);

	Path getAbsolutePath();

	/*
	 * File system based methods
	 */
	
	Collection<RdfHttpEntityFile> listEntities(Path basePath);

	/**
	 * Test whether the path lies within the store - does not check for existence
	 * @param path
	 * @return
	 */
	boolean contains(Path path);

	/**
	 * Return the metadata associated with a given path
	 * 
	 * @param path
	 * @param layer A label to retrieve the metadata from a single source
	 * @return
	 */
	Resource getInfo(Path path, String layer);

	void updateInfo(Path path, Consumer<? super Resource> info);
	
	default Resource getInfo(Path path) {
		Resource result = getInfo(path, null);
		return result;
	}

	RdfHttpEntityFile allocateEntity(Path relPath, Resource description);
}
