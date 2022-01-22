package org.aksw.dcat_suite.app;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
public class MainAppDcatSuiteVaadin extends SpringBootServletInitializer {

    public static void main(String[] args) {

//    	Path p = Path.of("/tmp/test.txt");
//    	PathMatcher pm = p.getFileSystem().getPathMatcher("glob:/tmp/test.txt");
//    	System.out.println(pm.matches(p));
    	
    	
    	if (false) {
	    	// Does jena retain the graph from which a resource originates? -> answer is: no!
	    	Dataset ds = DatasetFactory.create();
	    	ds.getNamedModel(RDF.type).add(RDF.type, RDF.type, RDF.type);
	    	Resource r;
	    	try (QueryExecution qe = QueryExecutionFactory.create("SELECT ?s { GRAPH ?g { ?s a ?o } }", ds)) {
	    		r = qe.execSelect().nextSolution().getResource("s");
	    	}
	    	r.addProperty(RDFS.label, "type");
	    	RDFDataMgr.write(System.out, ds, RDFFormat.TRIG_PRETTY);
    	}
    	
        SpringApplication.run(MainAppDcatSuiteVaadin.class, args);

    }

}