package org.aksw.ckan_deploy.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.DCAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;


/**
 * A catalog resolver can resolve
 * - datasets
 * - distributions
 * 
 * @author Claus Stadler, Nov 22, 2018
 *
 */
interface CatalogResolver {
	Maybe<DatasetResolver> resolveDataset(String datasetId) throws Exception;
	Flowable<DistributionResolver> resolveDistribution(String distributionId) throws Exception;


	/**
	 * Resolve a distributionId in regard to a given dataset.
	 * This method can be used to disambiguate a distribution should for some reason the
	 * case arise that the same distribution identifier is used with different datasets.
	 * 
	 * 
	 * @param datasetId
	 * @param distributionId
	 * @return
	 */
	Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId);
}



class CatalogResolverCkan
	implements CatalogResolver
{	
	private static final Logger logger = LoggerFactory.getLogger(CatalogResolverCkan.class);
	
	protected CkanClient ckanClient;
	protected String prefix;
	
	public CatalogResolverCkan(CkanClient ckanClient, String prefix) {
		this.ckanClient = ckanClient;
		this.prefix = prefix;
	}
	
	@Override
	public Maybe<DatasetResolver> resolveDataset(String datasetId) {
		return Maybe.fromCallable(() -> {
			CkanDataset ckanDataset = ckanClient.getDataset(datasetId);
		
			PrefixMapping pm = DcatUtils.addPrefixes(new PrefixMappingImpl());
		
			DcatDataset dcatDataset = DcatCkanRdfUtils.convertToDcat(ckanDataset, pm);
		
			try {
				// Skolemize the resource first (so we have a reference to the resource)
				dcatDataset = DcatCkanRdfUtils.skolemizeClosureUsingCkanConventions(dcatDataset).as(DcatDataset.class);
	//			if(prefix != null) {
	//				dcatDataset = DcatCkanRdfUtils.assignFallbackIris(dcatDataset, prefix).as(DcatDataset.class);
	//			}
				
			} catch(Exception e) {
				logger.warn("Error processing dataset: " + datasetId, e);
			}
			
			return new DatasetResolverCkan(this, dcatDataset);
		});
		
		//RDFDataMgr.write(System.out, dcatDataset.getModel(), RDFFormat.NTRIPLES);
	
//		return Maybe.just(new DatasetResolverCkan(this, dcatDataset));
	}	
	
	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) throws IOException {
		
		// TODO Search ckan by extra:uri field - but this needs reconfiguration of ckan...
		CkanResource ckanResource = ckanClient.getResource(distributionId);
		String datasetId = ckanResource.getPackageId();		
		
		Flowable<DistributionResolver> result = resolveDataset(datasetId)
				.toFlowable().flatMap(dr -> dr.resolveDistribution(distributionId));

		return result;
	}
	
	@Override
	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
		Maybe<DatasetResolver> datasetResolver = resolveDataset(datasetId);
			
		Flowable<DistributionResolver> result = datasetResolver.toFlowable().flatMap(dr ->
			Flowable.fromIterable(dr.getDataset().getDistributions())
				.filter(d -> d.isURIResource() && d.getURI().equals(distributionId))
				.map(d -> d.as(DcatDistribution.class))
				.map(dcatDistribution -> new DistributionResolverCkan(dr, dcatDistribution)));
		
		return result;
	}
}


class DistributionResolverCkan
	implements DistributionResolver
{
	protected DatasetResolver datasetResolver;
	//protected DcatDataset dcatDataset;
	
	// Should be a resource mentioned in the dataset
	protected DcatDistribution dcatDistribution;

	public DistributionResolverCkan(DatasetResolver datasetResolver, DcatDistribution dcatDistribution) {
		//this.dcatDataset = dcatDataset;
		this.datasetResolver = datasetResolver;
		this.dcatDistribution = dcatDistribution;
	}
	
	@Override
	public DatasetResolver getDatasetResolver() {
		return null;
	}

	@Override
	public DcatDistribution getDistribution() {
		return dcatDistribution;
	}

	@Override
	public InputStream open() throws Exception {
		return open(dcatDistribution.getDownloadURL());
	}

	@Override
	public InputStream open(String url) throws Exception {
		boolean isValid = dcatDistribution.getDownloadURLs().contains(url);
		InputStream result;
		if(isValid) {
			result = new URI(url).toURL().openStream();
		} else {
			throw new RuntimeException("Given url is not declared to be a download URL of the distribution: " + url);
		}
		
		return result;
	}
}




/**
 * Resolve distributions in regard to a dataset
 * 
 * @author Claus Stadler, Nov 21, 2018
 *
 */
interface DatasetResolver {
	CatalogResolver getCatalogResolver();
	DcatDataset getDataset();
	Flowable<DistributionResolver> resolveDistribution(String distributionId) throws Exception;

//	Flowable<DistributionResolver> resolveDistribution(
//			Resource dcatDistribution,
//			Function<String, String> iriResolver) throws Exception;
}

interface DistributionResolver {
	DatasetResolver getDatasetResolver();
	DcatDistribution getDistribution();
	
	/**
	 * Open the distribution; throws an exception unless there is exactly 1 download url
	 * 
	 * @return
	 */
	InputStream open() throws Exception;
	InputStream open(String url) throws Exception;

	default InputStream open(Resource downloadUrl) throws Exception {
		InputStream result = open(downloadUrl.getURI());
		return result;
	}

}



