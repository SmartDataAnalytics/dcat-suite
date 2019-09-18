package org.aksw.dcat_suite.server.conneg;

import java.nio.file.Path;
import java.util.Collection;

import org.apache.jena.rdf.model.Resource;

public class RdfFileResourceImpl
	implements RdfHttpResourceFile
{
	protected ResourceStore manager;
	protected Path path;
	
	public RdfFileResourceImpl(ResourceStore manager, Path path) {
		super();
		this.manager = manager;
		this.path = path;
	}

//	@Override
//	public ResourceManager getManager() {
//		return manager;
//	}

	@Override
	public Collection<RdfHttpEntityFile> getEntities() {
		return manager.listEntities(path);
	}

	@Override
	public Path getRelativePath() {
		return path;
	}

	@Override
	public ResourceStore getResourceStore() {
		return manager;
	}

	@Override
	public RdfHttpEntityFile allocate(Resource description) {
		RdfHttpEntityFile result = getResourceStore().allocateEntity(path, description);
		return result;
	}
	
}
