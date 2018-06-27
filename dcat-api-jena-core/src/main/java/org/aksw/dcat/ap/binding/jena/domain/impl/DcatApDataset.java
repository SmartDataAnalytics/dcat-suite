package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.util.Collection;

import org.aksw.dcat.ap.domain.api.DcatApDatasetCore;
import org.aksw.dcat.jena.domain.api.DcatDataset;

public interface DcatApDataset
	extends DcatDataset, DcatApDatasetCore
{
	DcatApDistribution createDistribution();

	default Collection<? extends DcatApDistribution> getDistributions() {
		return getDistributions(DcatApDistribution.class);
	}
}
