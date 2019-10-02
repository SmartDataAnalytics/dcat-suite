package org.aksw.trash;

import java.nio.file.Path;
import java.util.Objects;

import org.aksw.jena_sparql_api.http.repository.api.PathAnnotatorRdf;
import org.apache.jena.rdf.model.Resource;

public class RdfFileEntityImpl
	implements RdfFileEntity
{
	protected PathAnnotatorRdf pathAnnotator;	
	protected Path path;
	protected Resource cache;
	
	public RdfFileEntityImpl(PathAnnotatorRdf pathAnnotator, Path path, Resource cache) {
		super();
		this.pathAnnotator = pathAnnotator;
		this.path = Objects.requireNonNull(path);
		this.cache = Objects.requireNonNull(cache);
	}
	
	public boolean isValid() {
		//pathAnnotator.isV
		// TODO Ask the annotator whether the entity is still valid
		return true;
	}
	
	public Path getPath() {
		return path;
	}
	
	public Resource getInfo() {
		return cache;
	}
	
	
	public void refresh() {
		Resource tmp = pathAnnotator.getRecord(path);
		
		this.cache = tmp;
		//this.cache = tmp == null ? null : tmp; //tmp.as(clazz);
	}

	@Override
	public boolean canWriteInfo() {
		return true;
	}

	public void writeInfo() {
		pathAnnotator.setRecord(path, cache);
	}
}	

