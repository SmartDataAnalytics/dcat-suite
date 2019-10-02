package org.aksw.trash;

import java.nio.file.Path;

import org.aksw.jena_sparql_api.utils.turtle.TurtleNoBaseTest;
import org.apache.http.Header;

public class PathResourceImpl {
	protected TurtleNoBaseTest resourceManager;
	
	// Path relative to the base path of the resourceManager
	protected Path relPath;
	

	RdfFileEntity findEntity(Header[] headers) {
		return null;
	}
}
