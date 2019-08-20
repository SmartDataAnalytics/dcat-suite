package org.aksw.dcat_suite.server.conneg;

import java.net.URI;
import java.net.URISyntaxException;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerLookup {
	@Autowired
	protected CatalogResolver catalogResolver;
	
//	@GetMapping("/lookup")
	@RequestMapping(path="/lookup")
	@CrossOrigin(origins = "*")
	public @ResponseBody Object lookup(@RequestParam(required=false) String test) throws URISyntaxException {
		
		System.out.println("Catalog resolver is " + catalogResolver);
		
		System.out.println("Controller invoked");
		Model m = ModelFactory.createDefaultModel();
		
		for(int i = 0; i < 1000; ++i) {
			m.add(m.createResource("http://foo.bar/" + i), RDF.type, RDF.Property);
		}
		
		if(!Strings.isNullOrEmpty(test)) {
			return ResponseEntity.created(new URI("file:///foo/bar")).build();
		}
		
		return m;
	}
}
