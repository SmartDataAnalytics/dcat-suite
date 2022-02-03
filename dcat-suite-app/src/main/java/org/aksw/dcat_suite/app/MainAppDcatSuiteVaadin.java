package org.aksw.dcat_suite.app;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
@ServletComponentScan 
public class MainAppDcatSuiteVaadin extends SpringBootServletInitializer {

    public static void main(String[] args) throws MismatchedDimensionException, FactoryException, TransformException {

//    	if (true) {
//	    	String base = "";
//	    	System.out.println(NodeFunctions.iri(NodeValue.makeString("relativeIri"), base));
////	    	System.out.println(NodeFunctions.iri(NodeValue.makeString("/relativeIri"), base));
////	    	System.out.println(NodeFunctions.iri(NodeValue.makeString("#relativeIri"), base));
////	    	System.out.println(NodeFunctions.iri(NodeValue.makeString("urn:absoluteeIri"), base));
//	    	return;
//    	}
    	
//    	if (true) {
//    		System.out.println(NodeFactory.createURI("http://bar.baz").toString(false));
//    		System.out.println(NodeFactory.createBlankNode("weee").toString(false));
//    		System.out.println(NodeFactory.createLiteral("weee").toString(false));
//    	}
    	// GeoSPARQLConfig.setupNoIndex();
    	
//    	Path p = Path.of("/tmp/test.txt");
//    	PathMatcher pm = p.getFileSystem().getPathMatcher("glob:/tmp/test.txt");
//    	System.out.println(pm.matches(p));
    	
    	if (false) {
    		GeometryWrapper orig = GeometryWrapper.fromPoint(89, 179, SRS_URI.WGS84_CRS);
    		System.out.println("Original:");
    		System.out.println(orig.getParsingGeometry());
    		System.out.println(orig.getXYGeometry());

    		GeometryWrapper origEnv = orig.envelope();
    		System.out.println("Envelope:");
    		System.out.println(origEnv.getParsingGeometry());
    		System.out.println(origEnv.getXYGeometry());
    		
    		GeometryWrapper conv = orig.convertSRS(SRS_URI.DEFAULT_WKT_CRS84);
    		System.out.println("Converted:");
    		System.out.println(conv.getParsingGeometry());
    		System.out.println(conv.getXYGeometry());
    		
    		GeometryWrapper convEnv = origEnv.convertSRS(SRS_URI.DEFAULT_WKT_CRS84);
    		System.out.println("Converted Envelope:");
    		System.out.println(convEnv.getParsingGeometry());
    		System.out.println(convEnv.getXYGeometry());

    		/*
    		Original:
			POINT (89 179)
			POINT (179 89)
			Envelope:
			POINT (89 179)
			POINT (179 89)
			Converted:
			POINT (179 89)
			POINT (179 89)
			Converted Envelope:
			POINT (179 89)
			POINT (179 89)
    		 */
    		return;
    	}
    	
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