package org.aksw.dcat.server.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.dcat_suite.server.conneg.torename.HttpResourceRepositoryFromFileSystem;
import org.aksw.dcat_suite.server.conneg.torename.HttpResourceRepositoryManagerImpl;
import org.aksw.dcat_suite.server.conneg.torename.RdfEntityInfo;
import org.aksw.dcat_suite.server.conneg.torename.RdfHttpEntityFile;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.message.BasicHeader;
import org.glassfish.jersey.internal.guava.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;



@RestController
public class ControllerLookup {
	private static final Logger logger = LoggerFactory.getLogger(ControllerLookup.class);
	
	@Autowired
	protected CatalogResolver catalogResolver;
	
	@Autowired
	protected HttpResourceRepositoryFromFileSystem datasetRepository;
	
	public static Stream<Entry<String, String>> flattenHeaders(HttpHeaders springHeaders) {
		Stream<Entry<String, String>> result = springHeaders.entrySet().stream()
				.flatMap(e ->
					e.getValue().stream()
					.map(v -> Maps.immutableEntry(e.getKey(), v)));
		return result;
	}
	
	public static Header[] springToApache(HttpHeaders springHeaders) {
		List<Header> apacheHeaders = new ArrayList<>();
		for(Entry<String, List<String>> e : springHeaders.entrySet()) {
			String k = e.getKey();
			for(String v : e.getValue()) {
				Header h = new BasicHeader(k, v);
				apacheHeaders.add(h);
			}
		}
		
		Header[] result = apacheHeaders.toArray(new Header[0]);
		return result;
	}

	// https://stackoverflow.com/questions/4542489/match-the-rest-of-the-url-using-spring-3-requestmapping-annotation/11248730#11248730
	@RequestMapping(path="/")
	@CrossOrigin(origins = "*")
	//@PathVariable String id, 
	public @ResponseBody Object lookup(@RequestParam("id") String id, @RequestHeader HttpHeaders springHeaders)
			throws Exception {
		Object result = processRequest(id, springHeaders);
		return result;
	}

	// https://stackoverflow.com/questions/4542489/match-the-rest-of-the-url-using-spring-3-requestmapping-annotation/11248730#11248730
	@RequestMapping(path="/**")
	@CrossOrigin(origins = "*")
	//@PathVariable String id, 
	public @ResponseBody Object lookup(HttpServletRequest httpRequest, @RequestHeader HttpHeaders springHeaders)
			throws Exception {
		String rawId = (String)httpRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String id = rawId.replaceAll("^/+", "");

		Object result = processRequest(id, springHeaders);
		return result;		
	}
		
	
	public Object processRequest(String id, HttpHeaders springHeaders) throws Exception {
		logger.info("Got request for " + id);
		
		ResponseEntity<?> result;

		DatasetResolver datasetResolver = catalogResolver.resolveDataset(id)
				.blockingGet();
		
		List<DistributionResolver> dists = datasetResolver.resolveDistributions().toList().blockingGet();
		
		if(!dists.isEmpty()) {
			DistributionResolver dist = dists.iterator().next();
			String downloadUrl = dist.getDistribution().getDownloadURL();
			
			if(downloadUrl != null) {
				//Header[] apacheHeaders = springToApache(springHeaders);
				
				HttpUriRequest request = RequestBuilder
						.get(downloadUrl)
						.build();
				
				List<Entry<String, String>> tmp = flattenHeaders(springHeaders).collect(Collectors.toList());
				for(Entry<String, String> e : tmp) {
					String k = e.getKey();
					String v = e.getValue();
					request.addHeader(k, v);
				}
				
				RdfHttpEntityFile entity = datasetRepository.get(request, HttpResourceRepositoryManagerImpl::resolveRequest);
				
				if(entity == null) {
					throw new RuntimeException("Should not happen");
				}
				
//https://stackoverflow.com/questions/20333394/return-a-stream-with-spring-mvcs-responseentity
//				InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
//				httpHeaders.setContentLength(contentLengthOfStream);
//				return new ResponseEntity(inputStreamResource, httpHeaders, HttpStatus.OK);
						
				Path path = entity.getAbsolutePath();
				long size = Files.size(path);

				RdfEntityInfo info = entity.getCombinedInfo().as(RdfEntityInfo.class);
				
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentLength(size);
				responseHeaders.setContentType(MediaType.valueOf(info.getContentType()));
				responseHeaders.set(HttpHeaders.CONTENT_ENCODING, info.getEncodingsAsHttpHeader());				

				result = ResponseEntity.ok()
					.headers(responseHeaders)
					.body(new InputStreamResource(Files.newInputStream(path, StandardOpenOption.READ)));
				
			} else {
				throw new RuntimeException("Dataset has no suitablbe distribution; dataset id=" + id);
			}
		} else {
			throw new RuntimeException("No dataset found for id=" + id);			
		}

		return result;
	}
	
	
//	public static void deleteme() {
//
//		List<MediaType> rdfFamily = HttpMessageConverterModel.supportedMediaTypes();
//		MediaType requested = headers.getContentType();
//		
//		List<MediaType> matches = rdfFamily.stream()
//				.filter(mt -> mt.includes(requested))
//				.collect(Collectors.toList());
//		
//		// If any type is in the family, proceed
//		
//		if(!matches.isEmpty()) {
//			
//			// Forward the request with all types, 
//			
//			List<MediaType> reorderedMt = Streams.concat(
//					matches.stream(),
//					rdfFamily.stream().filter(x -> !matches.contains(x)))
//					.collect(Collectors.toList());
//			
//			// 
//			String downloadUrl = "foobar";
//			
//					
//		}
//		
//		
//		// Check whether the request's content type is within the
//		// a family of content types X
//		// If so, forward the request to X using *all* content types in X
//		//   - here we assume, we can convert between all formats of X
//		//   - priorities are affected (in order):
//		//     - global configuration (always forward requests to content type FOO as requests to BAR)
//		//     - original request (prefer the requested format)
//		
//		// e.g. X := triple-based RDF content types
//		// Issue: Can the case arise, where a content type lies within multiple families?
//		
//		//RDFLanguages.getRegisteredLanguages()
//		
//		
//		// If so, forward the request for any RDF format, with the following priorities
//		// - data node global override: data node can be configured to perform requests always with certain content types
//		// -
//		
//		
//		logger.debug("Got request to resolve id: " + id);
//		Objects.requireNonNull(id, "Dataset identifier must not be null");
//		DatasetResolver datasetResolver = catalogResolver.resolveDataset(id).blockingGet();
//
//		// TODO I suppose here we simply want the list of URLs we need to retrieve
//		// and then we pass them on to some download manager
//		
//		
////		Flowable<> foo = datasetResolver.resolveDistributions()
////				.flatMap(d -> d.re)
//		
//		
//		System.out.println("Resolved argument to " + datasetResolver);
//		
//		
//		System.out.println("Catalog resolver is " + catalogResolver);
//		
//		System.out.println("Controller invoked");
//		Model m = ModelFactory.createDefaultModel();
//		
//		for(int i = 0; i < 1000; ++i) {
//			m.add(m.createResource("http://foo.bar/" + i), RDF.type, RDF.Property);
//		}
//		
//		if(!Strings.isNullOrEmpty(id)) {
//			return ResponseEntity.created(new URI("file:///foo/bar")).build();
//		}
//		
//		return m;
//
//	}
}