class DatasetResolverCkan
	implements DatasetResolver
{
	protected CatalogResolver catalogResolver;
	protected DcatDataset dcatDataset;

	public DatasetResolverCkan(CatalogResolver catalogResolver, DcatDataset dcatDataset) {
		this.catalogResolver = catalogResolver;
		this.dcatDataset = dcatDataset;
	}
	
	
	@Override
	public DcatDataset getDataset() {
		return dcatDataset;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) throws Exception {
		return catalogResolver.resolveDistribution(dcatDataset.getURI(), distributionId);
	}

	
	//@Override
	public Collection<URI> resolveDistribution(Resource dcatDistribution, Function<String, String> iriResolver)
			throws Exception {

		// Sanity check that the given distribution is available in the metadata
		@SuppressWarnings("unlikely-arg-type")
		boolean isValid = ResourceUtils.listReverseProperties(dcatDistribution, DCAT.distribution)
				.toSet()
				.contains(dcatDataset);
	
		DcatDistribution d = dcatDistribution.as(DcatDistribution.class);
		Collection<URI> result = d.getDownloadURLs().stream()
			.map(iriResolver::apply)
			.map(DcatCkanDeployUtils::newURI)
			.filter(x -> x != null)
			.collect(Collectors.toList());
		
		return result;
	}


	@Override
	public CatalogResolver getCatalogResolver() {
		// TODO Auto-generated method stub
		return null;
	}



}


//interface DistributionResolver {
//	DatasetResolver getDatasetResolver();
//	
//}



class CatalogResolverFilesystem
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
	public Maybe<DatasetResolver> resolveDataset(String datasetId) throws Exception {
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
			
			
			result = Maybe.just(new DatasetResolverCkan(this, dcatDataset));
			
			
		}

		return result;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) throws Exception {
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
		
		
		return new DatasetResolverCkan(this, dcatDataset);
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
		
		Path result = Paths.get("./")
		.resolve(a)
		.resolve(b)
		.resolve((a.isEmpty() && b.isEmpty() ? "" : ".") + Optional.ofNullable(uri.getPath()).orElse(""))
		.resolve(Optional.ofNullable(uri.getQuery()).orElse(""));
		
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
	public static Collection<Path> allocateSymbolicLink(Path file, Path folder, String baseName) throws IOException {
		Collection<Path> result;

		// Check all symlinks in the folder whether any points to file
		result = Files.list(folder)
			.filter(Files::isSymbolicLink)
			.filter(t -> {
				Path tgt;
				try {
					 tgt = Files.readSymbolicLink(t);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				boolean r = Objects.equals(file, tgt);
				return r;
			})
			.collect(Collectors.toList());
		
		if(result.isEmpty()) {
			for(int i = 1; ; ++i) {
				String cand = baseName + (i == 1 ? "" : i);
				Path c = folder.resolve(cand);
				if(!Files.exists(c)) {
					Files.createSymbolicLink(c, file);
					result = Collections.singleton(c);
					break;
				}
			}
		}
		
		return result;
	}
}


class CatalogResolverCaching
	implements CatalogResolver
{
	// TODO Replace filesystem with a more general interface
	protected CatalogResolverFilesystem cache;
	protected CatalogResolver backend;
		
	public CatalogResolverCaching(CatalogResolverFilesystem cache, CatalogResolver backend) {
		super();
		this.cache = cache;
		this.backend = backend;
	}

	@Override
	public Maybe<DatasetResolver> resolveDataset(String datasetId) throws Exception {
		return cache.resolveDataset(datasetId).switchIfEmpty(
				backend.resolveDataset(datasetId).map(dr -> cache.doCacheDataset(datasetId, dr ))
		);
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
		// TODO Auto-generated method stub
		return null;
	}
	
}


//
public class DistributionResolverFilesystem {
	public static void main(String[] args) throws Exception {
		System.out.println("yay");
		CkanClient ckanClient = new CkanClient("http://ckan.qrowd.aksw.org", "25b91078-fbc6-4b3a-93c5-acfce414bbeb");
			
		
		//String id = "http://ckan.qrowd.aksw.org/dataset/8bbb915a-f476-4749-b441-5790b368c38b/resource/fb3fed1f-cc9a-4232-a876-b185d8e002c8/download/osm-bremen-2018-04-04-ways-amenity.sorted.nt.bz2";
		//String id = "fb3fed1f-cc9a-4232-a876-b185d8e002c8";
		//String id = "http://dcat.linkedgeodata.org/distribution/osm-bremen-2018-04-04-ways-amenity";
		
		String id = "org-linkedgeodata-osm-bremen-2018-04-04";
		CatalogResolver cr = new CatalogResolverCaching(
				CatalogResolverFilesystem.createDefault(),
				new CatalogResolverCkan(ckanClient, "http://foo"));
		
		DatasetResolver dr = cr.resolveDataset(id).blockingGet();
		System.out.println(dr);
	}
}
//
//	protected DcatRepoConfig repoConfig;
//	protected String catalogFolderName;
//	protected String datasetFolderName;
//	
//	
//	public static void main(String[] args) {
//		System.out.println("yay");
//		CkanClient ckanClient = new CkanClient("http://ckan.qrowd.aksw.org", "");
//
//		//String id = "http://ckan.qrowd.aksw.org/dataset/8bbb915a-f476-4749-b441-5790b368c38b/resource/fb3fed1f-cc9a-4232-a876-b185d8e002c8/download/osm-bremen-2018-04-04-ways-amenity.sorted.nt.bz2";
//		//String id = "fb3fed1f-cc9a-4232-a876-b185d8e002c8";
//		String id = "http://dcat.linkedgeodata.org/distribution/osm-bremen-2018-04-04-ways-amenity";
//		CkanResource r = ckanClient.getResource(id);
//		System.out.println(r);
//		System.out.println(r.getPackageId());
//	}
//	
//	public Collection<URI> resolveDistribution(
//			Resource dcatDistribution,
//			Function<String, String> iriResolver) throws Exception {
//	
//		return null;
//	}
//	
//	
//}

