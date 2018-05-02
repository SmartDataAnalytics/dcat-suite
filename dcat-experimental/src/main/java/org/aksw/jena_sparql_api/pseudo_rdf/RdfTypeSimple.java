package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.function.Supplier;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

public class RdfTypeSimple
	implements RdfType
{
//	protected Supplier<T> newJa
	protected Supplier<Node> nodeFactory;

	/**
	 * A type might not be instantiable at all
	 */
	public boolean isInstantiable() {
		return true;
	}
	
	
	/**
	 * Check for whether a new instance can be created from the provided arguments
	 * 
	 */
	@Override
	public boolean canNewInstance(RDFNode args) {
		return true;
	}

	@Override
	public Node newInstance(RDFNode args) {
		Node result = nodeFactory.get();
		return result;
	}

}
