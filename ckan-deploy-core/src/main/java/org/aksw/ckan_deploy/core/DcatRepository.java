package org.aksw.ckan_deploy.core;

import java.net.URI;
import java.util.Collection;
import java.util.function.Function;

import org.apache.jena.rdf.model.Resource;

public interface DcatRepository {
	
	/**
	 * 
	 * @param dcatDistribution An RDF description of a dcat distribution
	 * @param iriResolver A resolver for relative IRIs in case the downloadURLs of the distribution are relative
	 * @return
	 * @throws Exception
	 */
	Collection<URI> resolveDistribution(Resource dcatDistribution, Function<String, String> iriResolver) throws Exception;
}
