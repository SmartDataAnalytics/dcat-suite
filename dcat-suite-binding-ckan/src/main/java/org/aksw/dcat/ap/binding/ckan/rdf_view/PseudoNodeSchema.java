package org.aksw.dcat.ap.binding.ckan.rdf_view;

import org.aksw.commons.accessors.AccessorSupplierFactory;

/**
 * A schematic factory to create accessors on objects of type S.
 * 
 * @author raven
 *
 * @param <S>
 */
interface PseudoNodeSchema<S> {
	Class<S> getEntityClass();
	AccessorSupplierFactory<S> getAccessorSupplierFactory(); 		
	PredicateMappingRegistry getPredicateMappings();
}