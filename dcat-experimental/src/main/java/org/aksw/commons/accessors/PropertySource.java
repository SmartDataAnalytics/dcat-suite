package org.aksw.commons.accessors;

import java.util.Collection;

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
	
	
	/**
	 * Retrieve an accessor to a multi valued collection
	 * 
	 * @param name
	 * @param itemType
	 * @return
	 */
	default <T> SingleValuedAccessor<Collection<T>> getCollectionProperty(String name, Class<T> itemType) {
		Object tmp = getProperty(name, Collection.class);
		return (SingleValuedAccessor<Collection<T>>) tmp;
	}
	

	/**
	 * Retrieves a single valued property and wraps it as a set with at most 1 item.
	 * Removing the item from the set is equivalent to setting the property to null.
	 * 
	 * @param name
	 * @param itemType
	 * @return
	 */
//	default <T> SingleValuedAccessor<Collection<T>> getPropertyAsSet(String name, Class<T> itemType) {
//		SingleValuedAccessor<Collection<T>> result =
//				new SingleValuedAccessorDirect<>(
//						new CollectionFromSingleValuedAccessor<>(
//								getProperty(name, itemType)));
//		return result;
//	}
	default <T> CollectionAccessor<T> getPropertyAsSet(String name, Class<T> itemType) {
		SingleValuedAccessor<T> accessor = getProperty(name, itemType);
		if(accessor == null) {
			throw new RuntimeException("No accessor for " + name + " on " + this);
		}
		CollectionAccessor<T> result =
						new CollectionAccessorFromSingleValuedAccessor<>(accessor);
		return result;
	}

}