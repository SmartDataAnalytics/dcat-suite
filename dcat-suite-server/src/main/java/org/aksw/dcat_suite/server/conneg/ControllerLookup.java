package org.aksw.dcat_suite.server.conneg;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerLookup {
//	@GetMapping("/lookup")
	@RequestMapping(path="/lookup")
	@CrossOrigin(origins = "*")
	public @ResponseBody Model lookup() {
		System.out.println("Controller invoked");
		Model m = ModelFactory.createDefaultModel();
		
		for(int i = 0; i < 1000; ++i) {
			m.add(m.createResource("http://foo.bar/" + i), RDF.type, RDF.Property);
		}
		
		return m;
	}
}
