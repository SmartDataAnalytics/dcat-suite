package org.aksw.dcat_suite.server.conneg;

import java.nio.file.Path;

import org.apache.jena.rdf.model.Resource;

public class RdfHttpEntityFileImpl
	implements RdfHttpEntityFile
{
	protected RdfHttpResourceFile resource;
	
	// relative file or folder within the resource that denotes the entity
	protected Path relPath;

	public RdfHttpEntityFileImpl(RdfHttpResourceFile resource, Path path) {
		super();
		this.resource = resource;
		this.relPath = path;
	}

	@Override
	public RdfHttpResourceFile getResource() {
		return resource;
	}

	@Override
	public Path getRelativePath() {
		return relPath;
	}

	@Override
	public Resource getInfo() {
		Path absPath = getAbsolutePath();
		Resource result = resource.getResourceStore().getInfo(absPath);
		return result;
	}
}
