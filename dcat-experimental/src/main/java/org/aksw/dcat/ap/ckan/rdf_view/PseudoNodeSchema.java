package org.aksw.dcat.ap.ckan.rdf_view;

interface PseudoNodeSchema<S> {
	Class<S> getEntityClass();
	AccessorSupplierFactory<S> getAccessorSupplierFactory(); 		
	PredicateMappingRegistry getPredicateMappings();

}