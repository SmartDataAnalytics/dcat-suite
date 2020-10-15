package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Collection;

import org.aksw.commons.accessors.CollectionAccessor;
import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.NodeMapper;
import org.apache.jena.graph.Node;

import com.google.common.base.Converter;
import com.google.common.collect.Range;

public class PseudoRdfPropertyImpl<T>
	implements PseudoRdfProperty
{
	/** The accessor to retrieve the enity */
	//protected SingleValuedAccessor<Collection<T>> accessor;
	protected CollectionAccessor<T> accessor;
	
	/** The mapper which converts between the underlying entity and nodes */
	protected NodeMapper<T> nodeMapper;

	protected RdfType<T> rdfType;

	/** The supplier of new entities */ 
	//protected RdfType rdfType;
	
	// The available schematic properties 
	//protected Map<String, Function<T, PseudoRdfProperty>> propertyToAccessor;
	
	
	public PseudoRdfPropertyImpl(
			//SingleValuedAccessor<Collection<T>> accessor,
			CollectionAccessor<T> accessor,
			RdfType<T> rdfType,
			NodeMapper<T> nodeMapper) {
			//Function<T, Node> backendToNode) {
		super();
		this.accessor = accessor;
		//this.backendToNode = backendToNode;
		this.rdfType = rdfType;
		this.nodeMapper = nodeMapper;
	}
	
	@Override
	public Collection<Node> getValues() {
		//SetFromSingleValuedAccessor<T> backend = new SetFromSingleValuedAccessor<>(accessor);
		
		Converter<T, Node> converter = new ConverterFromNodeMapper<>(nodeMapper).reverse();
		Collection<T> backend = accessor.get();
		
		if(backend == null) {
			throw new RuntimeException("Got null value for " + accessor);
		}
		
		Collection<Node> result = new ConvertingCollection<>(backend, converter);
		return result;
	}

	@Override
	public Range<Long> getMultiplicity() {
		Range<Long> result = accessor.getMultiplicity();
		return result;
	}
	
	@Override
	public NodeMapper<T> getNodeMapper() {
		return nodeMapper;
	}
	
	@Override
	public RdfType<T> getType() {
		return rdfType;
	}
}

