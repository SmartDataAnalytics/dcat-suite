package org.aksw.dcat_suite.app.model.impl;

import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.commons.io.util.PathUtils;
import org.aksw.commons.util.string.StringUtils;
import org.aksw.dcat_suite.app.model.api.LifeCycleEntityPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifeCycleEntityPathImpl
    // extends LifeCycleEntityPathImpl
    implements LifeCycleEntityPath
{
    private final static Logger logger = LoggerFactory.getLogger(LifeCycleEntityPathImpl.class);

    // protected DataMgr dataMgr;
    protected String entityId;

    protected Path basePath;
    protected String[] relPath;

    protected Path userPath;


    public LifeCycleEntityPathImpl(Path basePath, String entityId) {
        super();
        this.basePath = basePath;
        this.entityId = entityId;

        this.relPath = new String[] { StringUtils.urlEncode(entityId) };

        userPath = PathUtils.resolve(basePath, relPath);
    }

    public Path getEntityPath() {
        return userPath;
    }

    @Override
    public boolean exists() {
        return Files.exists(userPath);
        // return DcatRepoLocalUtils.isRepository(getBasePath());
    }

    @Override
    public void create() throws Exception {
            Path path = getBasePath();

            logger.info("Creating entity for " + entityId + " at " + path);

            Files.createDirectories(path);
            // DcatRepoLocalUtils.init(path);

            // FIXME Is there a Git/nio version? .toFile() will break for virtual file systems...
            // Git.init().setDirectory(path.toFile()).call();
    }

    //@Override
    //public UserSpace get() {
    //    return DcatRepoLocalUtils.getLocalRepo(getBasePath());
    //}

    @Override
    public void delete() throws Exception {
        throw new UnsupportedOperationException("net yet implemeted");
    }

    @Override
    public Path getBasePath() {
        return PathUtils.resolve(basePath, relPath);
    }

}
