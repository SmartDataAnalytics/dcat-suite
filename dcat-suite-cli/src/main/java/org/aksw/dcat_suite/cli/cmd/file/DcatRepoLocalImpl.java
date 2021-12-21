package org.aksw.dcat_suite.cli.cmd.file;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.dcat.jena.conf.api.DcatRepoConfig;
import org.aksw.difs.builder.DifsFactory;
import org.aksw.difs.system.domain.StoreDefinition;
import org.aksw.jenax.arq.datasource.RdfDataSourceFromDataset;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

public class DcatRepoLocalImpl
    implements DcatRepoLocal
{
    protected Path configFile;
    protected Path repoRootFolder;

    protected RdfDataSourceFromDataset dataSource;

    public static final String NS = "http://dcat.aksw.org/ontology/";
    public static final Resource RepoConfig = ResourceFactory.createResource(NS + "RepoConfig");

    public DcatRepoLocalImpl(Path confFile, Path repoRoot, RdfDataSource dataSource) throws IOException {
        super();
        this.configFile = confFile;
        this.repoRootFolder = repoRoot;
        // this.dataSource = dataSource;

        Model confModel = RDFDataMgr.loadModel(confFile.toString());
        // Get the confg resource
        DcatRepoConfig conf = confModel.listResourcesWithProperty(RDF.type, RepoConfig)
                .nextOptional().map(r -> r.as(DcatRepoConfig.class)).orElse(null);

        StoreDefinition storeDef = conf.getEngineConf().as(StoreDefinition.class);

        this.dataSource = RdfDataSourceFromDataset.create(DifsFactory.newInstance()
                .setRepoRootPath(repoRootFolder.resolve("dcat.trig"))
                .setStoreDefinition(storeDef).connectAsDataset(), true);
    }


    @Override
    public Path getBasePath() {
        return repoRootFolder;
    }

    void addDataset(String groupId, String version, Path path) {

    }


    void addTransform(Path path) {

    }


    void applyTransform() {

    }

    @Override
    public Dataset getDataset() {
        return dataSource.getDataset();
    }
}
