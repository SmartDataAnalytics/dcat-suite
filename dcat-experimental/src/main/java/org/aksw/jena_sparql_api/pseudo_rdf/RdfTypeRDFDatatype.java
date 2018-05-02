package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Objects;

import org.aksw.jena_sparql_api.utils.model.NodeMapperRdfDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

public class RdfTypeRDFDatatype<T>
	implements RdfType<T>
{
	protected RDFDatatype dtype;

	public RdfTypeRDFDatatype(Class<T> clazz) {
		this(TypeMapper.getInstance(), clazz);
	}
	
	public RdfTypeRDFDatatype(TypeMapper typeMapper, Class<T> clazz) {
		this(Objects.requireNonNull(typeMapper.getTypeByClass(clazz)));
	}
	
	public RdfTypeRDFDatatype(RDFDatatype dtype) {
		super();
		this.dtype = dtype;
	}

	@Override
	public boolean canNewInstance(RDFNode rdfNode) {
		Node node = rdfNode.asNode();
		boolean result = NodeMapperRdfDatatype.canMapCore(node, dtype);
		return result;
	}

	@Override
	public T newInstance(RDFNode rdfNode) {
		Node node = rdfNode.asNode();
		T result = NodeMapperRdfDatatype.toJavaCore(node, dtype);
		return result;
	}

}
