package org.aksw.ckan_deploy.core;

import java.nio.file.Path;

public interface PathTransform {
	void transform(Path input, Path output) throws Exception;
	boolean cmdExists();
}
