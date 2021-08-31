package org.aksw.dcat_suite.app.fs2;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

public class PathOpsDefault
    implements PathOps
{
    protected FileSystemBase fileSystem;
    protected List<String> basePathSegments;

    @Override
    public String getParentToken() {
        return "..";
    }

    @Override
    public List<String> getBasePathSegments() {
        return basePathSegments;
    }

    @Override
    public URI toUri(List<String> components) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path newPath(boolean isAbsolute, List<String> segments) {
        return new PathGeneric(this, isAbsolute, segments);
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

}
