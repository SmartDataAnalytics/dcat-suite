package org.aksw.jena_sparql_api.pseudo_rdf;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Combines the functionalities of the following two classes:
 * 
 * - RdfType: used to create java objects from RDFNodes
 * - NodeMapper: used to wrap java objects as a Node (possibly PseudoNode)
 * 
 * 
 * 
 * @author raven May 2, 2018
 *
 */
public interface RdfTypeMapper {
	boolean canNewInstance(RDFNode arg);
	Node newInstance(RDFNode arg);
}
