package org.aksw.jena_sparql_api.data_client.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;

@RdfType
public interface OpUpdateRequest {
	@Iri
	String getUpdateRequestString();
	OpUpdateRequest setUpdateRequestString(String updateRequestString);
}
