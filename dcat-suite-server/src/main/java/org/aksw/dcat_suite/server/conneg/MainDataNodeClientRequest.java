package org.aksw.dcat_suite.server.conneg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.ckan_deploy.core.PathCoder;
import org.aksw.ckan_deploy.core.PathCoderRegistry;
import org.aksw.dcat.ap.domain.api.Spdx;
import org.aksw.dcat.repo.impl.fs.CatalogResolverFilesystem;
import org.aksw.dcat_suite.server.conneg.HttpAssetManagerFromPath.TransformStep;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.beam.repackaged.beam_sdks_java_core.com.google.common.collect.Iterables;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.net.MediaType;

import avro.shaded.com.google.common.collect.Maps;
import io.reactivex.Single;



/**
 * A simple cache for files.
 * 
 * The parameters of the cache and files are as follows:
 * cache:
 *   maximum size - when this size is reached, start evicting files
 * 
 * files:
 *   minLife: Files are typically not removed if this life time has not been reached yet (e.g. 1 day)
 *   minLifeCritical: If disk space is low, removal may still be allowed 
 *   maxLife: If a file exceeds this size, always remove it
 * 
 *   
 * 
 * 
 * @author raven
 *
 */
class FileCache {
	protected Path basePath;
	

	protected HashSpace hashSpace;

//	protected Path tempPath;
	
	protected long maxSize;
	protected long maxLife;

	protected Function<String, Path> keyToPath;
	
	

	
	public Path get(String key) {
		Path path = keyToPath.apply(key);
		
		Path tgt = basePath.resolve(path);
		
		return tgt;
	}

	/**
	 * 
	 * 
	 * @param key
	 * @param entity
	 * @return
	 */
	public Path put(String key, RdfEntity<?> entity) {
		// Check if an md5 hash is set
		Resource meta = entity.getMetadata();
		
		
		
		String algo = Strings.nullToEmpty(ResourceUtils.getLiteralPropertyValue(meta, Spdx.algorithm, String.class));
		String hash = ResourceUtils.getLiteralPropertyValue(meta, Spdx.checksumValue, String.class);

		
		String effectiveHash = null;
		
		Path tgtPath = null;
		// allow names like 'sha256'
		if(algo.toLowerCase().startsWith("sha256")) {
			effectiveHash = hash;
		} else {
			InputStream in = entity.getContent();
			try {
				Path tempPath = null;
				Path tempFile = Files.createTempFile(tempPath, "cache-", ".dat");
				Files.copy(in, tempFile);
				HashCode hashCode = com.google.common.io.Files.asByteSource(tempFile.toFile()).hash(Hashing.sha256());
				effectiveHash = hashCode.toString();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}

			// Now check the hash space for whether that entry already exists			
			tgtPath = hashSpace.get(effectiveHash).resolve("_content");
//			if(Files.exists(path)) {
//
//			}
		}
		
		
		//Path path = keyToPath.apply(key);


		return tgtPath;
	}

}






/**
 * Resource resolver that maps HTTP requests 
 * 
 * @author raven
 *
 */
