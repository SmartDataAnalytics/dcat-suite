package org.aksw.jena_sparql_api.pseudo_rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class MappingVocab {
	public static final Property r2rmlIRI = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#IRI");
	
	public static final String NS = "http://example.org/";
		
	public static final Property mapping = ResourceFactory.createProperty("http://example.org/mapping");	
	public static final Property type = ResourceFactory.createProperty(NS + "type");

	public static final Property LiteralMapping = ResourceFactory.createProperty(NS + "LiteralMapping");
	public static final Property CollectionMapping = ResourceFactory.createProperty(NS + "CollectionMapping");
	public static final Property JsonArrayMapping = ResourceFactory.createProperty(NS + "JsonArrayMapping");
	public static final Property TemporalMapping = ResourceFactory.createProperty(NS + "TemporalMapping");

	public static final Property target = ResourceFactory.createProperty(NS + "target");
	public static final Property predicate = ResourceFactory.createProperty(NS + "predicate");
	public static final Property key = ResourceFactory.createProperty(NS + "key");

}