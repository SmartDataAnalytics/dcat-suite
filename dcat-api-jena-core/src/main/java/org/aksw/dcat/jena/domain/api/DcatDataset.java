package org.aksw.dcat.jena.domain.api;

import java.util.Set;

public interface DcatDataset
	extends DcatEntity
{	
	Set<DcatDistribution> getDistributions();
	Set<String> getKeywords();
}
