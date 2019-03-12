package org.aksw.dcat.repo.impl.core;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;

import io.reactivex.Flowable;

public class DatasetResolverImpl
	implements DatasetResolver
{
	protected CatalogResolver catalogResolver;
	protected DcatDataset dcatDataset;

	public DatasetResolverImpl(CatalogResolver catalogResolver, DcatDataset dcatDataset) {
		this.catalogResolver = catalogResolver;
		this.dcatDataset = dcatDataset;
	}
	
	
	@Override
	public DcatDataset getDataset() {
		return dcatDataset;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) throws Exception {
		return catalogResolver.resolveDistribution(dcatDataset, distributionId);
	}



	@Override
	public CatalogResolver getCatalogResolver() {
		return catalogResolver;
	}
}