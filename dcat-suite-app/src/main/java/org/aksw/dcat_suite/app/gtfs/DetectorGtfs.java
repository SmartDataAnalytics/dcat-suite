package org.aksw.dcat_suite.app.gtfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipException;

import com.google.common.net.MediaType;

/**
 * This class detects {@link Path}s that correspond to gtfs files:
 * The path must be a zip archive that contains a set of mandatory file names
 * at the top level.
 *
 * @author raven
 *
 */
public class DetectorGtfs {

    /** The file names that are mandatory in a gtfs zip archive */
    public static final List<String> MANDATORY_FILES = Arrays.asList(
        "agency.txt",
        "routes.txt",
        "trips.txt",
        "stop_times.txt",
        "stops.txt",
        "calendar.txt"
    );

    /** Tests whether the given path is a zip archive which contains the mandatory gtfs files */
    public static boolean isGtfs(Path path) throws IOException {

        boolean isAllMandatoryFilesPresent = false;

        String contentType = Files.probeContentType(path);

        if (MediaType.ZIP.toString().equals(contentType)) {

            // We use the native ZipFileSystemProvider via jar: scheme
            // If Apache VFS2 and vfs2nio are present then the alternative
            // {@code URI.create("vfs:zip:" + path.toUri())} can be used
            try (FileSystem fs = FileSystems.newFileSystem(
                    URI.create("jar:" + path.toUri()),
                    Collections.emptyMap())) {

                for (Path root : fs.getRootDirectories()) {
                    Set<String> topLevelFileNames = new LinkedHashSet<>();
                    try (Stream<Path> stream = Files.list(root)) {
                        stream
                            .map(Path::getFileName)
                            .map(Object::toString)
                            .forEach(topLevelFileNames::add);
                    }

                    isAllMandatoryFilesPresent = topLevelFileNames.containsAll(MANDATORY_FILES);
                    if (isAllMandatoryFilesPresent) {
                        break;
                    }
                }
            } catch (ZipException e) {
                // Possibly corrupt zip file - Ignore
            }
        }

        return isAllMandatoryFilesPresent;
    }


}
