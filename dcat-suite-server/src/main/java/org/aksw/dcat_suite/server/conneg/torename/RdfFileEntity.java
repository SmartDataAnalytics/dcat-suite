package org.aksw.dcat_suite.server.conneg.torename;

import java.nio.file.Path;

import org.apache.jena.rdf.model.Resource;

/**
 * A bundle of a file and rdf metadata
 * @author raven
 *
 */
public interface RdfFileEntity {
	
	
	Path getPath();
	Resource getInfo();
	
	
	boolean canWriteInfo();
	void writeInfo();
}
