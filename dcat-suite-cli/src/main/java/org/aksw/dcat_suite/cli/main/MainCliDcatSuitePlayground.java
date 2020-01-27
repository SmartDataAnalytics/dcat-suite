package org.aksw.dcat_suite.cli.main;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.aksw.ckan_deploy.core.DcatRepositoryDefault;
import org.aksw.dcat_suite.core.docker.ImageSpec;
import org.aksw.dcat_suite.plugin.virtuoso.ImageSpecVirtuoso;
import org.aksw.jena_sparql_api.conjure.resourcespec.ResourceSpecUtils;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.fs.DatasetFromWatchedFolder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;

public class MainCliDcatSuitePlayground {

	// Test for resolving placeholders in the config
	public static void main(String[] args) throws IOException {
		Model model = RDFDataMgr.loadModel("/home/raven/.dcat/settings.ttl");
		ResourceSpecUtils.resolve(model);
		RDFDataMgr.write(System.out,  model, RDFFormat.TURTLE_PRETTY);
	}


	public static void main2(String[] args) throws Exception {
        JenaSystem.init();
		JenaPluginUtils.scan(ImageSpec.class);
        JenaPluginUtils.scan(ImageSpecVirtuoso.class);

        DcatRepositoryDefault repo = (DcatRepositoryDefault)MainCliDcatSuite.createDcatRepository();
		Path repoRootPath = repo.getDcatRepoRoot();
		
		System.out.println(repoRootPath);
		//repo.
		DatasetFromWatchedFolder watcher = new DatasetFromWatchedFolder(repoRootPath.getParent().resolve("settings.d"));
        watcher.init();
		Dataset ds = watcher.getDataset();

		Model unionModel = ds.getUnionModel();
		ds = DatasetFactory.wrap(unionModel);
		Thread watchThread = new Thread(watcher, "Dataset Watcher on " + repoRootPath);
		watchThread.start();
        try(RDFConnection conn = RDFConnectionFactory.connect(ds)) {
        	//SparqlRx.execPartitioned(conn, "?s { }")
//			+ "//SparqlRx.execPartitioned(conn, Vars.s, QueryFactory.create("CONSTRUCT {} { GRAPH ?g { ?s a <http://www.example.org/ImageSpec> } }"))

        	
        	List<ImageSpecVirtuoso> l = SparqlRx.execSelect(() -> conn.query("SELECT ?s { ?s a <http://www.example.org/ImageSpec> }"))
//        	List<ImageSpecVirtuoso> l = SparqlRx.execSelect(() -> conn.query("SELECT ?s { GRAPH ?g { ?s a <http://www.example.org/ImageSpec> } }"))
        		.map(qs -> qs.get("s").as(ImageSpecVirtuoso.class))
//        		.map(r -> r.as(ImageSpecVirtuoso.class))
        		.toList()
        		.blockingGet();
        	for(ImageSpecVirtuoso i : l) {
        		System.out.println("Got: " + i.getAllowedDir());
        	}
        }

		//QueryExecutionFactory.create("SELECT * { GRAPH ?g { ?s ?p ?o } }", ds);
		RDFDataMgr.write(System.out, ds, RDFFormat.TRIG_PRETTY);

//		watcher.cancel();
		watchThread.interrupt();

	}
}
