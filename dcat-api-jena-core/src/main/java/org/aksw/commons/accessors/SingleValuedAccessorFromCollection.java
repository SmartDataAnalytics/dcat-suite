package org.aksw.commons.accessors;

import java.util.Collection;

/**
 * This accessor treats the collection as the source for a single item that can be get or set.
 * This class is intended to be with collection views.
 * 
 * new SingleValuedAccessorFromCollection<T>(new SetFromMappedPropertyValues<T>(...))
 * 
 * @author raven Apr 9, 2018
 *
 * @param <T>
 */
public class SingleValuedAccessorFromCollection<T>
	implements SingleValuedAccessor<T> {

	protected Collection<T> collection;

	// Only clear the underlying when adding a null, but do not add the null itself
	protected boolean insertNulls;

	public SingleValuedAccessorFromCollection(Collection<T> collection) {
		this(collection, false);
	}

	public SingleValuedAccessorFromCollection(Collection<T> set, boolean insertNulls) {
		super();
		this.collection = set;
		this.insertNulls = insertNulls;
	}

	@Override
	public T get() {
		T result = collection.isEmpty() ? null : collection.iterator().next();
		return result;
	}

	@Override
	public void set(T value) {
		collection.clear();
		if(value != null || insertNulls) {
			collection.add(value);
		}
	}
}