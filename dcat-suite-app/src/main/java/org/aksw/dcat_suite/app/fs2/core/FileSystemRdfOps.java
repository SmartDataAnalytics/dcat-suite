package org.aksw.dcat_suite.app.fs2.core;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Path;

public interface FileSystemRdfOps
    extends PathAnnotatorRdf
{

    void move(Path source, Path target, CopyOption... options) throws IOException;
    void delete(Path path) throws IOException;
    boolean deleteIfExists(Path path) throws IOException;


    // boolean create(Path path) throws IOException;

    Path importFile(Path source, Path target, boolean deleteSource) throws IOException;
}
