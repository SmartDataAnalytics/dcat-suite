package org.aksw.dcat.repo.impl.ckan;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;

public class DistributionResolverCkan
	implements DistributionResolver
{
	protected DatasetResolver datasetResolver;
	//protected DcatDataset dcatDataset;
	
	// Should be a resource mentioned in the dataset
	protected DcatDistribution dcatDistribution;

	public DistributionResolverCkan(DatasetResolver datasetResolver, DcatDistribution dcatDistribution) {
		//this.dcatDataset = dcatDataset;
		this.datasetResolver = datasetResolver;
		this.dcatDistribution = dcatDistribution;
	}
	
	@Override
	public DatasetResolver getDatasetResolver() {
		return null;
	}

	@Override
	public DcatDistribution getDistribution() {
		return dcatDistribution;
	}

	@Override
	public InputStream open() throws Exception {
		return open(dcatDistribution.getDownloadURL());
	}


	@Override
	public InputStream open(String url) throws Exception {
		boolean isValid = dcatDistribution.getDownloadURLs().contains(url);
		InputStream result;
		if(isValid) {
			result = new URI(url).toURL().openStream();
		} else {
			throw new RuntimeException("Given url is not declared to be a download URL of the distribution: " + url);
		}
		
		return result;
	}

	@Override
	public Path getPath() {
		return null;
	}
}