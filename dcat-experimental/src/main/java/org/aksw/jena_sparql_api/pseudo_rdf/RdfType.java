package org.aksw.jena_sparql_api.pseudo_rdf;

import org.apache.jena.rdf.model.RDFNode;

/**
 * An RDF datatype.
 * Supports creation of new instances based on the arguments passed to the newInstance method.
 * 
 * 
 * TODO The type should expose the shape about which attributes it needs as input
 * TODO Investigate potential consolidation with mapper's RdfType class 
 * 
 * 
 * @author raven Apr 16, 2018
 *
 */
public interface RdfType<T> {
	boolean canNewInstance(RDFNode args);
	T newInstance(RDFNode args);
}
