package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
import org.apache.jena.riot.system.IRIResolver;
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

	public static final Property defaultGraphGroup = ResourceFactory.createProperty(DCAT.NS + "defaultGraphGroup");
	public static final Property defaultGraph = ResourceFactory.createProperty(DCAT.NS + "defaultGraph");
	

	
	public static void deploy(DcatDataset dcatDataset, IRIResolver iriResolver, Path allowedFolder, Connection conn) throws SQLException, IOException, URISyntaxException {

		String defaultGraphGroupIri = ResourceUtils.getPropertyValue(dcatDataset, defaultGraphGroup)
				.filter(RDFNode::isURIResource)
				.map(RDFNode::asResource)
				.map(Resource::getURI)
				.orElse(null);

		
		if(defaultGraphGroupIri != null) {
			logger.info("Creating graph group <" + defaultGraphGroupIri + ">");
			VirtuosoBulkLoad.graphGroupCreate(conn, defaultGraphGroupIri, 1);

		}
		
		
		Set<Path> toDelete = new HashSet<>();
		try {
			for(DcatDistribution dcatDistribution : dcatDataset.getDistributions()) {
				
				
				String graphIri = ResourceUtils.getPropertyValue(dcatDistribution, defaultGraph)
						.filter(RDFNode::isURIResource)
						.map(RDFNode::asResource)
						.map(Resource::getURI)
						.orElse(null);
				
				
				if(defaultGraphGroupIri != null && graphIri != null) {
					logger.info("Adding graph <" + graphIri + "> as member of graph group <" + defaultGraphGroupIri + ">");
					VirtuosoBulkLoad.graphGroupIns(conn, defaultGraphGroupIri, graphIri);
				}
				
				if(graphIri != null) {
					
					String downloadURL = ResourceUtils.getPropertyValue(dcatDistribution, DCAT.downloadURL)
							.filter(RDFNode::isURIResource)
							.map(RDFNode::asResource)
							.map(Resource::getURI)
							.orElse(null);
	
					if(downloadURL != null) {
					
						String url = iriResolver.resolveToStringSilent(downloadURL);
						Path path = Paths.get(new URI(url));
						
						
						String contentType = Files.probeContentType(path);
						
						logger.info("Content type " + contentType + " detected on " + path.toAbsolutePath());
						
						String filename = path.getFileName().toString();
						
						Path actualFile;
						if("application/x-bzip".equals(contentType)) {
							
	
							String rawUnzippedFilename = com.google.common.io.Files.getNameWithoutExtension(filename);
	
							String unzippedFilename = ".tmp-" + rawUnzippedFilename;
							
							actualFile = allowedFolder.resolve(unzippedFilename);
							
							if(!Files.exists(actualFile)) {
								Path tmpFile = allowedFolder.resolve(".tmp-unzipping-" + unzippedFilename);
	
								logger.info("bzip archive detected, unzipping to " + tmpFile.toAbsolutePath());
								
								try(InputStream in = new MetaBZip2CompressorInputStream(Files.newInputStream(path))) {
									Files.copy(in, tmpFile);
								}
								
								Files.move(tmpFile, actualFile);
							}
							
							toDelete.add(actualFile);
						} else {
							actualFile = allowedFolder.resolve(".tmp-" + filename);
	
							if(!Files.exists(actualFile)) {
								Files.createSymbolicLink(actualFile, path);
							}
							
							toDelete.add(actualFile);
						}
						
						logger.info("Registering " + actualFile.toAbsolutePath() + " with Virtuoso RDF Loader");
						
						String allowedDir = allowedFolder.toString();
						String actualFilename = actualFile.getFileName().toString();
						
						VirtuosoBulkLoad.ldDir(conn, allowedDir, actualFilename, graphIri);
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
	