package org.aksw.dcat_suite.app.model.impl;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.dcat_suite.app.model.api.UserProject;
import org.aksw.dcat_suite.app.model.api.UserProjectMgr;

public class UserProjectMgrImpl
    extends LifeCycleEntityMgrPathBase<UserProject>
    implements UserProjectMgr
{
    public UserProjectMgrImpl(Path basePath) {
        super(basePath);
    }

    @Override
    public UserProject get(String name) throws IOException {
        return new UserProjectImpl(getBasePath(), name);
    }

}
