package org.aksw.dcat_suite.server.conneg;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.ckan_deploy.core.PathCoder;
import org.aksw.ckan_deploy.core.PathEncoderRegistry;
import org.aksw.dcat.repo.impl.fs.CatalogResolverFilesystem;
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
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;
import com.google.common.net.MediaType;

import io.reactivex.Single;



interface HttpAssetManager {
	/**
	 * Retrieve an HttpEntity from the asset mangaer
	 * 
	 * @author raven
	 *
	 */
	CompletableFuture<HttpEntity> get(HttpRequest request);
	CompletableFuture<?> put(String baseName, Supplier<? extends HttpEntity> supplier);	
}

/**
 * A bundle of two maps, with the purpose of denoting a primary association between values of K and V
 * while also allowing for relating Vs to Ks.
 * 
 * Use case:
 * The primary file extension for content type 'application/turtle' is 'ttl',
 * However, the file extension 'turtle' is understood as an alternative of that content type.
 * In consequence: writes makes use of the primary file extension,
 * whereas reads can make use of the alternative ones.
 * 
 * 
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
class MapPair<K, V> {
	protected Map<K, V> primary = new LinkedHashMap<>();
	protected Map<V, K> alternatives = new LinkedHashMap<>();
	
	public MapPair() {
	}

	public Map<K, V> getPrimary() {
		return primary;
	}

	public Map<V, K> getAlternatives() {
		return alternatives;
	}
	
	/**
	 * Convenience method to set a primary mapping and its reverse mapping
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public MapPair<K, V> putPrimary(K key, V value) {
		primary.put(key, value);
		alternatives.put(value, key);
		
		return this;
	}
}

/**
 * Custom subclass in order to allow for clean access to the file.
 * (IMO FileEntity should already provide that getter)
 * 
 * @author raven
 *
 */
