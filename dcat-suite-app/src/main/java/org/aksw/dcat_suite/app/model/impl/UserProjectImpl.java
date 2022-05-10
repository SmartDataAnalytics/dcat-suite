package org.aksw.dcat_suite.app.model.impl;

import java.nio.file.Path;

import org.aksw.dcat_suite.app.model.api.UserProject;

public class UserProjectImpl
    extends LifeCycleEntityPathImpl
    implements UserProject
{

    public UserProjectImpl(Path basePath, String entityId) {
        super(basePath, entityId);
    }
}
