package org.aksw.dcat.repo.impl.cache;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;

import io.reactivex.Flowable;

public class DatasetResolverCaching
	implements DatasetResolver
{
	protected CatalogResolverCaching cr;
	protected DatasetResolver dr;

	@Override
	public CatalogResolver getCatalogResolver() {
		return cr;
	}

	@Override
	public DcatDataset getDataset() {
		return dr.getDataset();
	}

//	@Override
//	public Flowable<DistributionResolver> resolveDistributions(DcatDistribution distribution) throws Exception {
//		return cr.resolveDistributions(this, distribution);
//	}


	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) throws Exception {
		return cr.resolveDistribution(distributionId);
	}
}
