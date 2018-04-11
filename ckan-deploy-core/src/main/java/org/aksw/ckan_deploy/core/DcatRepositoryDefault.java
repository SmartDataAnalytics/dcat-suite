package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Dcat Repository is essentially a cache for downloads, typically backed by a file system.
 * Instead of using the download URLs, the dcat repository caches on distribution IRI level.
 * 
 * The cache is *distribution iri*-centric.
 * 
 * This means, that if a distribution IRI is in the cache, its available data is the one being referenced 
 * - regardless of its origin download URLs or URL of the dcat dataset resource that references it.
 * 
 * Distribution iris are resolved a dcat repo and a dcat model.
 * If there is no entry in the repo, the model will be used to create it.
 * 
 * 
 * One distribution should only have at most one data file attached.
 * 
 * 
 * TODO What to do with the provided dcat models?
 * - We could take the closure of the dataset resource and put it into a file in a corresponding folder
 *   - 
 * 
 * 
 * `dcat install dcat.rdf` will thus go through all distributions IRIs, and check which ones are not yet in the cache.
 * 
 * dcat deploy virtuoso dcat.rdf -ds 'datasetid' will deploy all given datsaets.
 * As the bulk loader requires access to files, it will request URI objects for each distribution from the dcat repo,
 * and check whether they can be accessed as Paths.
 * 
 * 
 * 
 * @author raven Apr 6, 2018
 *
 */
