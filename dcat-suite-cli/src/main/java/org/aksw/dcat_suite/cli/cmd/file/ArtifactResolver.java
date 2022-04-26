package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;

public interface ArtifactResolver {
    Path resolve(ArtifactResolutionRequest request) throws Exception;
}
