package org.aksw.jena_sparql_api.data_client.algebra;

import org.apache.jena.rdf.model.Resource;

public interface Op
	extends Resource
{
	<T> T accept(OpVisitor<T> visitor);
}
