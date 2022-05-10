package org.aksw.dcat_suite.app.model.impl;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.dcat_suite.app.model.api.UserSpace;
import org.aksw.dcat_suite.app.model.api.UserSpaceMgr;

public class UserSpaceMgrImpl
    extends LifeCycleEntityMgrPathBase<UserSpace>
    implements UserSpaceMgr
{
    public UserSpaceMgrImpl(Path basePath) {
        super(basePath);
    }

    @Override
    public UserSpace get(String name) throws IOException {
        return new UserSpaceImpl(basePath, name);
    }
}

