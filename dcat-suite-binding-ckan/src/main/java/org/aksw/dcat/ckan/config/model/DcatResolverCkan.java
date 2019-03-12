package org.aksw.dcat.ckan.config.model;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.apache.jena.rdf.model.Resource;

public interface DcatResolverCkan
	extends Resource
{
	@IriType
	@Iri("eg:url")
	String getUrl();
	DcatResolverCkan setUrl(String url);

	@Iri("eg:apiKey")
	String getApiKey();
	DcatResolverCkan setApiKey(String apiKey);
}
