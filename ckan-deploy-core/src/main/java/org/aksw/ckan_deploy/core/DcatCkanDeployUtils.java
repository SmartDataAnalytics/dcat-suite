package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.vocabulary.DCAT;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.exceptions.CkanException;
import eu.trentorise.opendata.jackan.exceptions.CkanNotFoundException;
import eu.trentorise.opendata.jackan.internal.org.apache.http.entity.ContentType;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import eu.trentorise.opendata.jackan.model.CkanTag;

public class DcatCkanDeployUtils {


	
	public static Model deploy(CkanClient ckanClient, Model dcatModel, IRIResolver iriResolver) {
		// List dataset descriptions
		Model result = DcatUtils.createModelWithDcatFragment(dcatModel);
		Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(result);
		
		for(DcatDataset d : dcatDatasets) {
			System.out.println("Processing: " + d.getTitle());
			deploy(ckanClient, d, iriResolver);
		}
		
		return result;
	}

		
	public static void deploy(CkanClient ckanClient, DcatDataset dataset, IRIResolver iriResolver) {
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

		// Append tags
		// TODO Add switch whether to overwrite instead of append
		boolean replaceTags = false; // true = appendTags

		Optional<List<CkanTag>> existingTags = Optional.ofNullable(remoteCkanDataset.getTags());		
		
		Optional<List<CkanTag>> newTags;
		if(replaceTags) {
			newTags = Optional.of(dataset.getKeywords().stream().map(CkanTag::new).collect(Collectors.toList()));
		} else {
			// Index existing tags by name
			Map<String, CkanTag> nameToTag = existingTags.orElse(Collections.emptyList()).stream()
					.filter(tag -> tag.getVocabularyId() == null)
					.collect(Collectors.toMap(CkanTag::getName, x -> x));

			// Allocate new ckan tags objects for non-covered keywords
			List<CkanTag> addedTags = dataset.getKeywords().stream()
					.filter(keyword -> !nameToTag.containsKey(keyword))
					.map(CkanTag::new)
					.collect(Collectors.toList());
			
			// If there was no change, leave the original value (whether null or empty list)
			// Otherwise, reuse the existing tag list or allocate a new one
			newTags = addedTags.isEmpty()
					? existingTags
					: Optional.of(existingTags.orElse(new ArrayList<>()));
			
			// If there were changes, append the added tags
			if(newTags.isPresent()) {
				newTags.get().addAll(addedTags);
			}
		}

		newTags.ifPresent(remoteCkanDataset::setTags);
				
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

			List<String> resolvedAccessURLs = accessURLs.stream()
					.filter(Resource::isURIResource)
					.map(Resource::getURI)
					.map(iriResolver::resolveToStringSilent)
					.collect(Collectors.toList());
			
			List<URI> resolvedValidAccessURLs = resolvedAccessURLs.stream()
					.map(str -> {
						URI r = null;
						try {
							r = new URI(str);
						} catch (URISyntaxException e) {
							// Ignore
						}
						return r;
					})
					.filter(r -> r != null)
					.collect(Collectors.toList());
			
			Optional<Path> pathReference = resolvedValidAccessURLs.stream()
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
