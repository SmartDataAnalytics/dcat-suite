package org.aksw.dcat.repo.impl.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCAT;

public class DCATX {
	/**
	 * Extension to allow classification of download urls using dcat:DownloadURL
	 * 
	 */
	public static final Resource DownloadURL = ResourceFactory.createResource(Strs.DownloadURL);
//	public static final Property isLatestVersion = ResourceFactory.createProperty(Strs.isLatestVersion);
	public static final Property relatedDataset = ResourceFactory.createProperty(Strs.relatedDataset);
	public static final Property versionTag = ResourceFactory.createProperty(Strs.versionTag);


	public static class Strs {
		public static final String DownloadURL = DCAT.NS + "DownloadURL";
		//public static final String isLatestVersion = DCAT.NS + "isLatestVersion";
		public static final String relatedDataset = DCAT.NS + "relatedDataset";		
		public static final String versionTag = DCAT.NS + "versionTag";
	}
}
