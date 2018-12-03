package org.aksw.dcat.repo.impl.fs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.ckan_deploy.core.DcatCkanDeployUtils;
import org.aksw.ckan_deploy.core.DcatRepositoryDefault;
import org.aksw.ckan_deploy.core.DcatUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DatasetResolverImpl;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import com.google.common.base.StandardSystemProperty;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Related work on mapping RDF to file systems: http://ceur-ws.org/Vol-368/paper5.pdf
 *
 * @author Claus Stadler, Nov 23, 2018
 *
 */
public class CatalogResolverFilesystem
	implements CatalogResolver
{
	protected Path dcatRepoRoot;
	//protected Function<String, String> iriResolver;
	
	protected transient Path catalogFolder;

	protected transient Path datasetDataFolder;
	protected transient Path datasetByIdFolder;
	
	
	protected transient Path distributionIndexFolder;

	protected CatalogResolver delegate;
	
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
			Model dcatModel = RDFDataMgr.loadModel(dcatFile.toFile().getAbsolutePath());
			Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(dcatModel);
			DcatDataset dcatDataset = dcatDatasets.iterator().next();
			
			
			result = Maybe.just(new DatasetResolverImpl(this, dcatDataset));
			
			
		}

		return result;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) {
		// Check the distribution index for all datasets containing the distribution
		Path distributionFolder = distributionIndexFolder.resolve(resolvePath(distributionId));

		Path datasetLinkFolder = distributionFolder.resolve("datasets");
		if(Files.exists(distributionFolder)) {
			// Read the links to the dataset folder, then load the datasets' dcat.ttl files
			//datasetLinkFolder
		}
		
		// If the entry does not exist, try to resolve against the delegate
		if(delegate != null) {
			Flowable<DistributionResolver> tmp = delegate.resolveDistribution(distributionId);
			
//			tmp.map(distR -> {
//				DcatDatdistR.getDatasetResolver().getDataset();
//			});
		}
		
		return null;
	}

	
	/**
	 * Use resolveDistribution and 'disambiguate' results by the given dataset
	 * 
	 */
	@Override
	public Flowable<DistributionResolver> resolveDistribution(DcatDataset dcatDataset, String distributionId) {
		Flowable<DistributionResolver> result = resolveDistribution(distributionId)
			.filter(distR -> dcatDataset.getDistributions().contains(distR.getDistribution()));
		
		return result;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
		
		
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

		return null;
	}	
	
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
		
		return new DistributionResolverFilesystem(datar, dr.getDistribution());
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
	public Single<Path> doCacheDistribution(
			String datasetId,
//			String datasetId,
			String requestDistributionId,
			DistributionResolver dr, URL urlObj) {
		
		return Single.create(new SingleOnSubscribe<Path>() {

			@Override
			public void subscribe(SingleEmitter<Path> emitter) throws Exception {
				//emitter.setDisposable(() -> );
				
				String url = urlObj.toString();
				//	Path distFolder = null;
					//String datasetId = null;
					Path datasetFolder = findExistingDataset(datasetId);
					
					Path downloadsFolder = datasetFolder.resolve("_downloads");
				
					String folderName = StringUtils.urlEncode(url);	
					Path downloadFolder = downloadsFolder.resolve(folderName);
					
					DcatRepositoryDefault.downloadFile(url, downloadFolder);
			}
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