package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.DcatEntity;
import org.aksw.dcat.utils.DcatUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.writer.NTriplesWriter;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCAT;

public class DcatExpandUtils {
	
	public static Model export(org.apache.jena.query.Dataset sparqlDataset, Path targetFolder) throws IOException {
		Collection<DcatDataset> rawDcatDatasets = DcatUtils.listDcatDatasets(sparqlDataset);
		
		// Create a (mutable) model with the dcat information only
		// (using the closure of dcat dataset resources)
		Model dcatModel = ModelFactory.createDefaultModel();
		rawDcatDatasets.stream()
			.map(ResourceUtils::reachableClosure)
			.forEach(dcatModel::add);
	
		
		Collection<DcatDataset> dcatDatasets = rawDcatDatasets.stream()
				.map(r -> r.inModel(dcatModel).as(DcatDataset.class))
				.collect(Collectors.toList());
		
		// Running export will change the dcat:accessURL to point to the exported filenames
		for(DcatDataset dcatDataset : dcatDatasets) {
			exportDcatDataset(sparqlDataset, dcatDataset, targetFolder);
		}
		
		return dcatModel;
	}

	
//	public static void writeModel(Model model, Pathpath) {
//		//Path dcatFile = targetFolder.resolve("dcat.nt");
//		try(OutputStream out = Files.newOutputStream(dcatFile)) {
//			RDFDataMgr.write(out, dcatModel, RDFFormat.NTRIPLES);
//			out.flush();
//		}
//	}
	
	
	public static String deriveName(DcatEntity dcatEntity) {
		String result = Optional.ofNullable(dcatEntity.getTitle())
				.orElse(dcatEntity.isURIResource()
						? dcatEntity.getLocalName()
						: "" + dcatEntity);
		
		return result;
	}
	
	public static void exportDcatDataset(org.apache.jena.query.Dataset sparqlDataset, DcatDataset dcatDataset, Path targetFolder) throws IOException {
		String datasetName = deriveName(dcatDataset);
		String datasetFilename = StringUtils.urlEncode(datasetName);
		
		Path relativeDatasetFolder = Paths.get(datasetFilename);//targetFolder.resolve(datasetFilename);
		Path datasetFolder = targetFolder.resolve(relativeDatasetFolder);
		Files.createDirectories(datasetFolder);
		
		for(DcatDistribution dcatDistribution : dcatDataset.getDistributions()) {
			exportDcatDestribution(sparqlDataset, targetFolder, relativeDatasetFolder, dcatDistribution);
		}
	}
	
	public static void writeSortedNtriples(Model model, Path outputFile) throws IOException {
		try(QueryExecution qe = QueryExecutionFactory.create("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ORDER BY ?s ?p ?o", model)) {
			Iterator<Triple> it = qe.execConstructTriples();
			
			try(OutputStream out = Files.newOutputStream(outputFile)) {
				NTriplesWriter.write(out, it);
//					for(Model model : uriToModel.values()) {
//						RDFDataMgr.write(out, model, RDFFormat.NTRIPLES);
//						//NTriplesWriter.
//					}
				out.flush();					
			}
		}
	}
	
	
	
	public static void exportDcatDestribution(org.apache.jena.query.Dataset sparqlDataset, Path targetFolder, Path relativeDatasetFolder, DcatDistribution dcatDistribution) throws IOException {
		String distributionName = Optional.ofNullable(dcatDistribution.getTitle())
				.orElse(dcatDistribution.isURIResource()
						? dcatDistribution.getLocalName()
						: "" + dcatDistribution);
		
			
		// If there is at least one accessURL that matches with a graph
		// require all other accessURLs to point to graphs as well and export the union graph
		Optional<? extends Collection<Resource>> possibleGraphAccessURLs = DcatUtils.tryGetGraphAccessURLs(dcatDistribution, sparqlDataset::containsNamedModel);
		
		if(possibleGraphAccessURLs.isPresent()) {
			Collection<Resource> graphAccessURLs = possibleGraphAccessURLs.get();

			Map<String, Model> uriToModel = graphAccessURLs.stream()
					.map(Resource::getURI)
					.collect(Collectors.toMap(uri -> uri, sparqlDataset::getNamedModel));

			String distributionFileName = StringUtils.urlEncode(distributionName) + ".nt";
			Path relativeDistributionPath = relativeDatasetFolder.resolve(distributionFileName);
			Path distributionPath = targetFolder.resolve(relativeDistributionPath);
			
			Model combinedModel;
			if(uriToModel.size() > 1) {
				combinedModel = ModelFactory.createDefaultModel();
				uriToModel.values().forEach(combinedModel::add);
			} else {
				combinedModel = uriToModel.values().iterator().next();
			}

			writeSortedNtriples(combinedModel, distributionPath);
			
			// Make the accessURL point to the newly created file
			Resource newDownloadUrl = ResourceFactory.createResource(relativeDistributionPath.toString());
			org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.setProperty(dcatDistribution, DCAT.downloadURL, newDownloadUrl);
		
			// Remove the prior accessURLs
			graphAccessURLs.forEach(o -> dcatDistribution.getModel().remove(dcatDistribution, DCAT.accessURL, o));
		}
	}
	
}
