package org.aksw.dcat.repo.api;

import org.aksw.dcat.jena.domain.api.DcatDataset;

import io.reactivex.Flowable;

/**
 * Resolve distributions in regard to a dataset
 * 
 * @author Claus Stadler, Nov 21, 2018
 *
 */
public interface DatasetResolver {
	CatalogResolver getCatalogResolver();
	DcatDataset getDataset();
	
	// There may be multiple datasets containing the same distribution id
	// This should not happen, but we allow enumeration of these cases - hence, the result is a flowable
	Flowable<DistributionResolver> resolveDistribution(String distributionId) throws Exception;

//	Flowable<DistributionResolver> resolveDistribution(
//			Resource dcatDistribution,
//			Function<String, String> iriResolver) throws Exception;
}