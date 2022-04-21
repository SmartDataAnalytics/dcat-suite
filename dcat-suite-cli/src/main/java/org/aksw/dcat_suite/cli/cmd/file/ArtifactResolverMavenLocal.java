package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;

import org.aksw.dcat.jena.domain.api.MavenEntityCore;

public class ArtifactResolverMavenLocal
    implements ArtifactResolver
{
    protected Path localRepository;

    public ArtifactResolverMavenLocal(Path localRepository) {
        super();
        this.localRepository = localRepository;
    }

    @Override
    public Path resolve(String artifact) {
        MavenEntityCore mvnId = MavenEntityCore.parse(artifact);

        String path = MavenEntityCore.toPath(mvnId);
        String fileName = MavenEntityCore.toFileName(mvnId);

        Path result = localRepository.resolve(path).resolve(fileName);

        return result;
    }
}
