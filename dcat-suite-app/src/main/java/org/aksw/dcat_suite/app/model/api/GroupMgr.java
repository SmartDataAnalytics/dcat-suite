package org.aksw.dcat_suite.app.model.api;

import java.nio.file.Path;

import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocal;

/**
 * An interface to resolve a group id to a folder that holds the repository
 * of the data project for that group.
 *
 * This interface is useful for dependency injection.
 *
 * @author raven
 *
 */
public interface GroupMgr {
    boolean exists();
    void create();

    default void createIfNotExists() {
        if (!exists()) {
            create();
        }
    }

    DcatRepoLocal get();
    void delete();

    /** Return the path to the folder where the group is located or would be if the group was created */
    Path getBasePath();
}
