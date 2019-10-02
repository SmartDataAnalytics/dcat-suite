package org.aksw.dcat.server.config;

import java.io.IOException;

import org.aksw.dcat_suite.server.conneg.torename.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigDatasetRepository {
	@Bean
	public HttpResourceRepositoryFromFileSystem datasetRepository() throws IOException {
		HttpResourceRepositoryFromFileSystem result = HttpResourceRepositoryFromFileSystemImpl.createDefault();
		return result;
	}
}
