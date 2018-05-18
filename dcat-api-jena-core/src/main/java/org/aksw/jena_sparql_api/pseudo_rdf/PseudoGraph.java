package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;

public class PseudoGraph
	extends GraphBase
{

	public PseudoNode getSubjectAsNode(Triple tp) {
		Node node = tp.getSubject();
		
		if(!(node instanceof PseudoNode)) {
			throw new RuntimeException("Only nodes of type " + PseudoNode.class.getName() + " supported, got: " + node);
		}
		
		PseudoNode s = (PseudoNode)node;

		return s;
	}

	@Override
	public void performAdd(Triple t) {
		PseudoNode s = getSubjectAsNode(t);
		
		Collection<Node> values = s.getPropertyValues(t.getPredicate().getURI());
		values.add(t.getObject());
	}
	
	@Override
	public void performDelete(Triple t) {
		PseudoNode s = getSubjectAsNode(t);
		
		Collection<Node> values = s.getPropertyValues(t.getPredicate().getURI());
		values.remove(t.getObject());
	}

	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple tp) {
		PseudoNode s = getSubjectAsNode(tp);

		Node p = tp.getMatchPredicate();
		Node o = tp.getMatchObject();
		
		ExtendedIterator<Triple> result = p == null
				? s.listProperties()
				: s.listProperties(p.getURI());
		
		// TODO make sure filter by object works for pseudo nodes
		result = result.filterKeep(t -> o == null || t.getObject().equals(o));
				
		return result;
	}

	
}