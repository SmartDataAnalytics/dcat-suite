package org.aksw.ckan_deploy.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.CkanClient;

/**
 * Read CkanDataset specifications from an RDF model
 *
 * @author raven Apr 2, 2018
 *
 */
public class CkanRdfDatasetProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CkanRdfDatasetProcessor.class);


    protected CkanClient ckanClient;

    public CkanRdfDatasetProcessor(CkanClient ckanClient) {
        super();
        this.ckanClient = ckanClient;
    }


}
