package org.aksw.dcat.util.view;

import java.util.Set;

/**
 * This class is to be intended with set views
 * 
 * new SingleValuedAccessorFromSet<T>(new SetFromMappedPropertyValues<T>(...))
 * 
 * @author raven Apr 9, 2018
 *
 * @param <T>
 */
public class SingleValuedAccessorFromSet<T>
	implements SingleValuedAccessor<T> {

	protected Set<T> set;

	// Only clear the underlying when adding a null, but do not add the null itself
	protected boolean insertNulls;

	public SingleValuedAccessorFromSet(Set<T> set) {
		this(set, false);
	}

	public SingleValuedAccessorFromSet(Set<T> set, boolean insertNulls) {
		super();
		this.set = set;
		this.insertNulls = insertNulls;
	}

	@Override
	public T get() {
		T result = set.isEmpty() ? null : set.iterator().next();
		return result;
	}

	@Override
	public void set(T value) {
		set.clear();
		if(value != null || insertNulls) {
			set.add(value);
		}
	}
}