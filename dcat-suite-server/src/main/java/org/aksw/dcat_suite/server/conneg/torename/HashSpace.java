package org.aksw.dcat_suite.server.conneg.torename;

import java.nio.file.Path;

/**
 * Supplier of paths for hashes
 * The base path should be an absolute path
 * 
 * @author raven
 *
 */
public interface HashSpace {
	Path get(String hash);
}