package org.aksw.dcat_suite.app.fs2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.collect.Iterables;

public class RdfPathsImpl
    implements RdfPaths
{
    protected String METADATA_SUFFIX = ".meta.trig";

//    protected Path resolveContent(Path path) {
//        return path.resolve("_content");
//    }

    protected Path resolveContentFolder(Path path) {
        Path contentFolder = path.resolve("_content");
        return contentFolder;
    }

    protected Path resolveContentFile(Path path) {
        String fileName = path.getFileName().toString();
        Path contentFolder = resolveContentFile(path);
        Path contentFile = contentFolder.resolve(fileName);
        return contentFile;
    }

    public PathRdf importFile(Path srcFile, Path targetFile, boolean deleteSrcFile) throws IOException {
        if (!Files.isRegularFile(srcFile)) {
            throw new IllegalArgumentException("Not a regular file: " + srcFile);
        }

        if (Iterables.isEmpty(targetFile)) {
            throw new RuntimeException("No target file name given");
        }

        Path targetFileFolder = resolveContentFile(targetFile);
        Files.createDirectories(targetFileFolder.getParent());

        Files.copy(srcFile, targetFileFolder);

        // return new PathRdf(targetFile);
        return null;
    }


    @Override
    public Path getMetaDataPath(Path path) {
        Path result = path.resolveSibling(path.getFileName() + METADATA_SUFFIX);
        return result;
    }

    @Override
    public void move(Path repoRoot, Path source, Path target) {
        // TODO Auto-generated method stub

    }
}
