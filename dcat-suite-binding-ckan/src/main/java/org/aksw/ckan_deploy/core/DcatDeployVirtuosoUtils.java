package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.util.compress.MetaBZip2CompressorInputStream;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoBulkLoad;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DcatDeployVirtuosoUtils {

	private static final Logger logger = LoggerFactory.getLogger(DcatDeployVirtuosoUtils.class);

	
	/**
	 * This is a compound transformation doing:
	 * 
	 * TODO Create beans so the transformation parameters can be adjusted
	 * 
	 * - Apply mapping of geosparql to virtuoso
	 * - Filter long literals
	 * - Discard invalid geometries
	 * 
	 * @param triple
	 * @return
	 */
	public static Optional<Triple> transformForVirtuoso(Triple triple) {
		return null;
	}

	public static final Property dcatDefaultGraphGroup = ResourceFactory.createProperty(DCAT.NS + "defaultGraphGroup");
	public static final Property dcatDefaultGraph = ResourceFactory.createProperty(DCAT.NS + "defaultGraph");
	
	// Used by dataid
	public static final Property sdDefaultGraph = ResourceFactory.createProperty("http://www.w3.org/ns/sparql-service-description#defaultGraph");

	
	public static final Set<Property> defaultGraphGroupProperties = new LinkedHashSet<>(Arrays.asList(dcatDefaultGraphGroup));
	public static final Set<Property> defaultGraphProperties = new LinkedHashSet<>(Arrays.asList(dcatDefaultGraph, sdDefaultGraph));

		
	public static Optional<String> findUri(Resource r, Collection<Property> searchProperties) {
				
		Optional<String> result = searchProperties.stream().flatMap(p -> ResourceUtils.listPropertyValues(r, p).toSet().stream())
			.filter(RDFNode::isURIResource)
			.map(RDFNode::asResource)
			.map(Resource::getURI)
			.findFirst();		

		return result;
	}

	
	public static void deploy(
			DcatRepository dcatRepository,
			DcatDataset dcatDataset,
			Function<String, String> iriResolver,
			//IRIResolver iriResolver,
			Path allowedFolder,
			boolean noSymlink,
			Connection conn) throws SQLException, IOException, URISyntaxException {

		String datasetDefaultGraph = DcatCkanRdfUtils.getUri(dcatDataset, dcatDefaultGraph).orElse(null);
		
		String defaultGraphGroupIri = DcatCkanRdfUtils.getUri(dcatDataset, dcatDefaultGraphGroup).orElse(null);

		// If both are specified, the graph group takes precedence by default - the interpretation is:
		// If possible, create the group group, but should this be not supported, use the given graph instead
		
		// Issue what if dataset and distribution specify a defaultGraph?
		// Right now the dataset level default graph takes precedence
		
		if(defaultGraphGroupIri != null) {
			logger.info("Creating graph group <" + defaultGraphGroupIri + ">");
			VirtuosoBulkLoad.graphGroupCreate(conn, defaultGraphGroupIri, 1);

		}
		
		
		Set<Path> toDelete = new HashSet<>();
		try {
			for(DcatDistribution dcatDistribution : dcatDataset.getDistributions()) {

				// If there is a group group, but the dataset does not declare a default graph, it is skipped
				String distributionGraphIri = DcatCkanRdfUtils.getUri(dcatDistribution, dcatDefaultGraph).orElse(null);
				
				String effectiveGraphIri = defaultGraphGroupIri != null
						// If there is a graph group, we enforce the graph on the distribution
						? distributionGraphIri
						// Otherwise, use the dataset graph first, and fallback to the one on the distribution
						: Optional.ofNullable(datasetDefaultGraph).orElse(distributionGraphIri)
						;
				
				
				if(defaultGraphGroupIri != null && effectiveGraphIri != null) {
					logger.info("Adding graph <" + effectiveGraphIri + "> as member of graph group <" + defaultGraphGroupIri + ">");
					VirtuosoBulkLoad.graphGroupIns(conn, defaultGraphGroupIri, effectiveGraphIri);
				}
				
				if(effectiveGraphIri != null) {
					
					Collection<URI> dataUris;
					try {
						dataUris = dcatRepository.resolveDistribution(dcatDistribution, iriResolver);
					} catch(Exception e) {
						logger.warn("Error resolving distribution" + dcatDistribution, e);
						// TODO Require permissive=true flag to proceed
						continue;
					}

//					String downloadURL = ResourceUtils.getPropertyValue(dcatDistribution, DCAT.downloadURL)
//							.filter(RDFNode::isURIResource)
//							.map(RDFNode::asResource)
//							.map(Resource::getURI)
//							.orElse(null);
	
					for(URI dataUri : dataUris) {
					
						//String url = iriResolver.resolveToStringSilent(downloadURL);
						Path path = Paths.get(dataUri);
						
						
						String contentType = Files.probeContentType(path);
						
						logger.info("Content type " + contentType + " detected on " + path.toAbsolutePath());
						
						String filename = path.getFileName().toString();
						
						Path actualFile;
						if("application/x-bzip".equals(contentType)) {
							
	
							String unzippedFilename = com.google.common.io.Files.getNameWithoutExtension(filename);
								
							actualFile = allowedFolder.resolve(".tmp-load-" + unzippedFilename);
							
							if(!Files.exists(actualFile)) {
								Path tmpFile = allowedFolder.resolve(".tmp-unzip-" + unzippedFilename);
								Files.deleteIfExists(tmpFile);
								
								logger.info("bzip archive detected, unzipping to " + tmpFile.toAbsolutePath());
								
								try(InputStream in = new MetaBZip2CompressorInputStream(Files.newInputStream(path))) {
									Files.copy(in, tmpFile);
								}
								
								Files.move(tmpFile, actualFile);
							}
							
							toDelete.add(actualFile);
						} else {
							actualFile = allowedFolder.resolve(".tmp-load-" + filename);
	
							if(!Files.exists(actualFile)) {
								if(noSymlink) {
									Files.copy(actualFile, path);								
								} else {
									Files.createSymbolicLink(actualFile, path);
								}
							}
							
							toDelete.add(actualFile);
						}

						logger.info("Preparing Virtuoso Bulk load:\n  File: " + actualFile.toAbsolutePath() + "\n  Graph: " + effectiveGraphIri + "\n");

						//logger.info("Registering " + actualFile.toAbsolutePath() + " with Virtuoso RDF Loader");
						
						String allowedDir = allowedFolder.toString();
						String actualFilename = actualFile.getFileName().toString();
						
						VirtuosoBulkLoad.ldDir(conn, allowedDir, actualFilename, effectiveGraphIri);
					}
				}
			}
			
			logger.info("Virtuoso RDF Loader started ...");
			VirtuosoBulkLoad.rdfLoaderRun(conn);
			logger.info("Virtuoso RDF Loader finished.");
			//dcatDataset
		}
		finally {
			for(Path path : toDelete) {
				try {
					if(Files.exists(path)) {
						logger.info("Deleting file " + path.toAbsolutePath());
						Files.delete(path);
					}
				} catch(IOException e) {
					logger.warn("Failed to clean up file " + path.toAbsolutePath());
				}
			}
		}
	}
}
	