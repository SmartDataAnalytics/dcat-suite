package org.aksw.dcat_suite.server.conneg.torename;

import java.nio.file.Path;

public interface PathGroup {
	Path findFirstExisting(Path lookupPath);
}
