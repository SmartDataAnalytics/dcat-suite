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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(DcatCkanDeployUtils.class);


	
	public static Model deploy(CkanClient ckanClient, Model dcatModel, IRIResolver iriResolver, boolean noFileUpload) {
		// List dataset descriptions
		Model result = DcatUtils.createModelWithDcatFragment(dcatModel);
		Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(result);
		
		for(DcatDataset d : dcatDatasets) {
			logger.info("Deploying dataset " + d.getTitle());
			deploy(ckanClient, d, iriResolver, noFileUpload);
		}
		
		return result;
	}

		
	public static void deploy(CkanClient ckanClient, DcatDataset dataset, IRIResolver iriResolver, boolean noFileUpload) {
		String datasetName = dataset.getName();
		CkanDataset remoteCkanDataset;
		
		boolean isDatasetCreationRequired = false;
		try {
			remoteCkanDataset = ckanClient.getDataset(datasetName);
		} catch(CkanNotFoundException e) {
			logger.info("Dataset does not yet exist");
			remoteCkanDataset = new CkanDataset();
			isDatasetCreationRequired = true;
		} catch(CkanException e) {
			// TODO Maybe the dataset was deleted
			remoteCkanDataset = new CkanDataset();
			isDatasetCreationRequired = true;
		}

//		System.out.println("Before: " + remoteCkanDataset);

		// Update existing attributes with non-null values
		//dataset.getName(datasetId);
		DcatCkanRdfUtils.convertToCkan(remoteCkanDataset, dataset);
		

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
				
//		System.out.println("After: " + remoteCkanDataset);
		
		if(isDatasetCreationRequired) {
			remoteCkanDataset = ckanClient.createDataset(remoteCkanDataset);
		} else {
			remoteCkanDataset = ckanClient.updateDataset(remoteCkanDataset);
		}
		
		for(DcatDistribution dcatDistribution : dataset.getDistributions()) {

			
			CkanResource remoteCkanResource = createOrUpdateResource(ckanClient, remoteCkanDataset, dataset, dcatDistribution);

			// Check if there is a graph in the dataset that matches the distribution
			String distributionName = dcatDistribution.getTitle();

			logger.info("Deploying distribution " + distributionName);

			Set<Resource> downloadUrls = dcatDistribution.getDownloadURLs();

			List<String> resolvedUrls = downloadUrls.stream()
					.filter(Resource::isURIResource)
					.map(Resource::getURI)
					.map(iriResolver::resolveToStringSilent)
					.collect(Collectors.toList());
			
			List<URI> resolvedValidUrls = resolvedUrls.stream()
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
			
			Optional<Path> pathReference = resolvedValidUrls.stream()
				.map(Paths::get)
				.filter(Files::exists)
				.findFirst();


			if(pathReference.isPresent()) {
				Path path = pathReference.get();

				//String filename = distributionName + ".nt";
				String filename = path.getFileName().toString();
				String probedContentType = null;
				try {
					probedContentType = Files.probeContentType(path);
				} catch (IOException e) {
					logger.warn("Failed to probe content type of " + path, e);
				}
				
				String contentType = Optional.ofNullable(probedContentType).orElse(ContentType.APPLICATION_OCTET_STREAM.toString());
				
				if(!noFileUpload) {
					logger.info("Uploading file " + path);
					CkanResource tmp = CkanClientUtils.uploadFile(
							ckanClient,
							remoteCkanDataset.getName(),
							remoteCkanResource.getId(),
							path.toString(),
							ContentType.create(contentType),
							filename);

					tmp.setOthers(remoteCkanResource.getOthers());
					remoteCkanResource = ckanClient.updateResource(tmp);
//					remoteCkanResource.setUrl(tmp.getUrl());
//					remoteCkanResource.setUrlType(tmp.getUrlType());

					//remoteCkanResource.set
					//remoteCkanResource = ckanClient.getResource(tmp.getId());
					// Run the metadata update again

					// This works, but retrieves the whole dataset on each resource, which we want to avoid
//					if(false) {
//						remoteCkanDataset = ckanClient.getDataset(remoteCkanDataset.getId());
//						remoteCkanResource = createOrUpdateResource(ckanClient, remoteCkanDataset, dataset, dcatDistribution);
//					}
					
					//DcatCkanRdfUtils.convertToCkan(remoteCkanResource, dcatDistribution);

					
					// FIXME upload currently destroys custom tags, hence we update the metadata again
					//remoteCkanResource = ckanClient.updateResource(remoteCkanResource);
					
					
				} else {
					logger.info("File upload disabled. Skipping " + path);
				}
			}
		
			Resource newDownloadUrl = ResourceFactory.createResource(remoteCkanResource.getUrl());
		
			org.aksw.jena_sparql_api.utils.model.ResourceUtils.setProperty(dcatDistribution, DCAT.downloadURL, newDownloadUrl);
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
		DcatCkanRdfUtils.convertToCkan(remote, res);

		if (isResourceCreationRequired) {
			remote = ckanClient.createResource(remote);
		} else {
			remote = ckanClient.updateResource(remote);
		}

		return remote;
	}
	
}
