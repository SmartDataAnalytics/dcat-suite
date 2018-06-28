package org.aksw.jena_sparql_api.pseudo_rdf;

import org.apache.jena.rdf.model.RDFNode;

/**
 * An RDF datatype.
 * Supports creation of new instances based on the arguments passed to the newInstance method.
 * 
 * 
 * TODO Investigate potential consolidation with mapper's RdfType class ; it may turn out that the other type class should extend this one
 * TODO The type should expose the shape about which attributes it needs as input
 * 
 * 
 * @author raven Apr 16, 2018
 *
 */
public interface RdfType<T> {
	boolean canNewInstance(RDFNode args);
	T newInstance(RDFNode args);
}
