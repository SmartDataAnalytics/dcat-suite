package org.aksw.dcat_suite.app.model.impl;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.PathUtils;
import org.aksw.commons.io.util.symlink.SymbolicLinkStrategies;
import org.aksw.dcat_suite.app.model.api.GroupMgr;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocal;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocalUtils;
import org.aksw.difs.builder.DifsFactory;
import org.aksw.difs.system.domain.StoreDefinition;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.webdav.WebdavFileSystemConfigBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sshtools.vfs2nio.Vfs2NioFileSystemProvider;

public class GroupMgrImpl
    implements GroupMgr
{
    private final static Logger logger = LoggerFactory.getLogger(GroupMgrImpl.class);

    // protected DataMgr dataMgr;
    protected String groupId;

    protected Path basePath;
    protected String[] relPath;

    public static String[] mavenCoordinateToSegments(String mvn) {
    	String[] result = Arrays.asList(mvn.split(":")).stream()
    		.flatMap(part -> Arrays.asList(part.split("\\.")).stream())
    		.collect(Collectors.toList())
    		.toArray(new String[0]);
    	return result;
    }
    
    public GroupMgrImpl(String groupId, Path basePath) {
        super();
        this.groupId = groupId;
        this.basePath = basePath;

        this.relPath = mavenCoordinateToSegments(groupId); // UriToPathUtils.javaifyHostnameSegments(groupId);
    }

    @Override
    public boolean exists() {
        return DcatRepoLocalUtils.isRepository(getBasePath());
    }

    @Override
    public void create() {
        try {
            Path path = getBasePath();

            logger.info("Creating data project repository for " + groupId + " at " + path);

            Files.createDirectories(path);
            DcatRepoLocalUtils.init(path);
            
            // FIXME Is there a Git/nio version? .toFile() will break for virtual file systems...
            Git.init().setDirectory(path.toFile()).call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DcatRepoLocal get() {
        return DcatRepoLocalUtils.getLocalRepo(getBasePath());
    }
    
    @Override
    public void delete() {
        throw new UnsupportedOperationException("net yet implemeted");
    }

    @Override
    public Path getBasePath() {
        return PathUtils.resolve(basePath, relPath);
    }


    protected  void createInternal() throws Exception {

        String[] vfsConfWebDav = new String[]{"webdav://localhost", "webdav/gitalog/store.conf.ttl"};
        String[] vfsConfLocalFs = new String[]{"file:///", "/var/www/webdav/gitalog/store.conf.ttl"};
        String[] vfsConfZip = new String[]{"zip:///tmp/gitalog/gitalog.zip", "store.conf.ttl"};


        boolean useJournal = true;
        String[] vfsConf = vfsConfLocalFs;


//		String vfsUri = "file:///var/www/webdav/gitalog/store.conf";
//		String vfsUri = "zip:///tmp/gitalog/gitalog.zip";
        FileSystemOptions webDavFsOpts = new FileSystemOptions();
        WebdavFileSystemConfigBuilder.getInstance().setFollowRedirect(webDavFsOpts, false);

        Map<String, Object> env = new HashMap<>();
        env.put(Vfs2NioFileSystemProvider.FILE_SYSTEM_OPTIONS, webDavFsOpts);

        String vfsUri = vfsConf[0];
        FileSystem fs;

        if (vfsUri.startsWith("file:")) {
            fs = Paths.get("/").getFileSystem();
        } else {
            fs = FileSystems.newFileSystem(
                    URI.create("vfs:" + vfsUri),
                    env);
        }
        // zip file
//		Path basePath = fs.getRootDirectories().iterator().next().resolve("store.conf.ttl");
//		Path basePath = Paths.get("/tmp/gitalog/store.conf");
//		Path basePath = fs.getRootDirectories().iterator().next()
//				 .resolve("var").resolve("www")
//				.resolve("webdav").resolve("gitalog");

        Path basePath = fs.getRootDirectories().iterator().next();
        for (int i = 1; i < vfsConf.length; ++i) {
            String segment = vfsConf[i];
            basePath = basePath.resolve(segment);
        }

        // RDFDataMgrRx
        StoreDefinition sd = RDFDataMgr.loadModel("difs/default-dcat.store.conf.ttl")
                .listSubjects().nextResource().as(StoreDefinition.class);

        DatasetGraph dg = DifsFactory.newInstance()
                .setStoreDefinition(sd)
                .setUseJournal(useJournal)
                .setSymbolicLinkStrategy(SymbolicLinkStrategies.FILE)
                .setConfigFile(basePath)
                .setMaximumNamedGraphCacheSize(10000)
//				.addIndex(RDF.Nodes.type, "type", DatasetGraphIndexerFromFileSystem::uriNodeToPath)
//				.addIndex(NodeFactory.createURI("http://dataid.dbpedia.org/ns/core#group"), "group", DatasetGraphIndexerFromFileSystem::uriNodeToPath)
//				.addIndex(NodeFactory.createURI("http://purl.org/dc/terms/hasVersion"), "version", DatasetGraphIndexerFromFileSystem::iriOrLexicalFormToToPath)
//				.addIndex(DCAT.downloadURL.asNode(), "downloadUrl", DatasetGraphIndexerFromFileSystem::uriNodeToPath)
//				// .addIndex(RDF.Nodes.type, "type", DatasetGraphIndexerFromFileSystem::uriNodeToPath)
//				.addIndex(DCTerms.identifier.asNode(), "identifier", DatasetGraphIndexerFromFileSystem::iriOrLexicalFormToToPath)
                .connect();

        Dataset result = DatasetFactory.wrap(dg);


    }
}
