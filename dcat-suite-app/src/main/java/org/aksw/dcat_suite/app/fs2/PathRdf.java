package org.aksw.dcat_suite.app.fs2;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

public class PathRdf
    extends PathBase<PathRdf>
{
//    protected Path getMetadataFile();
//    protected Path getContentFile();
    // rdffs.conf.ttl

    public PathRdf(boolean isAbsolute, List<String> segments) {
        super(isAbsolute, segments);
        // TODO Auto-generated constructor stub
    }

    @Override
    public FileSystem getFileSystem() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path newPath(boolean isAbsolute, List<String> segments) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PathRdf requireSubType(Path other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected PathOps getPathOpts() {
        // TODO Auto-generated method stub
        return null;
    }

}
