package org.aksw.dcat.server.config;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.impl.model.CatalogResolverModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigDcatCatalog {
	@Bean
	public CatalogResolver catalogResolver() {
		Model model = RDFDataMgr.loadModel("/home/raven/Projects/limbo/git/metadata-catalog/catalog.all.ttl");
		CatalogResolver result = new CatalogResolverModel(model);
		return result;
	}
}
