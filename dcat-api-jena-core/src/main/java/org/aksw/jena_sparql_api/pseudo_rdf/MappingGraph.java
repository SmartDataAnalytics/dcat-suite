package org.aksw.jena_sparql_api.pseudo_rdf;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Graph that retains a mapping between an actual rdf graph and a
 * pseudo rdf graph with its constrains.
 * 
 * 
 * Triples must be connected to existing ones
 * 
 * 
 * @author raven Apr 16, 2018
 *
 */
public class MappingGraph
	extends GraphBase
{
	protected PseudoNode root;
	protected BiMap<Node, PseudoNode> nodeMapping;
	
	
	protected Graph rdfGraph;
	protected Graph pseudoGraph;
	
	public MappingGraph(Graph delegate, Node rootA, PseudoNode rootB) {
		nodeMapping = HashBiMap.create();
		nodeMapping.put(rootA, rootB);
	}

	@Override
	public void performAdd(Triple t) {
		// Check if the subject is already mapped
		Node s = t.getSubject();
		PseudoNode ps = nodeMapping.get(s);
		
		if(ps == null) {
			// TODO Include a hint that the problem might be fixable by ordering the triples by dependency
			throw new RuntimeException("Node " + s + " does not have a corresponding pseudo node");
		}
		
		Node p = t.getPredicate();
		String pStr = p.getURI();

		// get the property descriptor from the pseudo node
		
		//ps.listP
		//ps.listProperties(pStr);
		
		
		// TODO Auto-generated method stub
		super.performAdd(t);
	}

	@Override
	public void performDelete(Triple t) {
		// TODO Auto-generated method stub
		super.performDelete(t);
	}

	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
		// TODO Auto-generated method stub
		return null;
	}
	
}