class HttpAssetManagerFromPath
	//implements HttpAssetManager
{
	private static final Logger logger = LoggerFactory.getLogger(HttpAssetManagerFromPath.class);
	
	/**
	 * Base path for the repository (persistent cache) area
	 */
	protected Path basePath;
		
	protected HashSpace hashSpace;
	
	public HttpAssetManagerFromPath(Path basePath) {
		this.basePath = basePath;
		
		hashSpace = new HashSpaceImpl(basePath.resolve("hash-space"));
		
		uriToPath = CatalogResolverFilesystem::resolvePath;
		
		for(Lang lang : RDFLanguages.getRegisteredLanguages()) {
			String contentType = lang.getContentType().getContentType();
			String primaryFileExtension = Iterables.getFirst(lang.getFileExtensions(), null);
			if(primaryFileExtension != null) {
				ctExtensions.getPrimary().put(contentType, primaryFileExtension);
			}
			
			for(String fileExtension : lang.getFileExtensions()) {
				ctExtensions.getAlternatives().put(fileExtension, contentType);
			}
		}

		// TODO We need a registry for coders similar to RDFLanguages
		codingExtensions.putPrimary("gzip", "gz");
		codingExtensions.putPrimary("bzip", "bz2");
	}
	
	/**
	 * Map a given URI to a relative path
	 */
	protected Function<String, Path> uriToPath;

	protected MapPair<String, String> ctExtensions = new MapPair<>();
	protected MapPair<String, String> codingExtensions = new MapPair<>();
//	protected Map<String, String> contentTypeToFileExtension = new LinkedHashMap<>();
//	protected Map<String, String> fileExtensionToContentType = new LinkedHashMap<>();
//	
//	protected Map<String, String> encodingToFileExtension = new LinkedHashMap<>();
	
	

	

	

	
	public FileEntityEx pathToEntity(Path path) {
		RdfEntityInfo info = deriveHeadersFromFileExtension(path.getFileName().toString());
		
		FileEntityEx result = info == null
				? null
				: new FileEntityEx(path, info); 
		
		return result;
	}
	
//	CompletableFuture<HttpEntity>
	//@Override
	/**
	 * Lookup method which returns file entities according to Accept and Accept-Encoding headers.
	 * 
	 * Thereby, the interpretation of the Accept header is more broad:
	 * When requesting a concrete content type, such as turtle or ntriples, the lookup will make
	 * an extra lookup for which abstract syntax these content types are concrete syntaxes of,
	 * and expand the request accordingly.
	 * This means, a request for ntriples may yield RDF/XML because of link via the abstract syntax.
	 * However, if there are matches for the provided headers, they are preferred.
	 * 
	 * 
	 * 
	 * @param request
	 * @param executor
	 * @return
	 * @throws IOException
	 */
	public FileEntityEx get(HttpRequest request, Function<HttpRequest, Entry<HttpRequest, HttpResponse>> executor) throws IOException {
		Header[] headers = request.getAllHeaders();		
		
		// TODO The request typically points to a dataset
		// But in general it might also refer to a distributon, a download or even into the hash space.
		// the dataset may have multiple distributions
		// and each distribution may either point to remote downloads or locally generated resources
		// but in all cases, we first need to translate it into the set of download urls
		// the download url then points into an entry in the hash space
		// and for this hash space entry, we check whether transformations have been applied 
		
		// design issues:
		// how to add overlays? e.g. maybe there is simply a 'layers' directory in the dataset?
		// the purpose of layers is to add third-party information to existing datasets
		// how to save download files for non-download distributions?
		
		String uri = request.getRequestLine().getUri();
		Path relativePath = uriToPath.apply(uri);
		Path resourcePath = basePath.resolve(relativePath);

		// Get the list of cached resources
		List<Path> candidates;

		// Directory mode: the path obtained from the URL is assumed to refer to a directory which then contains the set of files from
		// among which to select the candidates
		// Prefix mode: the path is assumed to refer to a base filename - all files in the current directory with the same base name
		// are candidates
		boolean directoryMode = false;
		if(directoryMode) {
			candidates = Files.list(resourcePath).collect(Collectors.toList());
		} else {
			String prefix = relativePath.getFileName().toString();
			candidates = Files.list(resourcePath.getParent())
					.filter(p -> p.getFileName().toString().startsWith(prefix))
					.collect(Collectors.toList());
		}

		// Get the requested content types in order of preference	
		Map<MediaType, Float> mediaTypeRanges = getOrderedValues(headers, HttpHeaders.ACCEPT).entrySet().stream()
				.collect(Collectors.toMap(e -> MediaType.parse(e.getKey()), Entry::getValue));


		List<MediaType> supportedMediaTypes = supportedMediaTypes();
		
		// Get the requested encodings in order of preference	
		Map<String, Float> encodings = getOrderedValues(headers, HttpHeaders.ACCEPT_ENCODING);

		
		//Map<Path, Float> candidateToScore = new HashMap<>();
		
		// Score the candidates by the dimensions
		List<FileEntityEx> fileEntities = new ArrayList<>();
		for(Path cand : candidates) {
			FileEntityEx fileEntity = pathToEntity(cand);
			if(fileEntity != null) {
				fileEntities.add(fileEntity);
			}
		}

		
		// TODO Find best candidate among the file entities
	
		
		
		Map<FileEntityEx, Float> entityToScore = new HashMap<>();
		for(FileEntityEx fe : fileEntities) {
			MediaType mt = MediaType.parse(fe.getCombinedInfo().getContentType());
			
			for(MediaType range : supportedMediaTypes) {
				if(mt.is(range)) {
					entityToScore.put(fe, 1.0f);
				}
			}

//			for(Entry<MediaType, Float> mediaTypeRange : mediaTypeRanges.entrySet()) {
//				if(mt.is(mediaTypeRange.getKey())) {
//					entityToScore.put(fe, 1.0f);
//				}
//			}
		}
		
		FileEntityEx result = entityToScore.entrySet().stream()
			.sorted((a, b) -> a.getValue().compareTo(b.getValue()))
			.findFirst()
			.map(Entry::getKey)
			.orElse(null);
		
		
		
		// TODO The best entities are the ones which require fewest transformations
		// to fit the requests
		
		result = null;
		if(result == null) {
			
			Entry<HttpRequest, HttpResponse> response = executor.apply(request);
			//Header[] responseHeaders = response.getAllHeaders();

	//		resourcePath.resolve(data + suffix)
//			result = saveResponse(resourcePath, response.getKey(), response.getValue());
			
			
			
			result = saveResponse(resourcePath, response.getKey(), response.getValue());
		
		}

//		String suffix = toFileExtension(responseHeaders);
//		
//		Path tgtPath = null;
//		Files.copy(response.getEntity().getContent(), tgtPath, StandardCopyOption.REPLACE_EXISTING);
		
		return result;
	}
	
	
	

			
	

	public List<String> getValues(Header header) {
		List<String> result = getElements(new Header[] { header })					
				.map(HeaderElement::getName)
				.collect(Collectors.toList());

			return result;

	}



	
	
	static class TransformStep {
		Path destPath;
		BiFunction<Path, Path, Single<Integer>> method;
		
		public TransformStep(Path destPath, BiFunction<Path, Path, Single<Integer>> method) {
			super();
			this.destPath = destPath;
			this.method = method;
		}

		@Override
		public String toString() {
			return "TransformStep [suffix=" + destPath + ", method=" + method + "]";
		}
	}
	
	protected TransformStep createTransformStep(Path destPath, BiFunction<Path, Path, Single<Integer>> method) {
		return new TransformStep(destPath, method);
	}
	

	
	public CompletableFuture<FileEntityEx> execPlan(FileEntityEx source, List<TransformStep> plan) throws Exception {

		//List<TransformStep> plan = createPlan(source, rawTgtBasePath, tgtContentType, tgtEncodings);
		
		Path src = source.getRelativePath();
		Path dest = src;

		// Execute the plan
		for(TransformStep step : plan) {
			dest = step.destPath;
			
			if(!Files.exists(dest)) {

				Path tmp = allocateTmpFile(dest);
				logger.info("Creating " + dest + " from " + src + " via tmp " + tmp);
				step.method.apply(src, tmp)
					.blockingGet();
				
				Files.move(tmp, dest, StandardCopyOption.ATOMIC_MOVE);
			}
			

			src = dest;
		}
		
		System.out.println("Generated: " + dest);

		return null;
	}
	
	

//	public static <T> T safeMove(Path src, Path tgt, Function<Path, T> fn) {
//		Path tmp;
//		for(int i = 0; ; ++i) {
//			String idx = i == 0 ? "" : "-" + i;
//			tmp = tgt.getParent().resolve(tgt.getFileName().toString() + idx + ".tmp");
//			if(!Files.exists(tmp)) {
//				break;
//			}
//		}
//		
//		step.method.apply(src, tmp)
//			.blockingGet();
//		
//		Files.move(tmp, dest, StandardCopyOption.ATOMIC_MOVE);
//	}
	
	//String baseName, 
	//@Override
	public CompletableFuture<?> put(HttpRequest request, Function<HttpRequest, HttpEntity> executor) { //Supplier<? extends HttpEntity> supplier) {
//		Path path = basePath.resolve(baseName);
//
//		File file = null; // extract the file from the entity
		
		
		
//		FileEntity fe;
		
		
//		supplier.get();
		return null;
	}

}



