package org.aksw.dcat.ap.playground.main;

import org.aksw.dcat.ap.binding.ckan.domain.impl.DcatApCkanDatsetViewImpl;

public class CkanResourceFactory {
	DcatApCkanDatsetViewImpl createDataset() {
		return new DcatApCkanDatsetViewImpl(null, null);
	}
	
	
	//CkanDistribution createDistribution();
}
