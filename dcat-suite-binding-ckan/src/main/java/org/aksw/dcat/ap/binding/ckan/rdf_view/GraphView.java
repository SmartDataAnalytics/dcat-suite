package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.Collection;

import org.aksw.jena_sparql_api.pseudo_rdf.NodeView;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

/**
 * A graph view for use with {@link NodeView} objects.
 * This graph implementation does not have a state and thus does not contain any triples.
 * Instead, triples are virtually created as follows:
 * The find method requires the subject to match a NodeView object, otherwise an exception will be raised.
 * The request to yield triples is then delegated to the NodeView object.
 *  
 * 
 * @author Claus Stadler, Jul 5, 2018
 *
 */
public class GraphView
	extends GraphBase
{

	public NodeView getSubjectAsNode(Triple tp) {
		NodeView s;

		Node node = tp.getSubject();		
		boolean strict = false;

		
		if(!(node instanceof NodeView)) {
			if(strict) {
				throw new RuntimeException("Only nodes of type " + NodeView.class.getName() + " supported, got: " + node);
			} else {
				s = null;
			}
		} else {
			s = (NodeView)node;			
		}

		return s;
	}

	@Override
	public void performAdd(Triple t) {
		NodeView s = getSubjectAsNode(t);
		
		Collection<Node> values = s.getPropertyValues(t.getPredicate().getURI());
		values.add(t.getObject());
	}
	
	@Override
	public void performDelete(Triple t) {
		NodeView s = getSubjectAsNode(t);
		
		Collection<Node> values = s.getPropertyValues(t.getPredicate().getURI());
		values.remove(t.getObject());
	}

	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple tp) {
		NodeView s = getSubjectAsNode(tp);

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