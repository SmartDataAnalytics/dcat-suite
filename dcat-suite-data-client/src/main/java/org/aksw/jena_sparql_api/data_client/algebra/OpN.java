package org.aksw.jena_sparql_api.data_client.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
import org.apache.jena.sparql.algebra.Op;

public interface OpN
	extends Op
{
	@Iri("eg:arg")
	@PolymorphicOnly
	List<Op> getSubOps();
	
	OpN setSubOps(List<Op> subOps);
}
