package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.accessors.PropertySource;
import org.aksw.jena_sparql_api.utils.model.NodeMapper;
import org.apache.jena.graph.Node;


/**
 * Mapper used to wrap Java objects (of type T) as (PseudoRdf)Nodes.
 * 
 * @author raven May 2, 2018
 *
 * @param <T>
 */
public class PseudoNodeMapper<T>
	implements NodeMapper<T>
{
	protected Class<T> clazz;
	protected Function<T, PropertySource> wrapper;
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor;	


	public PseudoNodeMapper(Class<T> clazz, Function<T, PropertySource> wrapper,
			Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor) {
		super();
		this.clazz = clazz;
		this.wrapper = wrapper;
		this.propertyToAccessor = propertyToAccessor;
	}

	@Override
	public Class<?> getJavaClass() {
		return clazz;
	}

	@Override
	public boolean canMap(Node node) {
		boolean result = node instanceof NodeView; // and the wrapped entity must be an instance of clazz && ((PseudoNode)node);
		return result;
	}

	@Override
	public Node toNode(T obj) {
		PropertySource ps = wrapper.apply(obj);
		Node result = new NodeView(ps, propertyToAccessor);
		return result;
	}

	@Override
	public T toJava(Node node) {
		NodeView pseudoNode = (NodeView)node;
		T result = (T)pseudoNode.getSource().getSource();
		return result;
	}	
}

