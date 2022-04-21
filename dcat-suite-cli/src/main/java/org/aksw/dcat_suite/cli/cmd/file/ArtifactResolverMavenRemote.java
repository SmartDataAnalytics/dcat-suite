package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.shared.invoker.Invoker;

public class ArtifactResolverMavenRemote
    implements ArtifactResolver
{
    protected ArtifactResolverMavenLocal localResolver;
    protected Invoker invoker;
    protected List<String> remoteRepositories;

    public ArtifactResolverMavenRemote(ArtifactResolverMavenLocal localResolver,
            Invoker invoker,
            List<String> remoteRepositories) {
        super();
        this.localResolver = localResolver;
        this.invoker = invoker;
        this.remoteRepositories = remoteRepositories;
    }

    @Override
    public Path resolve(String artifact) throws Exception {
        Path result = localResolver.resolve(artifact);
        if (!Files.exists(result)) {
            MavenUtils.dependencyGet(invoker, artifact, remoteRepositories);

            if (!Files.exists(result)) {
                throw new RuntimeException(String.format("Sanity check failed: Remote retrieval of %s raised no error but artifact not available at %s", artifact, result));
            }
        }

        return result;
    }
}
