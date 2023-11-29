package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.system.Txn;
import org.eclipse.jgit.lib.Repository;

public interface DcatRepoLocal {
    Path getBasePath();

    Dataset getDataset();


    Dataset getConfig();
    Resource getConfigResource(Dataset configDataset);

    default Resource getMemConfig() {
        Dataset ds = DatasetFactory.create();
        transactionalCopy(getConfig(), ds);
        Resource result = getConfigResource(ds);
        return result;
    }


    /** Copy the dataset into memory */
    default Dataset getMemDataset() {
        Dataset result = DatasetFactory.create();
        transactionalCopy(getDataset(), result);
        return result;
    }

    /** Copy a dataset into another thereby wrapping the operation in transactions */
    public static Dataset transactionalCopy(Dataset src, Dataset tgt) {
        Txn.executeWrite(tgt, () -> {
            Txn.executeRead(src, () -> {
                tgt.asDatasetGraph().addAll(src.asDatasetGraph());
            });
            tgt.commit();
        });
        return tgt;
    }


    Repository getGitRepository();


    void move(Path src, Path tgt);

    default void rename(Path src, String newName) {
        move(src, src.resolveSibling(newName));
    }
    // RdfDataSourceFromDataset
}