public class DcatRepositoryDefault
	implements DcatRepository
{
	
	private static final Logger logger = LoggerFactory.getLogger(DcatRepositoryDefault.class);

	
	// ~/.dcat/repository
	protected Path dcatRepoRoot;
	//protected Function<String, String> iriResolver;
	
	protected transient Path datasetsFolder;
	protected transient Path distributionsFolder;
	
	
	public DcatRepositoryDefault(Path dcatRepoRoot) { //, Function<String, String> iriResolver) {
		this.dcatRepoRoot = dcatRepoRoot;
		//this.iriResolver = iriResolver;
		
		datasetsFolder = dcatRepoRoot.resolve("datasets");
		distributionsFolder = dcatRepoRoot.resolve("distributions");
	}
	
	/**
	 * Find a dataset with the given id in the repo 
	 * 
	 * @param dcatDataset
	 * @return
	 */
	public URI resolveDataset(Resource dcatDataset) {
		if(!dcatDataset.isURIResource()) {
			throw new RuntimeException("Non IRI datasets resources currently not supported");
		}
	
		String str = dcatDataset.getURI();
		String encStr = StringUtils.urlDecode(str);
		
		Path datasetFolder = datasetsFolder.resolve(encStr);
		
		// If not exist
		return null;
	}
	
	public static class DownloadInfo {
		public String filename;
		public Path tmpPath;
		public Path targetPath;
		public CompletableFuture<?> future;

		public DownloadInfo(String filename, Path tmpPath, Path targetPath, CompletableFuture<?> future) {
			super();
			this.filename = filename;
			this.tmpPath = tmpPath;
			this.targetPath = targetPath;
			this.future = future;
		}		
	}

	public static Path downloadFile(String url, Path targetPath) throws IOException {
		
		Path result;
		
		Path path = DcatCkanDeployUtils.newURI(url).flatMap(DcatCkanDeployUtils::pathsGet).orElse(null);
		if(path != null && Files.exists(path)) {
			result = targetPath.resolve(path.getFileName());
			Files.copy(path, result);
		} else if(url.startsWith("http")) {
		
			result = downloadFileHttp(url,
					fn -> targetPath.resolve("." + fn + ".part"),
					fn -> targetPath.resolve(fn));
		} else {
			throw new RuntimeException("Don't know how to handle " + url);
		}
//		downloadFile(url, targetPath);
//		DownloadInfo result;
//		Executor executor = Executors.newSingleThreadExecutor();
//		
//		CompletionService<?> cs = new ExecutorCompletionService<>(executor);
//		result = downloadFile(url,
//				fn -> targetPath.resolve("." + fn + ".part"),
//				fn -> targetPath.resolve(fn),
//				cs);
//		
//		result.future.h
//
		return result;
	}
	
	// TODO Switch to some sophisticated downloader, that could e.g. restart failed downloads
	// e.g. https://developers.google.com/api-client-library/java/google-api-java-client/media-download
	public static Path downloadFileHttp(
			String url,
			Function<String, Path> filenameToTmpPath,
			Function<String, Path> filenameToPath) throws IOException {
		
	    CloseableHttpClient httpclient = HttpClients.createDefault();
	    HttpGet httpGet = new HttpGet(url);
	    CloseableHttpResponse response;
	    response = httpclient.execute(httpGet);

	    Path result;
		try {
	        HttpEntity entity = response.getEntity();
	
	        Header disposition = response.getFirstHeader("Content-Disposition");
	        String dispositionValue = disposition != null ? disposition.getValue() : "";
	        String key = "filename=";
	        int index = dispositionValue.indexOf(key);
	
		    String filename = index >= 0
		    		? dispositionValue.substring(index + key.length(), dispositionValue.length() - 1)
		    		//: DcatCkanDeployUtils.newURI(url).map(uri -> Paths.get(uri).getFileName().toString()).orElse(null);
		    		: DcatCkanDeployUtils.newURI(url).map(uri -> Paths.get(uri.getPath()).getFileName().toString()).orElse(null);
		    		
			result = filenameToPath.apply(filename);

		    if(filename == null) {
		    	throw new RuntimeException("Could not obtain a filename for url " + url);
		    }

		    try(InputStream in = entity.getContent()) {
		    	Path tmpPath = filenameToTmpPath.apply(filename);
	    	
		    	Files.copy(in, tmpPath);
		    	Files.move(tmpPath, result);
		    }
		} finally {
			response.close();
		}
//    	if(!targetFile.getParent().equals(targetFolder)) {
//    		throw new RuntimeException("Can only download into specified folder - filename " + filename + " considered malicous");
//    	}
    	
//    	Future<?> future = completionService.submit(() -> {
//    		try(InputStream i = in) {
//				Files.copy(i, tmpPath);
//				Files.move(tmpPath, targetPath);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			} finally {
//				try {
//					response.close();
//				} catch (IOException e) {
//					logger.error("Error", e);
//				}
//			}
//    	}, null);
//
//    	DownloadInfo result = new DownloadInfo(filename, tmpPath, targetPath, future);
		return result;
	}
	
	public Collection<URI> resolveDistribution(Resource dcatResource, Function<String, String> iriResolver) throws IOException {
		if(!dcatResource.isURIResource()) {
			throw new RuntimeException("Non IRI distribution resources currently not supported");
		}
		
		DcatDistribution dcatDistribution = dcatResource.as(DcatDistribution.class);

		
		String str = dcatDistribution.getURI();

		URI uri = DcatCkanDeployUtils.newURI(str).orElse(null);
		Path relativeDistributionPath;
		if(uri != null) {
			// TODO Make uri -> path mapping configurable
			// This approach is vulnerable to leaving the repo folder if an uri had a path such as ../../../../
			relativeDistributionPath = Paths.get("./")
				.resolve(Optional.ofNullable(uri.getHost()).orElse(""))
				.resolve(uri.getPort() == -1 ? "" : Integer.toString(uri.getPort()))
				.resolve("." + Optional.ofNullable(uri.getPath()).orElse(""))
				.resolve(Optional.ofNullable(uri.getQuery()).orElse(""))
				.normalize();
			
			// actually we should be able to skip any fragment, as this will not be sent to a server anyway	
		} else {
			String encStr = StringUtils.urlEncode(str);
			relativeDistributionPath = Paths.get(encStr);
		}
		
		Path distributionFolder = distributionsFolder.resolve(relativeDistributionPath);
		boolean distributionFolderExists = Files.exists(distributionFolder);
	
		Path dataFolder = distributionFolder.resolve("_data");
		
		
		if(!distributionFolderExists) {
			Files.createDirectories(dataFolder);
			
			// TODO Use view method once its available
			Collection<String> downloadUrls = ResourceUtils.asStream(ResourceUtils.listPropertyValues(dcatDistribution, DCAT.downloadURL))
					.filter(RDFNode::isURIResource)
					.map(RDFNode::asResource)
					.map(Resource::getURI)
					.collect(Collectors.toList());

			for(String downloadUrl : downloadUrls) {
			
				downloadUrl = iriResolver.apply(downloadUrl);
//				List<String> resolvedUrls = downloadUrls.stream()
//						.filter(Resource::isURIResource)
//						.map(Resource::getURI)
//						.map(iriResolver::resolveToStringSilent)
//						.collect(Collectors.toList());
				//String url = iriResolver.resolveToStringSilent(downloadURL);

				
				logger.info("Downloading " + downloadUrl);
				Path file = downloadFile(downloadUrl, dataFolder);
				logger.info("Download finished:\n  Url: " + downloadUrl + "\n  File: " + file + "\n");
			}
		}

		
		// TODO We should read out metadata from a (properties) file

		boolean dataFolderExists = Files.exists(dataFolder);

		// ... for now we take the first file in the data folder
		Collection<Path> dataFiles = dataFolderExists
				? Files.list(dataFolder).collect(Collectors.toList())
				: Collections.emptyList();

		Collection<URI> result = dataFiles.stream().map(Path::toUri).collect(Collectors.toList());
		
		logger.info("Distribution resolution:\n  Source: " + str + "\n  Target(s): " + result + "\n");
		
		// TODO If the distribution folder exists, but the data folder does not
		// we fail as another process might be working on it
		// We could use a watching mechanism to check whether any progress is going on
		
		return result;
	}
}
