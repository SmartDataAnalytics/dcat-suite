package org.aksw.dcat_suite.app.fs2;

import java.nio.file.FileSystem;

public abstract class FileSystemBase
    extends FileSystem
{
    public abstract PathOps getPathOps();
//	public abstract Path resolvePath(Path path);
//
//	public abstract String toString(Path path);
}
