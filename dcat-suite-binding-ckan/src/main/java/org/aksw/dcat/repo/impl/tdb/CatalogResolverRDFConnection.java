package org.aksw.dcat.repo.impl.tdb;

import java.net.URL;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.apache.jena.rdfconnection.RDFConnection;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

public class CatalogResolverRDFConnection
	implements CatalogResolver
{
	protected RDFConnection conn;
	
	@Override
	public Maybe<DatasetResolver> resolveDataset(String datasetId) {
		//DataQuery
		//"SELECT ?s { ?s dcat:distribution ?d }
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Maybe<URL> resolveDownload(String downloadUri) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, String distributionId) {
		// TODO Auto-generated method stub
		return null;
	}

}
