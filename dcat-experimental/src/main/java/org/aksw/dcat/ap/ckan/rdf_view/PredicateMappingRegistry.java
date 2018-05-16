package org.aksw.dcat.ap.ckan.rdf_view;

import java.util.Map;
import java.util.function.Function;

import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySource;
import org.aksw.jena_sparql_api.pseudo_rdf.PseudoRdfProperty;

class PredicateMappingRegistry {
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> predicateMappings;
	
	
	void put(String predicate, Function<PropertySource, PseudoRdfProperty> fn) {
		
	}
	
	Function<PropertySource, PseudoRdfProperty> get(String predicate) {
		return predicateMappings.get(predicate);
	}
}