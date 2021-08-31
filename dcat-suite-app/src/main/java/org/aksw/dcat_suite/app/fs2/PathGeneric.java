package org.aksw.dcat_suite.app.fs2;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

public class PathGeneric
    extends PathBase<PathGeneric> {

    protected FileSystem fs;
    protected PathOps pathOps;

    public PathGeneric(PathOps pathOps, boolean isAbsolute, List<String> segments) {
        super(isAbsolute, segments);
        // this.fs = fs;
        this.pathOps = pathOps;
        // Files.isRegularFile(getFileName(), null)
    }

    @Override
    public FileSystem getFileSystem() {
        return fs;
    }

//	@Override
//	public Path newPath(boolean isAbsolute, List<String> segments) {
//		return new PathGeneric(fs, isAbsolute, segments);
//	}

    @Override
    public PathGeneric requireSubType(Path other) {
        return (PathGeneric)other;
    }

    @Override
    public Path newPath(boolean isAbsolute, List<String> segments) {
        return pathOps.newPath(isAbsolute, segments);
    }

    @Override
    protected PathOps getPathOpts() {
        return pathOps;
    }


}
