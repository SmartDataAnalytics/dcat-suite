package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCAT;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.exceptions.CkanException;
import eu.trentorise.opendata.jackan.exceptions.CkanNotFoundException;
import eu.trentorise.opendata.jackan.internal.org.apache.http.entity.ContentType;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;

public class DcatCkanDeployUtils {

	public static Model deploy(CkanClient ckanClient, Model dcatModel) {
		// List dataset descriptions
		Model result = DcatUtils.createModelWithDcatFragment(dcatModel);
		Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(result);
		
		for(DcatDataset d : dcatDatasets) {
			System.out.println("Processing: " + d.getTitle());
			deploy(ckanClient, d);
		}
		
		return result;
	}

	public static void deploy(CkanClient ckanClient, DcatDataset dataset) {
		String datasetName = dataset.getName();
		CkanDataset remoteCkanDataset;
		
		boolean isDatasetCreationRequired = false;
		try {
			remoteCkanDataset = ckanClient.getDataset(datasetName);
		} catch(CkanNotFoundException e) {
			System.out.println("Dataset does not yet exist");
			remoteCkanDataset = new CkanDataset();
			isDatasetCreationRequired = true;
		} catch(CkanException e) {
			// TODO Maybe the dataset was deleted
			remoteCkanDataset = new CkanDataset();
			isDatasetCreationRequired = true;
		}

		System.out.println("Before: " + remoteCkanDataset);

		// Update existing attributes with non-null values
		//dataset.getName(datasetId);
		Optional.ofNullable(dataset.getName()).ifPresent(remoteCkanDataset::setName);
		Optional.ofNullable(dataset.getTitle()).ifPresent(remoteCkanDataset::setTitle);
		Optional.ofNullable(dataset.getDescription()).ifPresent(remoteCkanDataset::setNotes);
		
		
		System.out.println("After: " + remoteCkanDataset);
		
		if(isDatasetCreationRequired) {
			remoteCkanDataset = ckanClient.createDataset(remoteCkanDataset);
		} else {
			remoteCkanDataset = ckanClient.updateDataset(remoteCkanDataset);
		}
		
		for(DcatDistribution dcatDistribution : dataset.getDistributions()) {

			CkanResource remoteCkanResource = createOrUpdateResource(ckanClient, remoteCkanDataset, dataset, dcatDistribution);

			// Check if there is a graph in the dataset that matches the distribution
			String distributionName = dcatDistribution.getTitle();
						
			Set<Resource> accessURLs = dcatDistribution.getAccessURLs();

			Optional<Path> pathReference = accessURLs.stream()
				.filter(Resource::isURIResource)
				.map(Resource::getURI)
				.map(Paths::get)
				.filter(Files::exists)
				.findFirst();


			if(pathReference.isPresent()) {
				Path path = pathReference.get();
			
				remoteCkanResource = CkanClientUtils.uploadFile(
						ckanClient,
						remoteCkanDataset.getName(),
						remoteCkanResource.getId(),
						path.toString(),
						ContentType.create("application/n-triples"),
						distributionName + ".nt");
			}
		
			Resource newAccessURL = ResourceFactory.createResource(remoteCkanResource.getUrl());
		
			org.aksw.jena_sparql_api.utils.model.ResourceUtils.setProperty(dcatDistribution, DCAT.accessURL, newAccessURL);
		}
	}
	
	/**
	 * Create or update the appropriate resource among the ones in a given dataset
	 * 
	 * @param ckanClient
	 * @param dataset
	 * @param res
	 * @throws IOException 
	 */
	public static CkanResource createOrUpdateResource(CkanClient ckanClient, CkanDataset ckanDataset, DcatDataset dataset, DcatDistribution res) {
		Multimap<String, CkanResource> nameToCkanResources = Multimaps.index(
				Optional.ofNullable(ckanDataset.getResources()).orElse(Collections.emptyList()),
				CkanResource::getName);

		// Resources are required to have an ID
		String resName = Optional.ofNullable(res.getTitle())
				.orElseThrow(() -> new RuntimeException("resource must have a name i.e. public id"));
		
		boolean isResourceCreationRequired = false;
		
		CkanResource remote = null;
		Collection<CkanResource> remotes = nameToCkanResources.get(resName);
		
		// If there are multiple resources with the same name,
		// update the first one and delete all others
		
		Iterator<CkanResource> it = remotes.iterator();
		remote = it.hasNext() ? it.next() : null;
		
		while(it.hasNext()) {
			CkanResource tmp = it.next();
			ckanClient.deleteResource(tmp.getId());
		}
		
		
		// TODO We need a file for the resource
		
		if(remote == null) {
			isResourceCreationRequired = true;
			
			remote = new CkanResource(null, ckanDataset.getName());
		}
		
		// Update existing attributes with non-null values
		Optional.ofNullable(res.getTitle()).ifPresent(remote::setName);
		//Optional.ofNullable(res.getTitle()).ifPresent(remote::setna);
		Optional.ofNullable(res.getDescription()).ifPresent(remote::setDescription);

		if(isResourceCreationRequired) {
			remote = ckanClient.createResource(remote);
		} else {
			remote = ckanClient.updateResource(remote);
		}		
		
		return remote;
	}
	
}
