package org.aksw.dcat_suite.app.fs2;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utilities for working with 'content' files that carry RDF annotations
 * in the form of companion files with a 'meta.trig' suffix.
 *
 * The metadata's graphs should be relative IRIs.
 * that are resolved against a base URL set that point to the content file.
 * Hence content files can be described using the <> (empty relative URI)
 * as in {@code <> { <> dcat:mimeType "text/csv" }}.
 * Within each graph there should be a subject that matches the named of the graph.
 * This resource acts as the natural entry point.
 * The default graph should not be used - always use a named graph with '<>'.
 * If there is a need for further graphs, their names should always start with
 * a hash - e.g. <#custom-aspects> { }
 *
 *
 * Model 1a:
 * Repeat file name in a 'content' folder. The content folder is there to decrease likelyhood of clashes
 *  /foo/bar.zip/_content/bar.zip
 *  /foo/bar.zip/_content/bar.zip.meta.trig
 *  /foo/bar.zip/report.txt/_content/report.txt
 *
 * Model 1b:
 * Instead of repeating the name, use a generic 'content' and 'meta' file names.
 * But then every file in the repo has a generic name which seems to suck when working with a file
 * broser.
 *
 * 1c:
 * Hybrid of 1a and 1b: 1a by default, but if the repeated filename is too long, then a shorter one, such as 'content.zip'
 * may be used
 *
 *
 * Model 2:
 * Given a bath 'foo/bar':
 * - The content file is a plain file /foo/bar/baz.extension
 * - The metadata file must extend the content file with '.meta.trig' /foo/bar/baz.extension.meta.trig
 * - Nested data can be added to a folder with the file name /foo/bar/baz/nested/report.content.extension
 *
 * @author raven
 *
 */
public interface RdfPaths {

    Path getMetaDataPath(Path path);


    /** Move content files and update references to paths in the metadata */
    void move(Path repoRoot, Path source, Path target);
}
