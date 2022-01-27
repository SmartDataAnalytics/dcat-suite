package org.aksw.dcat_suite.cli.cmd.file;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.dcat.jena.conf.api.DcatRepoConfig;
import org.aksw.difs.builder.DifsFactory;
import org.aksw.difs.system.domain.StoreDefinition;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.aksw.jenax.arq.datasource.RdfDataSourceFromDataset;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateRequest;
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

        StoreDefinition storeDef = conf.getEngineConf().as(StoreDefinition.class)
                .setStorePath("dcat.trig");

        this.dataSource = RdfDataSourceFromDataset.create(DifsFactory.newInstance()
                .setRepoRootPath(repoRootFolder)
                .setStoreDefinition(storeDef).connectAsDataset(), true);
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
}
