package org.aksw.ckan_deploy.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import eu.trentorise.opendata.jackan.model.CkanTag;


public class DcatCkanRdfUtils {
	private static final Logger logger = LoggerFactory.getLogger(DcatCkanRdfUtils.class);

	/**
	 * TODO Read out extra:uri fields and such
	 * 
	 * @param dcatDataset
	 * @return
	 */
	public static DcatDataset assignDefaultIris(DcatDataset dcatDataset) {
		return dcatDataset;
	}
	
	
	@SuppressWarnings("unlikely-arg-type")
	public static DcatDataset assignFallbackIris(DcatDataset dcatDataset, String baseIri) {
		// NOTE Create a copy to avoid concurrent modification
		for(DcatDistribution dcatDistribution : new ArrayList<>(dcatDataset.getDistributions())) {
			String iri = generateFallbackIri(dcatDistribution, baseIri);
		
			// Avoid cryptic errors about resources not found for badly modeled data
			if(!dcatDistribution.equals(dcatDataset)) {
				ResourceUtils.renameResource(dcatDistribution, iri);
			}
		}
		
		
		DcatDataset result;
		if(dcatDataset.isAnon()) {
			String iri = generateFallbackIri(dcatDataset, baseIri);
			result = ResourceUtils.renameResource(dcatDataset, iri).as(DcatDataset.class);
		} else {
			result = dcatDataset;
		}
		
		return result;
	}
	
	/**
	 * Default strategy to assign URIs under a given prefix.
	 * 
	 * TODO: Make use of extra:uri field if present
	 * 
	 * dataset IRI pattern: baseIri-dataset-${dct:identifier}
	 * resource IRI pattern: baseIri-distribution-${dct:title} 
	 * 
	 * @param dcatDataset
	 * @param baseIri
	 * @return
	 */
	public static String generateFallbackIri(DcatDataset dcatDataset, String baseIri) {
		String result = Optional.ofNullable(dcatDataset.getName())
				.map(id -> baseIri + "dataset/" + StringUtils.urlEncode(id))
				.orElseThrow(() -> new RuntimeException("Cannot generate a IRI for a dataset without a local identifier"));
		
		return result;
	}
	
	public static String generateFallbackIri(DcatDistribution dcatDistribution, String baseIri) {
		String result = Optional.ofNullable(dcatDistribution.getName())
				.map(id -> baseIri + "distribution/" + StringUtils.urlEncode(id))
				.orElseThrow(() -> new RuntimeException("Cannot generate a IRI for a dataset without a local identifier"));

		return result;
	}
	
	
	/**
	 * This will create blank node resources for representing the ckan datsaet.
	 * You can use assignUris() for the default strategy to get rid of blank nodes - or use your own.
	 * 
	 * @param ckanDataset
	 * @return
	 */
	public static DcatDataset convertToDcat(CkanDataset ckanDataset) {
		Model model = ModelFactory.createDefaultModel();
		DcatDataset result = convertToDcat(model, ckanDataset);
		return result;
	}
	
	public static DcatDataset convertToDcat(Model model, CkanDataset ckanDataset) {
		DcatDataset dcatDataset = model.createResource().as(DcatDataset.class);
		convertToDcat(dcatDataset, ckanDataset);
		
		for(CkanResource ckanResource : ckanDataset.getResources()) {
			DcatDistribution dcatDistribution = model.createResource().as(DcatDistribution.class);
			dcatDataset.getDistributions().add(dcatDistribution);
			
			convertToDcat(dcatDistribution, ckanResource);
		}
		

		return dcatDataset;
	}

	public static void convertToDcat(DcatDataset dcatDataset, CkanDataset ckanDataset) {
	
		dcatDataset.addProperty(RDF.type, DCAT.Dataset);
	
		dcatDataset.setName(ckanDataset.getName());
		dcatDataset.setTitle(ckanDataset.getTitle());
		dcatDataset.setDescription(ckanDataset.getNotes());	
		
		for(CkanTag ckanTag : Optional.ofNullable(ckanDataset.getTags()).orElse(Collections.emptyList())) {
			String tagName = ckanTag.getName();
			dcatDataset.getKeywords().add(tagName);

			Optional.ofNullable(ckanTag.getVocabularyId()).ifPresent(vocabId -> {
				logger.warn("Tag had a vocabulary id which is not exported " + tagName + " " + vocabId);
			});
		}
	}
	
	public static void convertToDcat(DcatDistribution dcatDistribution, CkanResource ckanResource) {

		dcatDistribution.addProperty(RDF.type, DCAT.Distribution);

		dcatDistribution.setName(ckanResource.getName());
		dcatDistribution.setDescription(ckanResource.getDescription());		
		
		Optional.ofNullable(ckanResource.getUrl()).ifPresent(url ->
			dcatDistribution.setDownloadURL(dcatDistribution.getModel().createResource(url)));
	}
	
}
