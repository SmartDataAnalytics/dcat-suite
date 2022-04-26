package org.aksw.dcat_suite.cli.cmd.file;

public class ArtifactResolutionRequestImpl
    implements ArtifactResolutionRequest
{
    protected String artifactId;
    boolean updateCache;

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public ArtifactResolutionRequest setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    @Override
    public boolean updateCache() {
        return updateCache;
    }

    @Override
    public ArtifactResolutionRequest setUpdateCache(boolean updateCache) {
        this.updateCache = updateCache;
        return this;
    }

}
