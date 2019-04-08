package org.aksw.dcat.repo.api;

import java.net.URL;

import org.aksw.dcat.jena.domain.api.DcatDataset;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * A catalog resolver can resolve
 * - datasets
 * - distributions
 * 
 * @author Claus Stadler, Nov 22, 2018
 *
 */
public interface CatalogResolver {
	Maybe<DatasetResolver> resolveDataset(String datasetId);
	Flowable<DistributionResolver> resolveDistribution(String distributionId);

	Maybe<URL> resolveDownload(String downloadUri) throws Exception;

	/**
	 * Resolve a distributionId in regard to a given dataset.
	 * This method can be used to disambiguate a distribution should for some reason the
	 * case arise that the same distribution identifier is used with different datasets.
	 * 
	 * 
	 * @param datasetId
	 * @param distributionId
	 * @return
	 */
	Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId);


	// TODO This should be part of an internal interface
	Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, String distributionId);
}