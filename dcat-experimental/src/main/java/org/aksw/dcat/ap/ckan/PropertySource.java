package org.aksw.dcat.ap.ckan;

import org.aksw.dcat.util.view.SingleValuedAccessor;

/**
 * A simple interface to resolve properties by name, value type and value multiplicity (single or multi).
 * 
 * 
 * 
 * @author raven Apr 10, 2018
 *
 */
public interface PropertySource {
	<T> SingleValuedAccessor<T> getProperty(String name, Class<T> valueType);
}