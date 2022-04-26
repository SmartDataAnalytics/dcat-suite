package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.aksw.dcat.jena.domain.api.MavenEntityCore;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;

public class ArtifactResolverMavenRemote
    implements ArtifactResolver
{
    protected ArtifactResolverMavenLocal localResolver;
    protected Invoker invoker;

    // Factory for preconfigured invocation requests of the dependency:get goal
    protected Supplier<InvocationRequest> invocationRequestFactory;

    public ArtifactResolverMavenRemote(ArtifactResolverMavenLocal localResolver,
            Invoker invoker,
            Supplier<InvocationRequest> invocationRequestFactory) {
        super();
        this.localResolver = localResolver;
        this.invoker = invoker;
        this.invocationRequestFactory = invocationRequestFactory;
    }

    @Override
    public Path resolve(ArtifactResolutionRequest request) throws Exception {

        // Remove a preceeding urn:mvn:
        String tmp = request.getArtifactId();
        MavenEntityCore mvnEntity = MavenEntityCore.parse(tmp);
        String artifact = MavenEntityCore.toString(mvnEntity);

        Path result = null;
        if (!request.updateCache()) {
            result = localResolver.resolve(request);
        }

        if (result == null || !Files.exists(result)) {
            InvocationRequest mvnRequest = invocationRequestFactory.get();
            MavenUtils.dependencyGet(invoker, artifact, null, mvnRequest);

            result = localResolver.resolve(request);

            if (result == null || !Files.exists(result)) {
                throw new RuntimeException(String.format("Sanity check failed: Remote retrieval of %s raised no error but artifact not available at %s", artifact, result));
            }
        }

        return result;
    }
}
