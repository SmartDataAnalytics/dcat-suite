package org.aksw.dcat_suite.app.model.impl;

import java.nio.file.Path;

import org.aksw.dcat_suite.app.model.api.UserProjectMgr;
import org.aksw.dcat_suite.app.model.api.UserSpace;

public class UserSpaceImpl
    extends LifeCycleEntityPathImpl
    implements UserSpace
{
    protected UserProjectMgr projectMgr;

    public UserSpaceImpl(Path basePath, String entityId) {
        super(basePath, entityId);

        this.projectMgr = new UserProjectMgrImpl(super.getBasePath());
    }

    @Override
    public UserProjectMgr getProjectMgr() {
        return projectMgr;
    }
}
