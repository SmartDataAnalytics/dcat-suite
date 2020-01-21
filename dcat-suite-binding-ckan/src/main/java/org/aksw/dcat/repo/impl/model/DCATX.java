package org.aksw.dcat.repo.impl.model;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCAT;

public class DCATX {
	/**
	 * Extension to allow classification of download urls using dcat:DownloadURL
	 * 
	 */
	public static final Resource DownloadURL = ResourceFactory.createResource(DCAT.NS + "DownloadURL");
}
