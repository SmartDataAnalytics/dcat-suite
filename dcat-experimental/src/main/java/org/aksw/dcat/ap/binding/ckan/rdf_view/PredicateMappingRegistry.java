package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.accessors.PropertySource;
import org.aksw.jena_sparql_api.pseudo_rdf.PseudoRdfProperty;

class PredicateMappingRegistry {
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> predicateMappings;
	
	
	void put(String predicate, Function<PropertySource, PseudoRdfProperty> fn) {
		
	}
	
	Function<PropertySource, PseudoRdfProperty> get(String predicate) {
		return predicateMappings.get(predicate);
	}
}