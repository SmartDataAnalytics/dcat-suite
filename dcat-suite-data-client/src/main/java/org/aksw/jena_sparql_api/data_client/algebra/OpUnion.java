package org.aksw.jena_sparql_api.data_client.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.algebra.Op;

@RdfType
public interface OpUnion
	extends Resource
{
	@Iri("eg:arg")
	List<Op> getArgs();
}
