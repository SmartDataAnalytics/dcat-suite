package org.aksw.dcat_suite.app.gtfs;

import java.nio.file.Path;

import org.aksw.dcat_suite.app.fs2.core.PathAnnotatorRdf;
import org.apache.jena.rdf.model.ResourceFactory;

/** Update the RDF metadata of files (via RdfFs) if they are gtfs files */
public class FileAnnotatorGtfs {

    public static final String ANNOTATOR_ID = "org.aksw.rdf.path.annotator.gtfs";


    public void annotate(PathAnnotatorRdf pathAnnotator, Path path) {
        pathAnnotator.mutateAnnotation(path, ANNOTATOR_ID, res -> {
            boolean isGtfs = DetectorGtfs.isGtfs(path);
            res.addLiteral(ResourceFactory.createProperty("http://www.example.org/isGtfs"), isGtfs);
        });
    }
    // protected

// https://developers.google.com/transit/gtfs
}
