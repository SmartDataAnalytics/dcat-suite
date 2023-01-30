package org.aksw.dcat_suite.app.model.api;

import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocal;
import org.eclipse.jgit.lib.Repository;

public interface UserProject
    extends LifeCycleEntityPath
{

    DcatRepoLocal getDcatRepo();
    Repository getGitRepository();
    // UserSpace getUserSpace();
}
