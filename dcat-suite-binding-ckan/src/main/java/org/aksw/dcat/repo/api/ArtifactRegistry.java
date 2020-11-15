package org.aksw.dcat.repo.api;

import org.apache.jena.rdf.model.Resource;

import io.reactivex.rxjava3.core.Flowable;


public interface ArtifactRegistry {
    void register(Resource mavenEntity);
    Flowable<Resource> search(String pattern);
}

