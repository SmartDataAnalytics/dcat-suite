package org.aksw.dcat_suite.app.model.api;


/**
 * Resolve a groupId to an object that can initialize, delete and verify
 * the repository for that id.
 */
public interface GroupMgrFactory {
    GroupMgr create(String groupId);
}
