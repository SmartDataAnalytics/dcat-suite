package integrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;

public class IntegrationFactory {
	
	private static final String RESOURCE_VAR = "?resource";
	private static final String LINK_VAR = "?link";
	private static final String LINKPROP_VAR = "?linkProp";
	private static final String DATASET_VAR = "?dataset";
	private static final String EXTERNAL_VAR = "?external";
	private static final String PROPERTY_VAR = "?property";
	private static final String VALUE_VAR = "?value";
	
	
	
	public static Model integrate (Model dcatModel, Model linkModel, Model mapModel, RDFConnection conn) {
	
			ResultSet linkResult = queryLinkModel(linkModel); 

			HashMap<Node,List<Node>> resourceToLink = new HashMap<>();
			HashMap<Node,List<Node>> linkToResource = new HashMap<>();
			HashMap<String,String> linkProperties = new HashMap<>(); 
		    while(linkResult.hasNext()) {
		    	QuerySolution linkSolution = linkResult.nextSolution();
		    	Node resource = linkSolution
	    				.get(RESOURCE_VAR)
	    				.asNode();
		    	
		    	Node link = linkSolution
	    				.get(LINK_VAR)
	    				.asNode();
		    	
		    	Node linkProperty = linkSolution
	    				.get(LINKPROP_VAR)
	    				.asNode();
		    	resourceToLink = IntegrationUtils.createLinkMap(resourceToLink, resource, link);
		    	linkToResource = IntegrationUtils.createLinkMap(linkToResource, link, resource);
		    	linkProperties.put(resource.toString().concat(link.toString()), linkProperty.toString());
		    }	
			
		    ResultSet modelResult = queryDataModel(dcatModel);
		    List<Node> nodes = new ArrayList<Node>(); 
		    while(modelResult.hasNext())
		    {
		        Node node = modelResult
		        		.nextSolution()
		        		.get(DATASET_VAR)
		        		.asNode();
		        if (resourceToLink.containsKey(node)) {
		        	System.out.println(resourceToLink.get(node)); 
		        	nodes.addAll(resourceToLink.get(node)); 
		        }
		    }
		    
		    HashMap<Node,Node> mappingMap=getMappingMap(new HashMap<Node,Node>(),mapModel); 
		    String nodesValuesString = IntegrationUtils.getValuesString(nodes.toArray());
		    String propertyValuesString = IntegrationUtils.getValuesString(mappingMap.keySet().toArray());
		    
		    ResultSet dataResult = queryEndpoint(nodesValuesString, propertyValuesString, conn); 
		    while(dataResult.hasNext()) {	
		    	QuerySolution dataSolution = dataResult.nextSolution(); 
		    	for (Node resource : linkToResource.get(dataSolution.get(EXTERNAL_VAR).asNode())) {
	    			Resource source = dcatModel.createResource(resource.toString()); 
	    			if (!dcatModel.containsResource(dataSolution.get(EXTERNAL_VAR).asResource())) {
	    				Resource external = dcatModel.createResource(dataSolution.get(EXTERNAL_VAR).toString()); 
	    				Property linkProperty = dcatModel
	    						.createProperty(linkProperties
	    								.get(source.
	    										toString().
	    										concat(external.
	    												toString())));
	    				dcatModel.add(source, linkProperty, external);
		    		}
	   
	    			Property property = dcatModel
	    					.createProperty(mappingMap
	    							.get(dataSolution.get(PROPERTY_VAR).asNode())
	    							.toString()); 
	    			dcatModel.add(source,property,dataSolution.get(VALUE_VAR)); 
		    	
		    	}
		    }
		    return dcatModel; 
	 }
	
	private static ResultSet queryLinkModel (Model linkModel) {
		SelectBuilder linkBuilder = new SelectBuilder()
				.addVar("*")
				.addWhere(RESOURCE_VAR, LINKPROP_VAR, LINK_VAR);
	    QueryExecution linkExecution = QueryExecutionFactory.create(linkBuilder.build(),linkModel);
	    return linkExecution.execSelect();
	}
	
	private static ResultSet queryDataModel(Model dcatModel) {
		SelectBuilder minus = new SelectBuilder()
				.addPrefix("dcat", DCAT.getURI())
				.addPrefix("rdf", RDF.uri)
				.addWhere( DATASET_VAR, "rdf:type", "dcat:Dataset");
		SelectBuilder modelBuilder = new SelectBuilder()
			    .addVar( DATASET_VAR )
			    .addPrefix("rdf", RDF.uri)
			    .addWhere( DATASET_VAR, "rdf:type", "?o")
			    .addMinus( minus );
		
	    return QueryExecutionFactory
	    		.create(modelBuilder.build(),dcatModel)
	    		.execSelect();
	}
	
	private static  HashMap<Node,Node> getMappingMap(HashMap<Node,Node> mappingMap, Model mapModel) {
		SelectBuilder mappingBuilder = new SelectBuilder()
				.addVar("?source")
				.addVar("?target")
				.addWhere("?source", "?p", "?target");
	    QueryExecution mapExecution = QueryExecutionFactory.create(mappingBuilder.build(),mapModel);
	    ResultSet mapResult = mapExecution.execSelect();
	    while(mapResult.hasNext())
	    {
	    	QuerySolution mapSolution = mapResult.nextSolution();
	    	mappingMap.put(mapSolution.get("?source").asNode(), mapSolution.get("?target").asNode()); 
	    }
	    return mappingMap;
	}
	
	private static ResultSet queryEndpoint (String nodesValuesString, String propertyValuesString, RDFConnection conn) {
		String query = "SELECT DISTINCT "+EXTERNAL_VAR+" "+PROPERTY_VAR+" "+VALUE_VAR+" WHERE { "
	    		+ "VALUES "+PROPERTY_VAR+" { "+propertyValuesString+" } "
	    		+ "VALUES "+EXTERNAL_VAR+" { "+nodesValuesString+" } "
	    		+ EXTERNAL_VAR+" "+PROPERTY_VAR+" "+VALUE_VAR+" . }";
	    System.out.println(query); 
	    QueryExecution dataExecution = conn.query(query);
	    return dataExecution.execSelect();

	}
}
