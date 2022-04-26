package org.aksw.dcat_suite.cli.cmd.file;

public interface ArtifactResolutionRequest {
    String getArtifactId();
    ArtifactResolutionRequest setArtifactId(String artifactId);

    boolean updateCache();
    ArtifactResolutionRequest setUpdateCache(boolean updateCache);
}
