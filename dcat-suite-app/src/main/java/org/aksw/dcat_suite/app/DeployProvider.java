package org.aksw.dcat_suite.app;

import java.io.IOException;

import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;

import eu.trentorise.opendata.jackan.CkanClient;

public class DeployProvider {
	
	
	public static void deploy(String ckanUrl, String ckanApiKey, String dcatSource, boolean noFileUpload, boolean mapByGroup, String organization) throws IOException {
		 CkanClient ckanClient = new CkanClient(ckanUrl, ckanApiKey);
		 MainCliDcatSuite.processDeploy(ckanClient, dcatSource, noFileUpload, mapByGroup, organization);
	}

}
