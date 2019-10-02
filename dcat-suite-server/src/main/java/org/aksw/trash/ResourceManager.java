package org.aksw.trash;

import java.nio.file.Path;

import org.apache.jena.rdf.model.Resource;

public interface ResourceManager {
	Resource getInfo(Path path);
}
