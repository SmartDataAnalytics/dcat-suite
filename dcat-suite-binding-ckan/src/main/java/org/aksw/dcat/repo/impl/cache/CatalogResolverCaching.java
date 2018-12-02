package org.aksw.dcat.repo.impl.cache;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.dcat.repo.impl.fs.CatalogResolverFilesystem;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

public class CatalogResolverCaching
	implements CatalogResolver
{
	// TODO Replace filesystem with a more general interface
	protected CatalogResolverFilesystem cache;
	protected CatalogResolver backend;
		
	public CatalogResolverCaching(CatalogResolverFilesystem cache, CatalogResolver backend) {
		super();
		this.cache = cache;
		this.backend = backend;
	}

	@Override
	public Maybe<DatasetResolver> resolveDataset(String datasetId) {
		return cache.resolveDataset(datasetId).switchIfEmpty(
				backend.resolveDataset(datasetId).map(dr -> cache.doCacheDataset(datasetId, dr))
		);
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) {
		return cache.resolveDistribution(distributionId).switchIfEmpty(
				backend.resolveDistribution(distributionId).map(distR -> cache.doCacheDistribution(distributionId, distR))
		);
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, String distributionId) {
		return null;
	}	
}