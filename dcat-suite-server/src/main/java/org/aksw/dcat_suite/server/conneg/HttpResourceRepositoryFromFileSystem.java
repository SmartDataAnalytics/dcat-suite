package org.aksw.dcat_suite.server.conneg;

import java.nio.file.Path;

public interface HttpResourceRepositoryFromFileSystem {
	/**
	 * Obtain an entity for the given path
	 * 
	 * The repository may consult several stores to complete this action.
	 * 
	 * @param path
	 * @return
	 */
	RdfHttpEntityFile getEntityForPath(Path path);
}
