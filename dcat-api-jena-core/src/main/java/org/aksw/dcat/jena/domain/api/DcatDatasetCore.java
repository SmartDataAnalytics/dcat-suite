package org.aksw.dcat.jena.domain.api;

import java.util.Set;

public interface DcatDatasetCore
{	
	Set<DcatDistribution> getDistributions();
	Set<String> getKeywords();
	
	
//	FoafAgent getPublisher();
//	VCardKind getContactPoint();
	
}
