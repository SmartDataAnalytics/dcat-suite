package org.aksw.dcat_suite.app.model.api;

import java.nio.file.Path;

@FunctionalInterface
public interface HasBasePath {
    Path getBasePath();
}
