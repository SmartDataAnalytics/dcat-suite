package org.aksw.dcat_suite.cli.main;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;

/**
 * A distribution based on a conjure job instance
 * 
 * TODO Not sure if this should inherit from DcatDistribution
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface DcatDistributionFromJobInstance
	extends DcatDistribution
{
	@IriNs("rpif")
	JobInstance getJobInstance();
	DcatDistributionFromJobInstance setJobInstance(JobInstance jobInstance); 
}
