package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Set;

import org.apache.jena.graph.Node;

//public interface PseudoRdfSetObjectProperty {
//	Set<Node> getValues();
//
//	
//	// TODO Probably we need to add a class parameter to support polymorphic elements
//	// I.e. does this property support *creating and adding* a new instance of type X
//	// Or maybe creation should be done elsewhere?
//	// In jena there is the nodeFactory. however, in our case,
//	// the underlying entity may not exist as a object, but only inside some attributes
//	boolean canCreateNew();
//	
//	/**
//	 * Creates a new instance that is
//	 * appropriate to be added to the set of values
//	 *  
//	 * @return
//	 */
//	PseudoRdfResource createNew();
//}