package org.aksw.dcat_suite.app.model.impl;

import java.nio.file.Path;

import org.apache.jena.query.Dataset;


/** A folder with a accompanying dataset for metadata of any kind */
public interface RdfFolder {
    Dataset getDataset();
    Path getBasePath();

    /** Create a (relativ) uri for a given path suitable for use in the dataset */
    String pathToIri(Path path);
}
