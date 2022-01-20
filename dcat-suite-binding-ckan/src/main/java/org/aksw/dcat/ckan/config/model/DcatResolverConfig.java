package org.aksw.dcat.ckan.config.model;

import java.util.List;

import org.aksw.dcat.repo.impl.model.DcatResolver;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.rdf.model.Resource;

public interface DcatResolverConfig
	extends Resource
{
	@Iri("eg:resolvers")
	<T extends DcatResolver> List<T> resolvers(Class<T> clazz);
}
