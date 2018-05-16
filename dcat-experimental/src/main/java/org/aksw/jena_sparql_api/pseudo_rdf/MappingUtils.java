package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Map;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

public class MappingUtils {
	public static Map<String, Object> getMappingRegistry() {
		Map<String, Object> result = null;
		return result;
	}
	
	public static void applyMappingDefaults(Resource r) {
		// If the resource does not have a concrete mapping type, add String
		if(!r.hasProperty(RDF.type)) {
			r.addProperty(RDF.type, MappingVocab.LiteralMapping);
		}
		
		// If the literal mapping is without type, apply xsd:string
		if(r.hasProperty(RDF.type, MappingVocab.LiteralMapping) || r.hasProperty(RDF.type, MappingVocab.CollectionMapping)) {
			if(!r.hasProperty(MappingVocab.type)) {
				r.addProperty(MappingVocab.type, XSD.xstring);
			}
		}		
	}
}