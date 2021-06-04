package org.aksw.dcat_suite.app;

import java.io.IOException;

import org.aksw.dcat_suite.enrich.GTFSModel;


public class DCATProvider {

	//public void processEnrichGTFSWeb (String gtfsFile, String dsTitle, String prefix) {
	public GTFSModel processEnrichGTFSWeb (String gtfsFile, String dsTitle, String prefix, String downloadURL) throws IOException {
		GTFSModel gtfsModel = new GTFSModel(gtfsFile, dsTitle, prefix, downloadURL); 
		gtfsModel.enrichFromFeedInfo();
		return gtfsModel;
	}
	
	public GTFSModel processEnrichGTFSWeb (String gtfsFile, String dsTitle, String prefix) throws IOException {
		GTFSModel gtfsModel = new GTFSModel(gtfsFile, dsTitle, prefix); 
		gtfsModel.enrichFromFeedInfo();
		return gtfsModel;
	}
	
}
