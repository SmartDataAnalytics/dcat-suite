package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;

import org.apache.jena.query.Dataset;


public interface DcatRepoLocal {
    Path getBasePath();

    Dataset getDataset();

    // RdfDataSourceFromDataset
}
