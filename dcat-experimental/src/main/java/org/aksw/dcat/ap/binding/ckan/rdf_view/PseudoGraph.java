package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.Collection;

import org.aksw.jena_sparql_api.pseudo_rdf.PseudoNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

public class PseudoGraph
	extends GraphBase
{

	public PseudoNode getSubjectAsNode(Triple tp) {
		PseudoNode s;

		Node node = tp.getSubject();		
		boolean strict = false;

		
		if(!(node instanceof PseudoNode)) {
			if(strict) {
				throw new RuntimeException("Only nodes of type " + PseudoNode.class.getName() + " supported, got: " + node);
			} else {
				s = null;
			}
		} else {
			s = (PseudoNode)node;			
		}

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

		ExtendedIterator<Triple> result;
		if(s != null) {
			
			Node p = tp.getMatchPredicate();
			Node o = tp.getMatchObject();
			
			result = p == null
					? s.listProperties()
					: s.listProperties(p.getURI());
			
			// TODO make sure filter by object works for pseudo nodes
			result = result.filterKeep(t -> o == null || t.getObject().equals(o));
		} else {
			result = NullIterator.instance();
		}
		return result;
	}

	
}