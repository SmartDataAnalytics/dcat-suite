package org.aksw.dcat_suite.app.model.api;

/**
 * Interface to obtain group managers in certain spaces.
 * Typically such spaces are user spaces and/or a global 'release' space.
 * 
 * @author raven
 *
 */
public interface GroupSpaceMgr {
	GroupMgrFactory getSpace(String spaceName);
}
