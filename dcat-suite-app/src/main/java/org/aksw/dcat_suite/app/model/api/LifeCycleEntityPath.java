package org.aksw.dcat_suite.app.model.api;

import java.nio.file.Path;

public interface LifeCycleEntityPath
    extends LifeCycleEntity
{

    /** Return the path to the folder where the group is located or would be if the group was created */
    Path getBasePath();

}
