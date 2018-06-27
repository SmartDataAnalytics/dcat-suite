package org.aksw.dcat.jena.domain.api;

import java.util.Collection;

public interface DcatDatasetCore
{	
	/** Factory method for distributions - does not add them */
	DcatDistributionCore createDistribution();

	Collection<? extends DcatDistributionCore> getDistributions();
	Collection<String> getKeywords();
	
	
//	FoafAgent getPublisher();
//	VCardKind getContactPoint();
}