public class MainDataNodeClientRequest {
	

	
	public static void mainProcessTest(String[] args) {
		PathCoder coder = PathCoderRegistry.get().getCoder("bzip");
		
		Single<Integer> single = coder.encode(Paths.get("/tmp/test.txt"), Paths.get("/tmp/test.bz2"));
//		Single<Integer> single = coder.encode(Paths.get("/tmp/germany-latest.osm.pbf"), Paths.get("/tmp/test.bz2"));
	
		System.out.println("Created the process");
		System.out.println(single.timeout(5, TimeUnit.SECONDS).blockingGet().intValue());
		System.out.println("Done");
	}

	// Test to resolve a file name to an http entity
	public static void main(String[] args) throws Exception {
		
		Path userDir = Paths.get(StandardSystemProperty.USER_HOME.value());
		HttpAssetManagerFromPath m = new HttpAssetManagerFromPath(userDir.resolve(".dcat/test"));
		
		BasicHttpRequest r = new BasicHttpRequest("GET", "foo");
		r.setHeader(HttpHeaders.ACCEPT, WebContent.contentTypeTurtleAlt2);
		r.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
		
		
		
		FileEntityEx fe = m.get(r, MainDataNodeClientRequest::resolveRequest);
		
		System.out.println("Source file entity: " + fe.getRelativePath());
		
		List<TransformStep> plan = m.createPlan(fe, Paths.get("/tmp/yay"), WebContent.contentTypeRDFXML, Arrays.asList("gzip"));
		m.execPlan(fe, plan);
		
		
		System.out.println("Done");
		
// Caching
// Cache control: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control
// https://stackoverflow.com/questions/24164014/how-to-enable-http-response-caching-in-spring-boot
// http://dolszewski.com/spring/http-cache-with-spring-examples/
		
		
// Sophisticated header examples: https://www.singular.co.nz/2008/07/finding-preferred-accept-encoding-header-in-csharp/
		BasicHeader header = new BasicHeader(org.apache.http.HttpHeaders.ACCEPT_ENCODING, "deflate;q=0.5,gzip;q=0.5,identity");
		for(HeaderElement e : header.getElements()) {
			//System.out.println(e);
			System.out.println(e.getName() + " " + Optional.ofNullable(e.getParameterByName("q")).map(NameValuePair::getValue).map(Float::parseFloat).orElse(null));
			//System.out.println(Arrays.toString(e.getParameters()));
		}
		
		
		if(true) return;
		
		Path p = Paths.get("/tmp/test.dat");
		
		OutputStream out = Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		new Thread(() -> {
			PrintStream ps = new PrintStream(out);
			for(int i = 0; i < 1000000000; ++i) {
				ps.println("" + i);
			}
		}).start();
	
		InputStream in = Files.newInputStream(p, StandardOpenOption.READ);
		Reader reader = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(reader);
		String line;
//		while((line = br.readLine()) != null) {
		while(in.available() > 0) {
			line = br.readLine();
			System.out.println(line);
		}
	}
	
	
	// Test to resolve a file name to an http entity
	public static void main3(String[] args) throws IOException {

		// probeContentType returns null if the file does not exist
		// so no attempt is made to guess the type from the file extension alone
		System.out.println(Files.probeContentType(Paths.get("/myFile.ttl.bz2")));
		System.out.println(Files.probeContentType(Paths.get("/home/raven/Projects/EclipseOld2/RDFUnit/archive/WWW_2014/patterns.ttl.bz2")));

		//Files.pro
		List<String> files = Arrays.asList("myFile.ttl.bz2");
	
		for(String file : files) {
			System.out.println(RDFLanguages.guessContentType(file));
		}
//		
////		HttpResponse resp = new BasicHttpResponse(StatusLine);
//		FileEntity test = new FileEntity(new File("/tmp/foo"));
//		//test.getContentEncoding().getValue()
//
//		HttpClient client = HttpClients.custom().build();
//		HttpUriRequest request = RequestBuilder.get()
//				.setUri("http://localhost:8086/org-linkedgeodata-osm-bremen-2018-04-04")
//				.addParameters(nvps)
//				.setHeader(HttpHeaders.ACCEPT, WebContent.contentTypeTurtle)
//				.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip").build();
//		HttpResponse response = client.execute(request);
//		HttpEntity entity = response.getEntity();


	}
	
	// Test request against a data node
	public static void main2(String[] args) throws ClientProtocolException, IOException {
		HttpClient client = HttpClients.custom().build();
		HttpUriRequest request = RequestBuilder.get()
				.setUri("http://localhost:8086/org-linkedgeodata-osm-bremen-2018-04-04")
				.setHeader(HttpHeaders.ACCEPT, WebContent.contentTypeTurtle)
				.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip").build();
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		System.out.println("Content length: " + entity.getContentLength());
		response.getEntity().getContent().close();
	}
}
