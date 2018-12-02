package org.aksw.dcat.repo.api;

import java.io.InputStream;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.rdf.model.Resource;

public interface DistributionResolver {
	DatasetResolver getDatasetResolver();
	DcatDistribution getDistribution();
	
	/**
	 * Open the distribution; throws an exception unless there is exactly 1 download url
	 * 
	 * @return
	 */
	InputStream open() throws Exception;
	InputStream open(String url) throws Exception;

	default InputStream open(Resource downloadUrl) throws Exception {
		InputStream result = open(downloadUrl.getURI());
		return result;
	}

}