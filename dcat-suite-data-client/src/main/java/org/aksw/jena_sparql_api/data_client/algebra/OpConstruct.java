package org.aksw.jena_sparql_api.data_client.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface OpConstruct {
	@Iri
	String getQueryString();
	OpConstruct setQueryString(String queryString);
}
