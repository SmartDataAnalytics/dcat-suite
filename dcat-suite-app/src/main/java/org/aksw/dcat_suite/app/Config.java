package org.aksw.dcat_suite.app;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.io.util.symlink.SymbolicLinkStrategies;
import org.aksw.dcat_suite.app.qualifier.FileStore;
import org.aksw.dcat_suite.app.vaadin.view.FileRepoResolver;
import org.aksw.difs.builder.DifsFactory;
import org.aksw.difs.system.domain.StoreDefinition;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.webdav.WebdavFileSystemConfigBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sys.JenaSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sshtools.vfs2nio.Vfs2NioFileSystemProvider;


@Configuration
public class Config {


    @Autowired
    public ConfigurableApplicationContext context;

/*
    @Bean
    @FileStore
    public Path localFileStore() {
        return Paths.get("/var/www/webdav/gitalog/files");
    }

    @Bean
    public FileRepoResolver fileRepoResolver(@FileStore Path path) {
        return path::resolve;
    }

    @Bean
    @Autowired
    public Dataset dataset() throws Exception {
        JenaSystem.init();

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


        return result;
    }
*/

}
