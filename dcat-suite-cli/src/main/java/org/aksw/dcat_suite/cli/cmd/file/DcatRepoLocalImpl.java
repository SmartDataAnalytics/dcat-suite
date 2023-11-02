package org.aksw.dcat_suite.cli.cmd.file;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.commons.io.util.symlink.SymbolicLinkStrategies;
import org.aksw.dcat.jena.conf.api.DcatRepoConfig;
import org.aksw.difs.builder.DifsFactory;
import org.aksw.difs.system.domain.StoreDefinition;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFromDataset;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.jgit.lib.Repository;

public class DcatRepoLocalImpl
    implements DcatRepoLocal
{
    protected Path configFile;
    protected Path repoRootFolder;

    protected RdfDataEngineFromDataset dataSource;

    protected Dataset configDataset;

    // Maybe gitRepository should go to a separate class
    protected Repository gitRepository;

    public static final String NS = "http://dcat.aksw.org/ontology/";
    public static final Resource RepoConfig = ResourceFactory.createResource(NS + "RepoConfig");

    public DcatRepoLocalImpl(Path confFile, Path repoRoot, RdfDataSource dataSource, Repository gitRepository) throws IOException {
        super();
        this.configFile = confFile;
        this.repoRootFolder = repoRoot;

        this.gitRepository = gitRepository;
        // this.dataSource = dataSource;

        this.configDataset = DcatRepoLocalUtils.createDifsFromFile(configFile);

        Txn.executeRead(configDataset, () -> {
            // Get the confg resource
            DcatRepoConfig conf = getConfigResource(configDataset);

            Model copy = ModelFactory.createDefaultModel();
            copy.add(conf.getModel());

            StoreDefinition storeDef = conf.getEngineConf().inModel(copy).as(StoreDefinition.class)
                    .setSingleFile(true)
                    .setStorePath("dcat.trig");

            try {
                this.dataSource = RdfDataEngineFromDataset.create(DifsFactory.newInstance()
                        .setRepoRootPath(repoRootFolder)
                        .setSymbolicLinkStrategy(SymbolicLinkStrategies.FILE)
                        .setStoreDefinition(storeDef).connectAsDataset(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Repository getGitRepository() {
        return gitRepository;
    }

    @Override
    public Path getBasePath() {
        return repoRootFolder;
    }

    @Override
    public void move(Path src, Path tgt) {
        Path srcAbs = repoRootFolder.resolve(src);
        Path tgtAbs = repoRootFolder.resolve(tgt);

        if (!srcAbs.startsWith(repoRootFolder)) {
            throw new RuntimeException(String.format("Source path %s outside of repository %s", srcAbs, repoRootFolder));
        }

        if (!tgtAbs.startsWith(repoRootFolder)) {
            throw new RuntimeException(String.format("Target path %s outside of repository %s", srcAbs, repoRootFolder));
        }

        Path srcRel = repoRootFolder.relativize(srcAbs);
        Path tgtRel = repoRootFolder.relativize(tgtAbs);

        String srcIri = "" + srcRel;
        String tgtIri = "" + tgtRel;

        Dataset dataset = getDataset();
        Txn.executeWrite(dataset, () -> {

            UpdateRequest ur = UpdateExecutionUtils.createUpdateRequestRename(Vars.g, NodeFactory.createURI(srcIri), NodeFactory.createURI(tgtIri));
            org.apache.jena.update.UpdateExecutionFactory.create(ur, dataset).execute();

            try {
                FileUtils.moveAtomic(src, tgt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Note: If the jvm crashes here then the file was already moved but the transaction will be rolled back
            // This would result in an inconsistent state
        });
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

    @Override
    public Dataset getConfig() {
        return this.configDataset;
    }

    @Override
    public DcatRepoConfig getConfigResource(Dataset configDataset) {
        Model model = configDataset.getDefaultModel();
        DcatRepoConfig conf = model.listResourcesWithProperty(RDF.type, RepoConfig)
                .nextOptional().map(r -> r.as(DcatRepoConfig.class)).orElse(null);

        return conf;
    }
}
