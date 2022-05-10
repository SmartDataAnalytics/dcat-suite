package org.aksw.dcat_suite.app.model.api;

public interface UserSpace
    extends LifeCycleEntityPath
{
    // Reverse links
    // getUserSpaceMgr

    UserProjectMgr getProjectMgr();
}
