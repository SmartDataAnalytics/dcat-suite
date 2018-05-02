package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Collection;

import org.apache.jena.graph.Node;

import com.google.common.collect.Range;

interface PseudoRdfProperty {
	/**
	 * A collection view of the property values
	 * Implementations should support removals
	 * If the underlying field is single valued, removal should unset the field
	 * 
	 * For additions, convert to {@link PseudoRdfSetObjectProperty} first
	 * 
	 * @return
	 */
	Collection<Node> getValues();
	
	Range<Long> getMultiplicity();
}