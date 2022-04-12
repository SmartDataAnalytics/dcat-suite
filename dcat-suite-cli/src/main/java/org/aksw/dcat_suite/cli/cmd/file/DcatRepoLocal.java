package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.eclipse.jgit.lib.Repository;


public interface DcatRepoLocal {
    Path getBasePath();

    Dataset getDataset();

    /** Copy the dataset into memory */
    default Dataset getMemDataset() {
        Dataset result = DatasetFactory.create();
        Dataset repoDs = getDataset();
        repoDs.begin(ReadWrite.READ);
        result.asDatasetGraph().addAll(repoDs.asDatasetGraph());
        repoDs.commit();
        return result;
    }

    Repository getGitRepository();


    void move(Path src, Path tgt);

    default void rename(Path src, String newName) {
        move(src, src.resolveSibling(newName));
    }
    // RdfDataSourceFromDataset
}
