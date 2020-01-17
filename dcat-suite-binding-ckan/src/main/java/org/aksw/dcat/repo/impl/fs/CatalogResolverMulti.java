package org.aksw.dcat.repo.impl.fs;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

public class CatalogResolverMulti
	implements CatalogResolver
{
	protected Collection<CatalogResolver> resolvers;

	public CatalogResolverMulti() {
		this(new LinkedHashSet<>());
	}

	public CatalogResolverMulti(Collection<CatalogResolver> resolvers) {
		super();
		this.resolvers = resolvers;
	}
	
//	public addResolver(CatalogResolver resolver) {
//		this.resolvers.add(resolver);
//	}
	
	public Collection<CatalogResolver> getResolvers() {
		return resolvers;
	}
	
	// TODO The implementations are hacky:
	// In general, the first resolver returning a non-empty flow should be used

	@Override
	public Maybe<DatasetResolver> resolveDataset(String datasetId) {
		return Flowable.fromIterable(resolvers)
			.flatMap(resolver -> resolver.resolveDataset(datasetId).toFlowable())
			.firstElement()
			;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) {
		return Flowable.fromIterable(resolvers)
				.flatMap(resolver -> resolver.resolveDistribution(distributionId));
	}

	@Override
	public Maybe<URL> resolveDownload(String downloadUri) throws Exception {
		return Flowable.fromIterable(resolvers)
				.flatMap(resolver -> resolver.resolveDownload(downloadUri).toFlowable())
				.firstElement();
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
		return Flowable.fromIterable(resolvers)
				.flatMap(resolver -> resolver.resolveDistribution(datasetId, distributionId));
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, String distributionId) {
		return Flowable.fromIterable(resolvers)
				.flatMap(resolver -> resolver.resolveDistribution(dataset, distributionId));
	}

}
