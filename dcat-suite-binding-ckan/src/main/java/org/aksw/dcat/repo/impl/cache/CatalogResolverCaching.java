package org.aksw.dcat.repo.impl.cache;

import java.net.URL;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.dcat.repo.impl.core.DatasetResolverImpl;
import org.aksw.dcat.repo.impl.core.DistributionResolverImpl;
import org.aksw.dcat.repo.impl.fs.CatalogResolverCacheCapable;
import org.apache.jena.rdf.model.Resource;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

public class CatalogResolverCaching
	implements CatalogResolver
{
	protected CatalogResolverCacheCapable cache;
	protected CatalogResolver backend;

	public CatalogResolverCaching(CatalogResolverCacheCapable cache, CatalogResolver backend) {
		super();
		this.cache = cache;
		this.backend = backend;
	}

	@Override
	public Flowable<Resource> search(String pattern) {
		return backend.search(pattern);
	}
	
	@Override
	public Maybe<DatasetResolver> resolveDataset(String datasetId) {
		return cache.resolveDataset(datasetId).switchIfEmpty(
				backend.resolveDataset(datasetId)
					.map(dr -> cache.doCacheDataset(datasetId, dr)))
				.map(dr -> new DatasetResolverImpl(this, dr.getDataset()));
	}

	
//	@Override
//	public Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, DcatDistribution distribution) {
//		return cache.resolveDistribution(distributionId).switchIfEmpty(
//				backend.resolveDistribution(distributionId)
//					.map(distR -> cache.doCacheDistribution(distributionId, distR)))
//			.map(distR -> new DistributionResolverImpl(new DatasetResolverImpl(this, distR.getDatasetResolver().getDataset()), distR.getDistribution()));	
//	}
//
	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) {
		return cache.resolveDistribution(distributionId).switchIfEmpty(
				backend.resolveDistribution(distributionId)
					.map(distR -> cache.doCacheDistribution(distributionId, distR)))
			.map(distR -> new DistributionResolverImpl(new DatasetResolverImpl(this, distR.getDatasetResolver().getDataset()), distR.getDistribution()));	
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
		return cache.resolveDistribution(datasetId, distributionId).switchIfEmpty(
				backend.resolveDistribution(datasetId, distributionId)
					.map(distR -> cache.doCacheDistribution(distributionId, distR)))
			.map(distR -> new DistributionResolverImpl(new DatasetResolverImpl(this, distR.getDatasetResolver().getDataset()), distR.getDistribution()));	
	}

	
	@Override
	public Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, String distributionId) {
		return cache.resolveDistribution(dataset, distributionId).switchIfEmpty(
				backend.resolveDistribution(dataset, distributionId)
					.map(distR -> cache.doCacheDistribution(distributionId, distR)))
			.map(distR -> new DistributionResolverImpl(new DatasetResolverImpl(this, distR.getDatasetResolver().getDataset()), distR.getDistribution()));
	}

	@Override
	public Maybe<URL> resolveDownload(String downloadUri) throws Exception {
		return cache.resolveDownload(downloadUri).toFlowable().switchIfEmpty(
				backend.resolveDownload(downloadUri).toFlowable()
					.flatMap(url -> cache.doCacheDownload(url).toFlowable()))
				.firstElement();
		
	}
}
