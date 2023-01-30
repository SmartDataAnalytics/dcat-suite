package org.aksw.dcat_suite.app.model.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.dcat_suite.app.model.api.LifeCycleEntity;
import org.aksw.dcat_suite.app.model.api.LifeCycleEntityMgr;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

public abstract class LifeCycleEntityMgrPathBase<T extends LifeCycleEntity>
    implements LifeCycleEntityMgr<T>
{

    public LifeCycleEntityMgrPathBase(Path basePath) {
        super();
        this.basePath = basePath;
    }

    // protected LifeCycleEntityPath parent;
    protected Path basePath;

    @Override
    public Path getBasePath() {
        return basePath;
    }

    @Override
    public Stream<Resource> list() throws IOException {
        if (!Files.exists(basePath)) {
            throw new IOException("Base path does not exist: " + basePath);
        }

        return Files.list(basePath)
            .map(path -> {
                Resource r = ModelFactory.createDefaultModel().createResource(); //.as(FileEntity.class);
                r.addLiteral(RDFS.label, Objects.toString(path.getFileName()));
                return r;
            });
    }

    @Override
    public abstract T get(String name) throws IOException;
}

