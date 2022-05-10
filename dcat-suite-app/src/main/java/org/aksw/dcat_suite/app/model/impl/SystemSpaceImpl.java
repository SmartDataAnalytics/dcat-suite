package org.aksw.dcat_suite.app.model.impl;

import org.aksw.dcat_suite.app.model.api.SystemSpace;
import org.aksw.dcat_suite.app.model.api.UserSpaceMgr;

public class SystemSpaceImpl
    implements SystemSpace
{
    protected UserSpaceMgr userSpaceMgr;

    public SystemSpaceImpl(UserSpaceMgr userSpaceMgr) {
        super();
        this.userSpaceMgr = userSpaceMgr;
    }

    @Override
    public UserSpaceMgr getUserSpaceMgr() {
        return userSpaceMgr;
    }
}
