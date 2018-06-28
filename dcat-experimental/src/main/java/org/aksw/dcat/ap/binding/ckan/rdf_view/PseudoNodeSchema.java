package org.aksw.dcat.ap.binding.ckan.rdf_view;

import org.aksw.commons.accessors.AccessorSupplierFactory;

interface PseudoNodeSchema<S> {
	Class<S> getEntityClass();
	AccessorSupplierFactory<S> getAccessorSupplierFactory(); 		
	PredicateMappingRegistry getPredicateMappings();

}