package org.aksw.dcat.ckan.config.model;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;

public interface DcatResolverConfig
	extends Resource
{
	@Iri("eg:resolvers")
	<T extends Resource> List<T> resolvers(Class<T> clazz);
}
