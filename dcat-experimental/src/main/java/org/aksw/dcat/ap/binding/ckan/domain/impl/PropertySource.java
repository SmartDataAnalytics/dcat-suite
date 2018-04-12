package org.aksw.dcat.ap.binding.ckan.domain.impl;

import java.util.Collection;

import org.aksw.dcat.util.view.SingleValuedAccessor;

/**
 * This class is similar to the spring PropertySource - except that it allows setting properties as well.
 * A simple interface to resolve properties by name, value type and value multiplicity (single or multi).
 * 
 * 
 * 
 * @author raven Apr 10, 2018
 *
 */
public interface PropertySource {
	Object getSource();
	
	<T> SingleValuedAccessor<T> getProperty(String name, Class<T> valueType);
	
	
	default <T> SingleValuedAccessor<Collection<T>> getCollectionProperty(String name, Class<T> itemType) {
		Object tmp = getProperty(name, Collection.class);
		return (SingleValuedAccessor<Collection<T>>) tmp;
	}
}