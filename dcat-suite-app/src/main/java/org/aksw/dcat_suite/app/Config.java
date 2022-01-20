package org.aksw.dcat_suite.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.aksw.dcat_suite.app.model.impl.GroupMgrImpl;
import org.aksw.dcat_suite.app.qualifier.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.StandardSystemProperty;


@Configuration
public class Config {


    @Autowired
    public ConfigurableApplicationContext context;

    @Bean
    @FileStore
    public Path localFileStore() {
        Path tmpDir = Path.of(StandardSystemProperty.JAVA_IO_TMPDIR.value());
        Path result = tmpDir.resolve("dman");

        try {
            Files.createDirectories(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;

        // return Paths.of(StandardSystemProperty.JAVA_IO_TMPDIR.value() + "/dman"); // Paths.get("/var/www/webdav/gitalog/files");
    }

//    @Bean
//    public FileRepoResolver fileRepoResolver(@FileStore Path path) {
//        return path::resolve;
//    }


    @Bean
    public GroupMgrFactory repositoryMgr(@FileStore Path path) {
        return groupId -> new GroupMgrImpl(groupId, path);
    }

//    @Bean
//    @Autowired
//    public Dataset dataset() throws Exception {
//        JenaSystem.init();
//
//        String[] vfsConfWebDav = new String[]{"webdav://localhost", "webdav/gitalog/store.conf.ttl"};
//        String[] vfsConfLocalFs = new String[]{"file:///", "/var/www/webdav/gitalog/store.conf.ttl"};
//        String[] vfsConfZip = new String[]{"zip:///tmp/gitalog/gitalog.zip", "store.conf.ttl"};
//
//
//        boolean useJournal = true;
//        String[] vfsConf = vfsConfLocalFs;
//
//
////		String vfsUri = "file:///var/www/webdav/gitalog/store.conf";
////		String vfsUri = "zip:///tmp/gitalog/gitalog.zip";
//        FileSystemOptions webDavFsOpts = new FileSystemOptions();
//        WebdavFileSystemConfigBuilder.getInstance().setFollowRedirect(webDavFsOpts, false);
//
//        Map<String, Object> env = new HashMap<>();
//        env.put(Vfs2NioFileSystemProvider.FILE_SYSTEM_OPTIONS, webDavFsOpts);
//
//        String vfsUri = vfsConf[0];
//        FileSystem fs;
//
//        if (vfsUri.startsWith("file:")) {
//            fs = Paths.get("/").getFileSystem();
//        } else {
//            fs = FileSystems.newFileSystem(
//                    URI.create("vfs:" + vfsUri),
//                    env);
//        }
//        // zip file
////		Path basePath = fs.getRootDirectories().iterator().next().resolve("store.conf.ttl");
////		Path basePath = Paths.get("/tmp/gitalog/store.conf");
////		Path basePath = fs.getRootDirectories().iterator().next()
////				 .resolve("var").resolve("www")
////				.resolve("webdav").resolve("gitalog");
//
//        Path basePath = fs.getRootDirectories().iterator().next();
//        for (int i = 1; i < vfsConf.length; ++i) {
//            String segment = vfsConf[i];
//            basePath = basePath.resolve(segment);
//        }
//
//        // RDFDataMgrRx
//        StoreDefinition sd = RDFDataMgr.loadModel("difs/default-dcat.store.conf.ttl")
//                .listSubjects().nextResource().as(StoreDefinition.class);
//
//        DatasetGraph dg = DifsFactory.newInstance()
//                .setStoreDefinition(sd)
//                .setUseJournal(useJournal)
//                .setSymbolicLinkStrategy(SymbolicLinkStrategies.FILE)
//                .setConfigFile(basePath)
//                .setMaximumNamedGraphCacheSize(10000)
////				.addIndex(RDF.Nodes.type, "type", DatasetGraphIndexerFromFileSystem::uriNodeToPath)
////				.addIndex(NodeFactory.createURI("http://dataid.dbpedia.org/ns/core#group"), "group", DatasetGraphIndexerFromFileSystem::uriNodeToPath)
////				.addIndex(NodeFactory.createURI("http://purl.org/dc/terms/hasVersion"), "version", DatasetGraphIndexerFromFileSystem::iriOrLexicalFormToToPath)
////				.addIndex(DCAT.downloadURL.asNode(), "downloadUrl", DatasetGraphIndexerFromFileSystem::uriNodeToPath)
////				// .addIndex(RDF.Nodes.type, "type", DatasetGraphIndexerFromFileSystem::uriNodeToPath)
////				.addIndex(DCTerms.identifier.asNode(), "identifier", DatasetGraphIndexerFromFileSystem::iriOrLexicalFormToToPath)
//                .connect();
//
//        Dataset result = DatasetFactory.wrap(dg);
//
//
//        return result;
//    }


}
