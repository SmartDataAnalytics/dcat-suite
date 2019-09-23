package org.aksw.dcat_suite.server.conneg;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.dcat_suite.algebra.Op;
import org.aksw.dcat_suite.algebra.OpExecutor;
import org.aksw.dcat_suite.algebra.OpPath;
import org.aksw.dcat_suite.algebra.OpUtils;
import org.aksw.dcat_suite.algebra.Planner;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpRequest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sys.JenaSystem;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.net.MediaType;

import avro.shaded.com.google.common.collect.Maps;

public class HttpResourceRepositoryManagerImpl
	implements HttpResourceRepositoryFromFileSystem
{
	//protected ResourceStoreImpl store;
	
	protected ResourceStore downloadStore;
	protected ResourceStore cacheStore;
	protected ResourceStore hashStore;
	
	//protected Function<String, Path> uriToRelPath;

	public HttpResourceRepositoryManagerImpl() {
		super();
//		this.uriToRelPath = CatalogResolverFilesystem::resolvePath;
	}

	public static HttpResourceRepositoryManagerImpl create(Path absBasePath) {
		HttpResourceRepositoryManagerImpl result = new HttpResourceRepositoryManagerImpl();
		result.setDownloadStore(new ResourceStoreImpl(absBasePath.resolve("downloads")));
		result.setCacheStore(new ResourceStoreImpl(absBasePath.resolve("cache")));
		
		result.setHashStore(new ResourceStoreImpl(absBasePath.resolve("hash")));
		
		return result;
	}
	
	
	public Collection<ResourceStore> getResourceStores() {
		return Arrays.asList(downloadStore, cacheStore);
	}
	
	public ResourceStore getDownloadStore() {
		return downloadStore;
	}

	public void setDownloadStore(ResourceStore downloadStore) {
		this.downloadStore = downloadStore;
	}

	public ResourceStore getCacheStore() {
		return cacheStore;
	}

	public void setCacheStore(ResourceStore cacheStore) {
		this.cacheStore = cacheStore;
	}
	
	public ResourceStore getHashStore() {
		return hashStore;
	}

	public void setHashStore(ResourceStore hashStore) {
		this.hashStore = hashStore;
	}


	public ResourceStore getStoreByPath(Path path) {
		ResourceStore result = getResourceStores().stream()
			.filter(store -> store.contains(path))
			.findFirst()
			.orElse(null);

		return result;
	}

	public Resource getInfo(Path path) {
		Resource result = Optional.ofNullable(getStoreByPath(path)).map(store -> store.getInfo(path))
				.orElse(null);
		return result;
	}
	
	// Assumes that there is at most 1 repository associated with a given path
	public RdfHttpEntityFile getEntityForPath(Path path) {
		Collection<ResourceStore> stores = getResourceStores();
		
		RdfHttpEntityFile result = stores.stream()
				.map(store -> store.getEntityForPath(path))
				.findFirst()
				.orElse(null);

		return result;
	}
	
	public Collection<RdfHttpEntityFile> getEntities(String uri) {
		Collection<ResourceStore> stores = getResourceStores();
		
		Collection<RdfHttpEntityFile> result = stores.stream()
				.map(store -> store.getResource(uri))
				.flatMap(res -> res.getEntities().stream())
				//.flatMap(store -> store.listEntities(relPath).stream())
				.collect(Collectors.toList());
		return result;
	}

	public RdfHttpEntityFile get(String url, String contentType, List<String> encodings) throws IOException {
		BasicHttpRequest r = new BasicHttpRequest("GET", url);
		r.setHeader(HttpHeaders.ACCEPT, contentType);
		String encoding = Stream.concat(encodings.stream(), Stream.of("identity;q=0"))
				.collect(Collectors.joining(","));
		
		r.setHeader(HttpHeaders.ACCEPT_ENCODING, encoding);

		RdfHttpEntityFile result = get(r, HttpResourceRepositoryManagerImpl::resolveRequest);
		return result;
	}
	
	public RdfHttpEntityFile get(HttpRequest request, Function<HttpRequest, Entry<HttpRequest, HttpResponse>> executor) throws IOException {
		Header[] headers = request.getAllHeaders();		
		
		String uri = request.getRequestLine().getUri();

		//RdfHttpResourceFile res = store.get(uri);
		
		
		Collection<RdfHttpEntityFile> entities = getEntities(uri);

		// Get the requested content types in order of preference	
		Map<MediaType, Float> mediaTypeRanges = HttpHeaderUtils.getOrderedValues(headers, HttpHeaders.ACCEPT).entrySet().stream()
				.collect(Collectors.toMap(e -> MediaType.parse(e.getKey()), Entry::getValue));


		List<MediaType> supportedMediaTypes = HttpHeaderUtils.supportedMediaTypes();
		
		// Get the requested encodings in order of preference	
		Map<String, Float> encodings = HttpHeaderUtils.getOrderedValues(headers, HttpHeaders.ACCEPT_ENCODING);

		
		//Map<Path, Float> candidateToScore = new HashMap<>();
		
		
		// TODO Find best candidate among the file entities
	
		
		// Score entities
		Map<RdfHttpEntityFile, Float> entityToScore = new HashMap<>();
		for(RdfHttpEntityFile entity : entities) {
			RdfEntityInfo info = entity.getCombinedInfo().as(RdfEntityInfo.class);
			MediaType mt = MediaType.parse(info.getContentType());
			
			for(MediaType range : supportedMediaTypes) {
				if(mt.is(range)) {
					entityToScore.put(entity, 1.0f);
				}
			}
		}
		
		// Pick entity with the best score
		RdfHttpEntityFile result = entityToScore.entrySet().stream()
			.sorted((a, b) -> a.getValue().compareTo(b.getValue()))
			.findFirst()
			.map(Entry::getKey)
			.orElse(null);
		
		
		//result = null;
		if(result == null) {
			RdfHttpResourceFile res = downloadStore.getResource(uri);
			Entry<HttpRequest, HttpResponse> response = executor.apply(request);
			
			result = saveResponse(res, response.getKey(), response.getValue());
		}

		return result;
	}

	
	/**
	 * Derives the suffix which to append to the base path from the entity's headers.
	 * 
	 * @param basePath
	 * @param entity
	 * @throws IOException 
	 * @throws UnsupportedOperationException 
	 */
	public RdfHttpEntityFile saveResponse(RdfHttpResourceFile targetResource, HttpRequest request, HttpResponse response) throws UnsupportedOperationException, IOException {
		HttpEntity entity = response.getEntity();
		
		// If the type is application/octet-steam we
		// can try to derive content type and encodings from
		// a content-disposition header or the original URI
		// In fact, we can try both things, and see whether any yields results
		// the results can be verified afterwards (e.g. by Files.probeContentType)
		// hm, since content-disposition seems to be non-standard maybe we can also just ignore it

		String ct = HttpHeaderUtils.getValue(new Header[] { entity.getContentType() }, HttpHeaders.CONTENT_TYPE);
		
		RdfEntityInfo meta = HttpHeaderUtils.copyMetaData(entity, null);
		if(ct.equalsIgnoreCase(ContentType.APPLICATION_OCTET_STREAM.getMimeType())) {
			String uri = request.getRequestLine().getUri();
			meta = ContentTypeUtils.deriveHeadersFromFileExtension(uri);
		}
		
		RdfHttpEntityFile rdfEntity = targetResource.allocate(meta);
		
		//Path targetPath = res.getPath().resolve("data");
		Path targetPath = rdfEntity.getAbsolutePath();

		// HACK - this assumes the target path refers to a file (and not a directory)!
		Files.createDirectories(targetPath.getParent());
		
		Path tmp = FileUtils.allocateTmpFile(targetPath);
		Files.copy(entity.getContent(), tmp, StandardCopyOption.REPLACE_EXISTING);

		// Compute hash
		ByteSource bs = com.google.common.io.Files.asByteSource(tmp.toFile());
		
		HashCode hashCode;
		try {
			hashCode = bs.hash(Hashing.sha256());
			String str = hashCode.toString();
			
			try {
				Files.createFile(targetPath);
			} catch (FileAlreadyExistsException e) {
				// Ignored
			}
			rdfEntity.updateInfo(info -> {
				HashInfo hi = info.getModel().createResource().as(HashInfo.class);
				
				hi.setAlgorithm("sha256").setChecksum(str);
				Collection<HashInfo> hashes = info.as(RdfEntityInfo.class).getHashes();
				hashes.add(hi);				
			});
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		Files.move(tmp, targetPath, StandardCopyOption.ATOMIC_MOVE);
		
		//RdfFileEntity result = new RdfFileEntityImpl(finalPath, meta);
//		result.setContentType(meta.getContentType());
//		result.setContentEncoding(meta.getContentEncoding());
		
		return rdfEntity;
	}
	
	
	
	/**
	 * May rewrite an original request and returns it together with its response
	 * 
	 * @param request
	 * @return
	 */
	public static Entry<HttpRequest, HttpResponse> resolveRequest(HttpRequest request) {
		request.getRequestLine().getUri();
		
		// Extract a dataset id from the URI
		// Check all data catalogs for whether they can resolve the id
		
		// Fake a request to a catalog for now - the result is a dcat model
		Model m = RDFDataMgr.loadModel("/home/raven/.dcat/repository/datasets/data/www.example.org/dataset-dbpedia-2016-10-core/_content/dcat.ttl");
		
		//System.out.println(m.size());
		
		String url = "http://downloads.dbpedia.org/2016-10/core-i18n/en/genders_en.ttl.bz2";
		//String url = m.listObjectsOfProperty(DCAT.downloadURL).mapWith(x -> x.asNode().getURI()).next();
		System.out.println(url);
		
		HttpClient client = HttpClientBuilder.create().build();
		
		
		HttpUriRequest myRequest =
				RequestBuilder
				.copy(request)
				.setUri(url)
				.build();
		
		//new DefaultHttpRequestFactory().
		HttpResponse response;
		try {
			response = client.execute(myRequest);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		//client.execute(request, context)
		
		//m.listObjectsOfProperty(DCAT.downloadURL).toList();
		
		//throw new RuntimeException("not implemented yet");
		return Maps.immutableEntry(myRequest, response);
	}
	
	public static void main(String[] args) throws IOException {
		JenaSystem.init();

		TurtleNoBaseTest.initTurtleWithoutBaseUri();

		JenaPluginUtils.registerResourceClass(RdfEntityInfo.class, RdfEntityInfoDefault.class);
		JenaPluginUtils.registerResourceClasses(RdfGav.class);
		JenaPluginUtils.registerResourceClasses(HashInfo.class);


		
		JenaPluginUtils.scan(Op.class);

		Path root = Paths.get("/home/raven/.dcat/test3");
		Files.createDirectories(root);

		HttpResourceRepositoryManagerImpl manager = create(root);

		ResourceStore store = manager.getDownloadStore();
		ResourceStore hashStore = manager.getHashStore();
		
		
//		String url = "/home/raven/.dcat/test3/genders_en.ttl.bz2";
		String url = "http://downloads.dbpedia.org/2016-10/core-i18n/en/genders_en.ttl.bz2";
//		String url = "/home/raven/Projects/limbo/git/train_3-dataset/target/metadata-catalog/catalog.all.ttl";
//		Model m = RDFDataMgr.loadModel(url);
//		try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(m))) {
//			for(int i = 0; i < 10; ++i) {
//				try(QueryExecution qe = conn.query("SELECT * { ?s ?p ?o BIND(RAND() AS ?sortKey) } ORDER BY ?sortKey LIMIT 1")) {
//					System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//				}
//			}
//		}
		
		RdfHttpEntityFile entity = manager.get(url, "application/turtle", Arrays.asList("bzip2"));
		
		//RdfHttpResourceFile res = store.getResource(url);
		//RdfHttpEntityFile entity = res.getEntities().iterator().next();
		
		Op op = Planner.createPlan(entity, "application/rdf+xml", Arrays.asList("bzip2"));
		RDFDataMgr.write(System.out, op.getModel(), RDFFormat.TURTLE_PRETTY);
		System.out.println("Number of ops before optimization: " + OpUtils.getNumOps(op));
		
		OpExecutor executor = new OpExecutor(manager, hashStore);

		//ModelFactory.createDefaultModel()		
		
		op = executor.optimizeInPlace(op);
		RDFDataMgr.write(System.out, op.getModel(), RDFFormat.TURTLE_PRETTY);
		System.out.println("Number of ops after optimization: " + OpUtils.getNumOps(op));

		
		op.accept(executor);
		
		//Planner.execute(op);
		
		
		if(true) {
			return;
		}
		
//		RdfFileResource res = rm.get("http://downloads.dbpedia.org/2016-10/core-i18n/en/genders_en.ttl.bz2");
		
		

		BasicHttpRequest r = new BasicHttpRequest("GET", url);
		r.setHeader(HttpHeaders.ACCEPT, WebContent.contentTypeTurtleAlt2);
		r.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip,identity;q=0");

		manager.get(r, HttpResourceRepositoryManagerImpl::resolveRequest);
		
	}
}
