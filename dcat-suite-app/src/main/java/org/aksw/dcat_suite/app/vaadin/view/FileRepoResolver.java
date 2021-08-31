package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Path;

/**
 * Resolve an identifier to a directory where files can be stored
 *
 * @author raven
 *
 */
public interface FileRepoResolver {
    Path getRepo(String id);
}
