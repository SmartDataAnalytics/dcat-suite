package org.aksw.dcat.repo.impl.cache;

import java.io.InputStream;
import java.nio.file.Path;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;

public class DistributionResolverCaching
	implements DistributionResolver
{
	protected CatalogResolverCaching cr;
	protected DistributionResolver dr;
	
	public DistributionResolverCaching(CatalogResolverCaching cr, DistributionResolver dr) {
		super();
		this.cr = cr;
		this.dr = dr;
	}

	@Override
	public DatasetResolver getDatasetResolver() {
		return dr.getDatasetResolver();
	}

	@Override
	public DcatDistribution getDistribution() {
		return dr.getDistribution();
	}

	@Override
	public InputStream open() throws Exception {
		return null;
	}

	@Override
	public InputStream open(String url) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Path getPath() {
		return null;
	}
}
