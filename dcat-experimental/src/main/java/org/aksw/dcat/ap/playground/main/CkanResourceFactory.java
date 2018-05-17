package org.aksw.dcat.ap.playground.main;

import org.aksw.dcat.ap.trash.DcatApCkanDatsetViewImpl;

public class CkanResourceFactory {
	DcatApCkanDatsetViewImpl createDataset() {
		return new DcatApCkanDatsetViewImpl(null, null);
	}
	
	
	//CkanDistribution createDistribution();
}
