package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.util.Set;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.accessors.SingleValuedAccessorFromCollection;
import org.aksw.commons.accessors.SingleValuedAccessorImpl;
import org.aksw.jena_sparql_api.utils.model.NodeMapper;
import org.aksw.jena_sparql_api.utils.model.SetFromMappedPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class RdfDcatApResource
	extends ResourceImpl
{
	public RdfDcatApResource(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	public static <T> SingleValuedAccessor<T> create(Resource s, Property p, NodeMapper<T> nodeMapper) {
		return new SingleValuedAccessorFromCollection<>(new SetFromMappedPropertyValues<>(s, p, nodeMapper));
	}

	
	public static <T> SingleValuedAccessor<Set<T>> createSet(Resource s, Property p, NodeMapper<T> nodeMapper) {
		Set<T> set = new SetFromMappedPropertyValues<>(s, p, nodeMapper);
		SingleValuedAccessor<Set<T>> result = new SingleValuedAccessorImpl<>(
				() -> set,
				arg -> { set.clear(); set.addAll(arg); });
		
		return result;
	}

}
