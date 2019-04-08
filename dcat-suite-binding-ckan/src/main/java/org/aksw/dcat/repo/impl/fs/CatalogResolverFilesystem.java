package org.aksw.dcat.repo.impl.fs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.ckan_deploy.core.DcatCkanDeployUtils;
import org.aksw.ckan_deploy.core.DcatRepositoryDefault;
import org.aksw.ckan_deploy.core.DcatUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.dcat.repo.impl.core.DatasetResolverImpl;
import org.aksw.dcat.repo.impl.core.DistributionResolverImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * Related work on mapping RDF to file systems: http://ceur-ws.org/Vol-368/paper5.pdf
 *
 * @author Claus Stadler, Nov 23, 2018
 *
 */
public class CatalogResolverFilesystem
	implements CatalogResolverCacheCapable
{
	private static final Logger logger = LoggerFactory.getLogger(CatalogResolverFilesystem.class);
	
	protected Path dcatRepoRoot;
	//protected Function<String, String> iriResolver;
	
	protected transient Path catalogFolder;

	protected transient Path datasetDataFolder;
	protected transient Path datasetByIdFolder;
	
	protected transient Path distributionIndexFolder;

	protected transient Path downloadBaseFolder;
	protected transient Path downloadFolder;
	
	
	protected transient Path hashSpaceFolder;

	//protected CatalogResolver delegate;
	
	public static CatalogResolverFilesystem createDefault() {
		String homeDir = System.getProperty(StandardSystemProperty.USER_HOME.key());
		
		Path root = Paths.get(homeDir).resolve(".dcat");
		return new CatalogResolverFilesystem(root);
	}
	
	public CatalogResolverFilesystem(Path dcatRepoRoot) {
		this.dcatRepoRoot = dcatRepoRoot;
		
		Path tmp = dcatRepoRoot.resolve("repository"); 
		this.catalogFolder = tmp.resolve("catalogs");
		
		Path tmp2 = tmp.resolve("datasets");
		this.datasetDataFolder = tmp2.resolve("data");
		
		// TODO Make index directories more flexible: 
		// In principle, we could have a mapping from attribute to directory, whereas "id"
		// is a special attribute
		// Wouldn't that by quite similar to Harsh' property graph / RDF mapping?
		this.datasetByIdFolder = tmp2.resolve("by-id");
		
		this.distributionIndexFolder = tmp.resolve("distributions");
		
		this.downloadBaseFolder = tmp.resolve("downloads");
		this.downloadFolder = downloadBaseFolder.resolve("by-url");
		this.hashSpaceFolder = downloadBaseFolder.resolve("by-md5");
	}

	
	public Path findExistingDataset(String datasetId) {
		Path relPath = resolvePath(datasetId);
		List<Path> cands = Arrays.asList(
				datasetDataFolder.resolve(relPath).resolve("_content"),
				datasetByIdFolder.resolve(relPath).resolve("_content"));
		
		
		Path result = null;
		for(Path cand : cands) {
			// TODO [fixed?] For id folders the link ends in _dataset; for content folder, the suffix is _content
			result = cand.resolve("dcat.ttl");
			
			if(Files.exists(result)) {
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * The main point of resolving a dataset is, that it creates
	 * symbolic links for all distributions.
	 * 
	 * This means, that subsequent lookup for distributions point to the dataset folder, from where the metadata 
	 * is accessed.
	 * 
	 */
	@Override
	public Maybe<DatasetResolver> resolveDataset(String datasetId) {
		Path dcatFile = findExistingDataset(datasetId);
		
		Maybe<DatasetResolver> result;
		if(!Files.exists(dcatFile)) {
			result = Maybe.empty();
//			Files.createDirectories(datasetFolder);
//
//			CatalogResolver datasetResolver = delegate.resolveDataset(datasetId);
//
//			DcatDataset dataset = datasetResolver.getDataset();
//			Model closure = org.apache.jena.util.ResourceUtils.reachableClosure(dataset);
//			
//			RDFDataMgr.write(Files.newOutputStream(dcatFile), closure, RDFFormat.TURTLE);			
		} else {
			DcatDataset dcatDataset = loadDcatDataset(dcatFile);
			result = Maybe.just(new DatasetResolverImpl(this, dcatDataset));
		}

		return result;
	}
	
	
	public static DcatDataset loadDcatDataset(Path path) {
		Model dcatModel = RDFDataMgr.loadModel(path.toFile().getAbsolutePath());
		Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(dcatModel);
		DcatDataset result = dcatDatasets.iterator().next();

		return result;
	}

	public Path resolveDistributionPath(String distributionId) {
		Path relativePath = resolvePath(distributionId);
		Path distributionFolder = distributionIndexFolder.resolve(relativePath);
		return distributionFolder;
	}
	
	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) {
		// Check the distribution index for all datasets containing the distribution
		Flowable<DistributionResolver> result;
		
		Path distributionFolder = resolveDistributionPath(distributionId); //distributionIndexFolder.resolve(resolvePath(distributionId));
		
		if(Files.exists(distributionFolder)) {
			// Read the links to the dataset folder, then load the datasets' dcat.ttl files
			// datasetLinkFolder
			try {
				List<Path> datasets = Files.list(distributionFolder)
					.filter(Files::isSymbolicLink)
					.map(p -> p.resolve("dcat.ttl"))
					.filter(Files::exists)
					.collect(Collectors.toList());
				
				logger.info("Resolved distribution " + distributionId + " to " + datasets);
				//System.out.println("Entries: " + datasets);
				if(datasets.isEmpty()) {
					result = Flowable.empty();
				} else if(datasets.size() == 1) {
					DcatDataset dcatDataset = loadDcatDataset(datasets.iterator().next());
					//result = resolveDistribution(dcatDataset, distributionId);
					DatasetResolver dr = new DatasetResolverImpl(this, dcatDataset);
					
					// Find the dcat distribution that matches the given ID
					DcatDistribution dcatDistribution = dcatDataset.getDistributions().stream()
						.filter(r -> r.getURI().equals(distributionId))
						.filter(r -> resolvePath(r.getURI()) != null)
						.findAny().orElse(null);
					
					//dcatDataset.getDistributions().contains(distR.getDistribution());
							
					result = Flowable.just(new DistributionResolverImpl(dr, dcatDistribution));
				} else {
					throw new RuntimeException("Distribution contained in multiple datasets: (TODO: If the distribution files are equal (i.e. have equal hash codes), this is acceptable)");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		} else {
			result = Flowable.empty();
		}


		return result;
	}

	
	/**
	 * Use resolveDistribution and 'disambiguate' results by the given dataset
	 * 
	 */
	@Override
	public Flowable<DistributionResolver> resolveDistribution(DcatDataset dcatDataset, String distributionId) {
		Flowable<DistributionResolver> result = resolveDistribution(distributionId)
//				.doOnNext(distributionResolver -> {
//					System.out.println("Test: " + distributionResolver.getDatasetResolver().getDataset().getURI().equals(dcatDataset.getURI()));
//				})
				.filter(distributionResolver -> distributionResolver.getDatasetResolver().getDataset().getURI().equals(dcatDataset.getURI()));
				//.filter(distR -> dcatDataset.getDistributions().contains(distR.getDistribution()));
		
		return result;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
		return resolveDistribution(distributionId);
		// TODO Filter by given dataset id
//				.filter(distribution -> distribution.getDatasetResolver().getDataset().)
		
//		// Derive names from the given ids
//		String catalogFolderName = "";
//		Path datasetFolder = datasetFolder.resolve(resolvePath(datasetId));
//
//		
//		
//		String distributionFolderName = "";
//		Path distributionPath = catalogFolder
//			.resolve(catalogFolderName)
//			.resolve(datasetFolderName)
//			.resolve(distributionFolderName);
//
//		// If the distribution does not yet exist, update all links
//		Path distributionFile = null;
//
//		// Allocate a new symlink if needed
//		String baseName = "d";
//		allocateSymbolicLink(distributionFile, repoConfig.distributionsFolder, baseName);
	}	
	
	@Override
	public DistributionResolver doCacheDistribution(
			//String datasetId,
			String requestDistributionId,
			DistributionResolver dr) {
		
		DatasetResolver datar = dr.getDatasetResolver();
		String datasetId = datar.getDataset().getURI();
		
		Function<String, String> iriResolver = iri -> iri;
		Collection<URL> urls = dr.getDistribution().getDownloadURLs().stream()
				.map(iriResolver::apply)
				.map(DcatCkanDeployUtils::newURI)
				.filter(x -> x != null)
				.map(DcatCkanDeployUtils::toURL)
				.collect(Collectors.toList());

		
		for(URL url : urls) {
			doCacheDistribution(datasetId, requestDistributionId, dr, url);
		}
		
		return new DistributionResolverImpl(datar, dr.getDistribution());
	}

	@Override
	public Maybe<URL> doCacheDownload(URL downloadUrl) throws IOException {
		String downloadUri = downloadUrl.toString();
		Path folder = downloadFolder.resolve(resolvePath(downloadUri)).resolve("_file");
		
		// Check if the folder already contains a file
		Collection<Path> files = Files.exists(folder)
				? Files.list(folder).collect(Collectors.toList())
				: Collections.emptySet();

		Path r;
		if(files.isEmpty()) {
			Path fileFolder = Files.createDirectories(folder);
			r = DcatRepositoryDefault.downloadFile(downloadUri, fileFolder);
			
			boolean useHashSpace = true;
			if(useHashSpace) {
				String md5;
				try (InputStream is = Files.newInputStream(r)) 
				{
					md5 = DigestUtils.md5Hex(is);
				}
				
				String prefix = md5.substring(0, 2);
				String suffix = md5.substring(2);
				Path target = hashSpaceFolder.resolve(prefix).resolve(suffix);
				Files.createDirectories(target.getParent());
				
				if(Files.exists(target)) {
					// We can delete the redundant download and just link to the existing target
					Files.delete(r);
				} else {
					Files.move(r, target);
				}

				Files.createSymbolicLink(r, target);
				
				//r = target;
			}
			
		} else if(files.size() == 1) {
			r = files.iterator().next();
		} else {
			throw new RuntimeException("Multiple files for " + downloadUri + " in folder: " + folder);
		}
	
		return Maybe.just(r.toUri().toURL());		
	}
	
	// TODO Result should probably be a Single / CompletableFuture
	@Override
	public Maybe<URL> resolveDownload(String downloadUri) throws Exception {
		Path folder = downloadFolder.resolve(resolvePath(downloadUri)).resolve("_file");
		
		Maybe<URL> result;
		// Check if the folder - if it even exists - already contains a (valid link to the) file
		if(Files.exists(folder)) {
			Collection<Path> files = Files.list(folder).collect(Collectors.toList());
			Path r;
			if(files.isEmpty()) {
				result = Maybe.empty();
			} else if(files.size() == 1) {
				r = files.iterator().next();

				Path resolved = resolveSymbolicLink(r);
				if(!Files.exists(resolved)) {
					if(Files.isSymbolicLink(r)) {
						Files.delete(r);
					}
					result = Maybe.empty();
				} else {
					result = Maybe.just(r.toUri().toURL());
				}
			} else {
				throw new RuntimeException("Multiple files for " + downloadUri + " in folder: " + folder);
			}
		} else {
			result = Maybe.empty();
		}

		return result;
	}
	
	public static Path resolveSymbolicLink(Path path) {
		Path result = path;
		
		Set<Path> seen = new HashSet<>();
		while(!seen.contains(result) && Files.isSymbolicLink(result)) {
			seen.add(result);
			try {
				result = Files.readSymbolicLink(result);
			} catch (IOException e) {
				logger.warn("Should not happen", e);
			}
		}
		
		return result;
	}

	
	/**
	 * Downloads a distribution and yields the target file once complete
	 * 
	 * 
	 * @param datasetId
	 * @param requestDistributionId
	 * @param dr
	 * @param urlObj
	 * @return
	 */
	@Override
	public CompletableFuture<Path> doCacheDistribution(
			String datasetId,
//			String datasetId,
			String requestDistributionId,
			DistributionResolver dr, URL urlObj) {

		
		return CompletableFuture.supplyAsync(() -> {
			String url = urlObj.toString();
			//	Path distFolder = null;
			//String datasetId = null;
			Path datasetFolder = findExistingDataset(datasetId);
			
			Path downloadsFolder = datasetFolder.resolve("_downloads");
		
			String folderName = StringUtils.urlEncode(url);	
			Path downloadFolder = downloadsFolder.resolve(folderName);
			
			Path r;
			try {
				r = DcatRepositoryDefault.downloadFile(url, downloadFolder);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return r;
		});

	//	DcatDistribution dcatDistribution = dcatDataset.getModel().createResource(distributionId).as(DcatDistribution.class);
	//	
//		Function<String, String> iriResolver = iri -> iri;
	//	//@Override
	//	public Collection<URI> resolveDistribution(Resource dcatDistribution, Function<String, String> iriResolver)
	//			throws Exception {
	//
		//Function<String, String> iriResolver
		// Sanity check that the given distribution is available in the metadata
		//@SuppressWarnings("unlikely-arg-type")
	//	Set<DcatDataset> datasets = new SetFromResourceAndInverseProperty<>(dcatDistribution, DCAT.distribution, DcatDataset.class);
	//	boolean isValid = datasets.contains(dcatDataset);
	//
	//	if(!isValid) {
	//		throw new RuntimeException("Distribution not declared at dataset:\nDataset: "+ dcatDataset.getURI() + "\nDistribution: " + distributionId);
	//	}
	
	//	DcatDistribution d = dcatDistribution.as(DcatDistribution.class);
			
		
		
		//DcatRepositoryDefault.downloadFile(url, targetPath);
	}
	
	/**
	 * Retrieves the dcat metadata record from a resolver and creates files and symbolic links
	 * for the metadata and indexes on the file system.
	 * This does not download distributions, but it links the distributions ids with the dataset id.
	 * 
	 * @param requestId
	 * @param dr
	 * @return
	 * @throws Exception
	 */
	@Override
	public DatasetResolver doCacheDataset(String requestId, DatasetResolver dr) throws Exception {
		DcatDataset dcatDataset = dr.getDataset();
		String datasetId = dcatDataset.getURI();
		
		//Path folder = datasetFolder.resolve(CatalogResolverFilesystem.resolvePath(datasetId));
		
		Path dsFolder = datasetDataFolder.resolve(resolvePath(datasetId));
		// Move to a subfolder to avoid clashes with other ids
		dsFolder = dsFolder.resolve("_content");

		Files.createDirectories(dsFolder);
		Path dcatFile = dsFolder.resolve("dcat.ttl");
		
		RDFDataMgr.write(new FileOutputStream(dcatFile.toFile()), dcatDataset.getModel(), RDFFormat.TURTLE);

		// Index the distributions
		indexDistributions(dcatDataset, dsFolder);

		// Index alt ids
		List<String> altIds = Arrays.asList(requestId);
		for(String altId : altIds) {
			Path tgt = datasetByIdFolder.resolve(resolvePath(altId));
			Files.createDirectories(tgt);
			allocateSymbolicLink(dsFolder, tgt, "_content");
		}
		
		
		return new DatasetResolverImpl(this, dcatDataset);
	}
	
	public void indexDistributions(DcatDataset dcatDataset, Path targetDatasetFolder) throws Exception {
		for(DcatDistribution dcatDistribution : dcatDataset.getDistributions()) {
			if(dcatDistribution.isURIResource()) {
				String uri = dcatDistribution.getURI();
				
				Path linkSource = distributionIndexFolder.resolve(resolvePath(uri));
				Files.createDirectories(linkSource);
				
				allocateSymbolicLink(targetDatasetFolder, linkSource, "_content");
			}
		}
	}

	

	public static Path resolvePath(String uri)  {
		URI u = DcatCkanDeployUtils.newURI(uri);
		
		Path result = u == null ? Paths.get(StringUtils.urlEncode(uri)) : resolvePath(u);
		return result;
	}

	public static Path resolvePath(URI uri) {
		String a = Optional.ofNullable(uri.getHost()).orElse("");
		String b = uri.getPort() == -1 ? "" : Integer.toString(uri.getPort());
		
		Path result = Paths.get(".")
		.resolve(a)
		.resolve(b)
		.resolve((a.isEmpty() && b.isEmpty() ? "" : ".") + Optional.ofNullable(uri.getPath()).orElse(""))
		.resolve(Optional.ofNullable(uri.getQuery()).orElse(""))
		.normalize();
		
		return result;
	}
	

	/**
	 * Within 'folder' create a link to 'file' with name 'baseName' if it does not yet exist.
	 * Return the new link or or all prior existing link(s)
	 * 
	 * @param file
	 * @param folder
	 * @param baseName
	 * @return
	 * @throws IOException
	 */
	public static Collection<Path> allocateSymbolicLink(Path rawTarget, Path rawSourceFolder, String baseName) throws IOException {
		Path sourceFolder = rawSourceFolder.normalize();
		Path target = rawTarget.normalize();
		
		Path relTgt = sourceFolder.relativize(target);

		Path absTarget = target.toAbsolutePath();
//		Path folder = rawFolder.normalize();
//		Path file = rawFile.normalize().relativize(folder);
		
		//System.out.println("Realtivation: " + file.relativize(folder));
		
		Collection<Path> result;

		// Check all symlinks in the folder whether any points to target
		result = Files.list(sourceFolder)
			.filter(Files::isSymbolicLink)
			.filter(t -> {
				Path tgt;
				try {
					 tgt = Files.readSymbolicLink(t);
					 tgt = tgt.toAbsolutePath();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				boolean r = Objects.equals(absTarget, tgt);
				return r;
			})
			.collect(Collectors.toList());
		
		if(result.isEmpty()) {
			for(int i = 1; ; ++i) {
				String cand = baseName + (i == 1 ? "" : i);
				Path c = sourceFolder.resolve(cand);
				
				//Path relTgt = c.relativize(target);
				
				if(!Files.exists(c)) {
					Files.createSymbolicLink(c, relTgt);
					result = Collections.singleton(c);
					break;
				}
			}
		}
		
		return result;
	}
}