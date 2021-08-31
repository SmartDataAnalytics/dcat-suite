package org.aksw.dcat_suite.app.fs2;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

public interface PathOps {
    String getParentToken();
    List<String> getBasePathSegments();
    URI toUri(List<String> components);
    Path newPath(boolean isAbsolute, List<String> components);

    FileSystem getFileSystem();
}
