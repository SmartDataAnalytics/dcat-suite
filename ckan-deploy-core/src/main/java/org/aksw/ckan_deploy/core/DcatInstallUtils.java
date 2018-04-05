package org.aksw.ckan_deploy.core;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Download datasets based on dcat descriptions
 * 
 * @author raven Apr 5, 2018
 *
 */
public class DcatInstallUtils {
//	public static void install(Model dcatModel) {
	
	private static final Logger logger = LoggerFactory.getLogger(DcatInstallUtils.class);

	
	public static void install(Path repoFolder, DcatDataset dcatDataset, boolean forceReDownload) {
		for(DcatDistribution dcatDistribution : dcatDataset.getDistributions()) {
			
			install(repoFolder, dcatDistribution, forceReDownload);
//			for(Resource r : dcatDistribution.getDownloadURLs()) {
//			}
		}
	}
	
	
	
	public static void install(Path repoFolder, DcatDistribution dcatDistribution, boolean forceReDownload) {
		for(Resource r : dcatDistribution.getDownloadURLs()) {
			if(!r.isURIResource()) {
				logger.warn("Not a URI: " + r);
				continue;
			}

			try {
				download(repoFolder, dcatDistribution, r, forceReDownload);
			} catch(Exception e) {
				logger.warn("Failed to download " + r);
			}
		}
	}
	
	public static void download(Path repoFolder, DcatDistribution dcatDistribution, Resource r, boolean forceReDownload) {
		
		String uri = r.getURI();
		
		logger.info("Downloading " + uri);
		
		String filename = Optional.ofNullable(dcatDistribution.getName())
				.orElseThrow(() -> new RuntimeException("could not obtain a file name from downloadable distribution"));
		
		filename = StringUtils.trim(filename);
		if(filename.isEmpty()) { 
			throw new RuntimeException("Got empty file name");
		}
		
		Path relativeTargetFile = Paths.get(filename);
		Path targetFile = repoFolder.resolve(relativeTargetFile);
		
		
		try(InputStream in = new URI(uri).toURL().openStream()) {
			Files.copy(in, targetFile);
		} catch(Exception e) {
			logger.error("Failed to download from " + uri + " and write to " + targetFile, e);
		}		
	}

}