package org.aksw.dcat.repo.api;

import java.util.Collection;
import java.util.List;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

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

	//Flowable<DistributionResolver> resolveDistribution(DcatDistribution distribution);
	//Flowable<DistributionResolver> resolveDistributions();
	
	
	default Flowable<DistributionResolver> resolveDistributions() {
		DcatDataset dcatDataset = getDataset();
		//List<RDFNode> list = dcatDataset.listProperties().mapWith(Statement::getObject).toList();
		Collection<? extends DcatDistribution> distributions = dcatDataset.getDistributions();
		
		Flowable<DistributionResolver> result = Flowable.fromIterable(distributions)
			.flatMap(dist -> resolveDistribution(dist.getURI()));
		
		return result;
	}
	
//	Flowable<DistributionResolver> resolveDistribution(
//			Resource dcatDistribution,
//			Function<String, String> iriResolver) throws Exception;
}