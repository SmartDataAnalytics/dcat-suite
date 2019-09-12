package org.aksw.dcat_suite.server.conneg;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;

@RestController
public class ControllerLookup {
	private static final Logger logger = LoggerFactory.getLogger(ControllerLookup.class);
	
	@Autowired
	protected CatalogResolver catalogResolver;
	
	@RequestMapping(path="/{id}")
	@CrossOrigin(origins = "*")
	public @ResponseBody Object lookup(@PathVariable String id, @RequestHeader HttpHeaders headers) throws URISyntaxException {

		
		List<MediaType> rdfFamily = HttpMessageConverterModel.supportedMediaTypes();
		MediaType requested = headers.getContentType();
		
		List<MediaType> matches = rdfFamily.stream()
				.filter(mt -> mt.includes(requested))
				.collect(Collectors.toList());
		
		// If any type is in the family, proceed
		
		if(!matches.isEmpty()) {
			
			// Forward the request with all types, 
			
			List<MediaType> reorderedMt = Streams.concat(
					matches.stream(),
					rdfFamily.stream().filter(x -> !matches.contains(x)))
					.collect(Collectors.toList());
			
			// 
			String downloadUrl = "foobar";
			
					
		}
		
		
		// Check whether the request's content type is within the
		// a family of content types X
		// If so, forward the request to X using *all* content types in X
		//   - here we assume, we can convert between all formats of X
		//   - priorities are affected (in order):
		//     - global configuration (always forward requests to content type FOO as requests to BAR)
		//     - original request (prefer the requested format)
		
		// e.g. X := triple-based RDF content types
		// Issue: Can the case arise, where a content type lies within multiple families?
		
		//RDFLanguages.getRegisteredLanguages()
		
		
		// If so, forward the request for any RDF format, with the following priorities
		// - data node global override: data node can be configured to perform requests always with certain content types
		// -
		
		
		logger.debug("Got request to resolve id: " + id);
		Objects.requireNonNull(id, "Dataset identifier must not be null");
		DatasetResolver datasetResolver = catalogResolver.resolveDataset(id).blockingGet();

		// TODO I suppose here we simply want the list of URLs we need to retrieve
		// and then we pass them on to some download manager
		
		
//		Flowable<> foo = datasetResolver.resolveDistributions()
//				.flatMap(d -> d.re)
		
		
		System.out.println("Resolved argument to " + datasetResolver);
		
		
		System.out.println("Catalog resolver is " + catalogResolver);
		
		System.out.println("Controller invoked");
		Model m = ModelFactory.createDefaultModel();
		
		for(int i = 0; i < 1000; ++i) {
			m.add(m.createResource("http://foo.bar/" + i), RDF.type, RDF.Property);
		}
		
		if(!Strings.isNullOrEmpty(id)) {
			return ResponseEntity.created(new URI("file:///foo/bar")).build();
		}
		
		return m;
	}
}
