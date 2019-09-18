package org.aksw.dcat_suite.server.conneg;

import java.nio.file.Path;

import org.apache.jena.rdf.model.Resource;

public interface RdfHttpEntityFile
//	extends RdfFileEntity
{
	// Path relative to the resource
	Path getRelativePath(); // TODO Inherit from some file-based entity class
	
	default Path getAbsolutePath() {
		Path relPath = getRelativePath();
		Path parentAbsPath = getResource().getAbsolutePath();
		Path result = parentAbsPath.resolve(relPath);

		return result;
	}
	
	Resource getInfo();
	RdfHttpResourceFile getResource();
}
