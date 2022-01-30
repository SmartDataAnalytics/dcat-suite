package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;

import org.apache.jena.query.Dataset;
import org.eclipse.jgit.lib.Repository;


public interface DcatRepoLocal {
    Path getBasePath();

    Dataset getDataset();
    
    Repository getGitRepository();

    
    void move(Path src, Path tgt);
    
    default void rename(Path src, String newName) {
    	move(src, src.resolveSibling(newName));
    }
    // RdfDataSourceFromDataset
}