class FileEntityEx
	extends FileEntity
{
	public FileEntityEx(File file, ContentType contentType) {
		super(file, contentType);
	}

	public FileEntityEx(File file) {
		super(file);
	}

	public File getFile() {
		return file;
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
	protected Path basePath;
	
	public HttpAssetManagerFromPath(Path basePath) {
		this.basePath = basePath;
		
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
	
	
	public static float qValueOf(HeaderElement h) {
		float result = Optional.ofNullable(h.getParameterByName("q"))
						.map(NameValuePair::getValue)
						.map(Float::parseFloat)
						.orElse(1.0f);
		return result;
	}

	public Map<String, Float> getOrderedValues(Header[] headers, String name) {
		Map<String, Float> result = Arrays.asList(headers).stream()
			.filter(h -> h.getName().equalsIgnoreCase(name))
			.flatMap(h -> Arrays.asList(h.getElements()).stream())
			.collect(Collectors.toMap(e -> e.getName(), e -> qValueOf(e)));
		return result;
	}
	
	
	public static List<MediaType> supportedMediaTypes() {
		return supportedMediaTypes(RDFLanguages.getRegisteredLanguages());
	}
	
	public static List<MediaType> supportedMediaTypes(Collection<Lang> langs) {
		List<MediaType> types = langs.stream()
				// Models can surely be served using based languages
				// TODO but what about quad based formats? I guess its fine to serve a quad based dataset
				// with only a default graph
				//.filter(RDFLanguages::isTriples)
				.flatMap(lang -> Stream.concat(
						Stream.of(lang.getContentType().getContentType()),
						lang.getAltContentTypes().stream()))
				.map(MediaType::parse)
				.collect(Collectors.toList());
		return types;
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
	public FileEntityEx get(HttpRequest request, Function<HttpRequest, HttpEntity> executor) throws IOException {
		Header[] headers = request.getAllHeaders();		
		
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
			String contentType = null;
			List<String> codings = new ArrayList<>();
			
			String fileName = cand.getFileName().toString();

			String current = fileName;
			
			while(true) {
				String ext = com.google.common.io.Files.getFileExtension(current);
				
				if(ext == null) {
					break;
				}
				
				// Classify the extension - once it maps to a content type, we are done
				String coding = codingExtensions.getAlternatives().get(ext);
				String ct = ctExtensions.getAlternatives().get(ext);
	
				// Prior validation of the maps should ensure that at no point a file extension
				// denotes both a content type and a coding
				assert !(coding != null && ct != null) :
					"File extension conflict: '" + ext + "' maps to " + coding + " and " + ct;  
				
				if(coding != null) {
					codings.add(coding);
				}
				
				if(ct != null) {
					contentType = ct;//MediaType.parse(ct);
					break;
				}
				
				// both coding and ct were null - skip
				if(coding == null && ct == null) {
					break;
				}
				
				current = com.google.common.io.Files.getNameWithoutExtension(current);//current.substring(0, current.length() - ext.length());
			}
			

			if(contentType != null) {
				File file = cand.toFile();
				FileEntityEx fileEntity = new FileEntityEx(file);
				fileEntity.setContentType(contentType);
				if(codings.isEmpty()) {
					// nothing to do
				} else {
					String str = codings.stream().collect(Collectors.joining(","));
					fileEntity.setContentEncoding(str);
				}

				fileEntities.add(fileEntity);
			}
		}

		
		// TODO Find best candidate among the file entities
	
		
		
		Map<FileEntityEx, Float> entityToScore = new HashMap<>();
		for(FileEntityEx fe : fileEntities) {
			MediaType mt = MediaType.parse(fe.getContentType().getValue());
			
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
		
		return result;
	}

	public  String toFileExtension(List<String> codings) {
		List<String> parts = new ArrayList<>(codings.size());
		
		for(String coding : codings) {
			String part = Objects.requireNonNull(codingExtensions.getPrimary().get(coding));
			parts.add(part);
		}
		
		String result = parts.stream().collect(Collectors.joining("."));
		
		result = result.isEmpty() ? result : "." + result;
		return result;
	}

	public  String toFileExtension(String contentType, List<String> codings) {
		List<String> parts = new ArrayList<>(1 + codings.size());
		
		String part = Objects.requireNonNull(ctExtensions.getPrimary().get(contentType));
		parts.add(part);
		
		for(String coding : codings) {
			part = Objects.requireNonNull(codingExtensions.getPrimary().get(coding));
			parts.add(part);
		}
		
		String result = parts.stream().collect(Collectors.joining("."));
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
	
	/**
	 * Provide a plan that converts the given source file entity to a file in the target
	 * with the given content type and encoding 
	 * 
	 * @param source
	 * @param contentType
	 * @param encodings
	 * @return
	 * @throws Exception 
	 */
	public CompletableFuture<FileEntityEx> convert(FileEntityEx source, Path rawTgtBasePath, String tgtContentType, List<String> tgtEncodings) throws Exception {
		// Decode 
		PathEncoderRegistry coders = PathEncoderRegistry.get();
		
//		String srcContentType = source.getContentType().getElements()[0].get
		String srcContentType = Arrays.asList(source.getContentType().getElements()).stream()
				.map(HeaderElement::getName)
				.findFirst()
				.orElseThrow(() -> new RuntimeException("No content type on file entity: " + source));

		Path tgtBasePath = rawTgtBasePath.getParent();
		String tgtBaseName = rawTgtBasePath.getFileName().toString();

		String srcExt = ctExtensions.getPrimary().get(srcContentType);;
		String decodeBaseName = tgtBaseName + "." + srcExt;
		
		String tgtExt = ctExtensions.getPrimary().get(tgtContentType);;
		String encodeBaseName = tgtBaseName + "." + tgtExt;
		
		
		//String baseNameCt = baseName + "." + srcBaseName;
		
		List<String> srcEncodings = Optional
					.ofNullable(source.getContentEncoding())
					.map(Header::getElements)
					.map(Arrays::asList)
					.orElse(Collections.emptyList()).stream()					
				.map(HeaderElement::getName)
				.collect(Collectors.toList());

		// Find out how many encodings are the same from the start of the lists
		int offset = 0;
		int min = Math.min(srcEncodings.size(), tgtEncodings.size());
		for(int i = 0; i < min; ++i) {
			if(srcEncodings.get(i) == tgtEncodings.get(i)) {
				++offset; // == i + 1
			} else {
				break;
			}
		}		
		
		List<TransformStep> plan = new ArrayList<>(); 

		// Decode up to the final offset of the common path head
		for(int i = srcEncodings.size() - 1; i >= offset; --i) {
			String srcEncoding = srcEncodings.get(i);
			PathCoder coder = coders.getCoder(srcEncoding);
			if(coder == null) {
				throw new RuntimeException("No coder found for " + srcEncoding);
			}

	
			String suffix = toFileExtension(srcEncodings.subList(0, i));
			Path fullName = tgtBasePath.resolve(decodeBaseName + suffix);

			//String fullName = srcBaseName + suffix;
//			String fileExtension = codingExtensions.getPrimary().get(srcEncoding);
			TransformStep step = createTransformStep(fullName, coder::decode);
			plan.add(step);
		}

		Lang srcLang = RDFLanguages.nameToLang(srcContentType);

		Lang tgtLang = RDFLanguages.nameToLang(tgtContentType);
		
		// Perform content type conversion
		BiFunction<Path, Path, Single<Integer>> convert = (src, tgt) -> {
			
			Model m = ModelFactory.createDefaultModel();
			try {
				RDFDataMgr.read(m, Files.newInputStream(src, StandardOpenOption.READ), srcLang);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
	
			try(OutputStream out = Files.newOutputStream(tgt,
					StandardOpenOption.CREATE,
					StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				RDFDataMgr.write(out, m, tgtLang);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return Single.just(0);
		};
		
		{
			TransformStep step = createTransformStep(tgtBasePath.resolve(encodeBaseName), convert);
			plan.add(step);
		}
		
		// Encode from the final offset of the common path head
		for(int i = offset; i < tgtEncodings.size(); ++i) {
			String tgtEncoding = tgtEncodings.get(i);
			PathCoder coder = coders.getCoder(tgtEncoding);
			if(coder == null) {
				throw new RuntimeException("No coder found for " + tgtEncoding);
			}

			String suffix = toFileExtension(tgtEncodings.subList(0, i + 1));
			Path fullName = tgtBasePath.resolve(encodeBaseName + suffix);

//			String fileExtension = codingExtensions.getPrimary().get(srcEncoding);
			TransformStep step = createTransformStep(fullName, coder::encode);
			plan.add(step);

//			plan.add(coder::encode);
		}

		
		File file = source.getFile();
		Path src = Paths.get(file.toURI());
		Path dest;

		// Execute the plan
		for(TransformStep step : plan) {
			dest = step.destPath;
			logger.info("Creating " + dest + " from " + src);
			step.method.apply(src, dest)
				.blockingGet();

			src = dest;
		}

		return null;
	}
	
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
		PathCoder coder = PathEncoderRegistry.get().getCoder("bzip");
		
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
		
		FileEntityEx fe = m.get(r, null);
		
		System.out.println("Source file entity: " + fe.getFile());
		m.convert(fe, Paths.get("/tmp/yay"), WebContent.contentTypeRDFXML, Arrays.asList("gzip"));
		
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
