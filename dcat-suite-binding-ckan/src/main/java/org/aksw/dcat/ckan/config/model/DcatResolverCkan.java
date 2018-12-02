package org.aksw.dcat.ckan.config.model;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;

public interface DcatResolverCkan {
	
	@IriType
	@Iri("eg:url")
	String getUrl();
	@IriType
	DcatResolverCkan setUrl(String url);
	
	
	@Iri("eg:apiKey")
	String getApiKey();
	DcatResolverCkan setApiKey(String apiKey);
}
