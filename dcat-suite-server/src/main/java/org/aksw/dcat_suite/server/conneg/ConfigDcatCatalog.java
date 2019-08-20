package org.aksw.dcat_suite.server.conneg;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.impl.core.CatalogResolverUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigDcatCatalog {
	@Bean
	public CatalogResolver catalogResolver() {
		CatalogResolver result = CatalogResolverUtils.createCatalogResolverDefault();
		return result;
	}
}
