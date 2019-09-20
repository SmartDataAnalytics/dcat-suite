package org.aksw.dcat_suite.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType("eg:OpCode")
public interface OpVar
	extends Op0
{
	@IriNs("eg")
	String getName();
	OpVar setName(String name);

	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	
	public static OpVar create(String name) {
		OpVar result = ModelFactory.createDefaultModel()
				.createResource().as(OpVar.class)
				.setName(name);

		return result;
	}
}