package org.aksw.dcat_suite.app.fs2.core;

import java.nio.file.Path;

import org.aksw.commons.lambda.throwing.ThrowingConsumer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public interface PathAnnotatorRdf {

    /**
     * Yield an annotation for the given path with the given qualifier.
     * Annotations with different qualifiers reside in separate named graphs.
     * The qualifier may be null.
     *
     */
    Resource getAnnotation(Path path, String qualifier, boolean createIfNotExists);

//    default Resource getAnnotation(Path path, String qualifier) {
//        return getAnnotation(path, qualifier, false);
//    }
//
//    default Resource getOrCreateAnnotation(Path path, String qualifier) {
//        return getAnnotation(path, qualifier, true);
//    }

    default void mutateAnnotation(Path path, String qualifier, ThrowingConsumer<? super Resource> mutator) {
        Resource r = getAnnotation(path, qualifier, true);
        Model model = r.getModel();
        model.begin();
        try {
            mutator.accept(r);
            model.commit();
        } catch (Exception e) {
            model.abort();
        }
    }
}
