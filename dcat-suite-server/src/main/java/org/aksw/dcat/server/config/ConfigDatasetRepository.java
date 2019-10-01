package org.aksw.dcat.server.config;

import java.io.IOException;

import org.aksw.dcat_suite.server.conneg.torename.HttpResourceRepositoryFromFileSystem;
import org.aksw.dcat_suite.server.conneg.torename.HttpResourceRepositoryManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigDatasetRepository {
	@Bean
	public HttpResourceRepositoryFromFileSystem datasetRepository() throws IOException {
		HttpResourceRepositoryFromFileSystem result = HttpResourceRepositoryManagerImpl.createDefault();
		return result;
	}
}
