package org.aksw.dcat_suite.app.model.impl;

import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.dcat_suite.app.model.api.UserProject;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocal;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocalUtils;
import org.eclipse.jgit.lib.Repository;

public class UserProjectImpl
    extends LifeCycleEntityPathImpl
    implements UserProject
{
    protected DcatRepoLocal dcatRepo;
    protected Repository gitRepo;

    public UserProjectImpl(Path basePath, String entityId) {
        super(basePath, entityId);

        this.dcatRepo = null;
    }

    @Override
    public void create() throws Exception {
        Path path = super.getEntityPath();
        Files.createDirectories(path);
        DcatRepoLocalUtils.init(path);
        super.create();
    }

    @Override
    public DcatRepoLocal getDcatRepo() {
        if (dcatRepo == null) {
            dcatRepo = DcatRepoLocalUtils.getLocalRepo(super.getBasePath());
        }
        return dcatRepo;
    }

    @Override
    public Repository getGitRepository() {
        return null;
    }
}
