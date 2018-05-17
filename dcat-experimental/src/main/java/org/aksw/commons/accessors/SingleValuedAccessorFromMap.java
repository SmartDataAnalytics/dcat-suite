package org.aksw.commons.accessors;

import java.util.Map;

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
public class SingleValuedAccessorFromMap<K, T>
	implements SingleValuedAccessor<T> {

	protected Map<K, T> map;
	protected K key;
	
	// Only clear the underlying when adding a null, but do not add the null itself
	protected boolean insertNulls;

	public SingleValuedAccessorFromMap(Map<K, T> map, K key) {
		this(map, key, false);
	}

	public SingleValuedAccessorFromMap(Map<K, T> map, K key, boolean insertNulls) {
		super();
		this.map = map;
		this.key = key;
		this.insertNulls = insertNulls;
	}

	@Override
	public T get() {
		T result = map.get(key);
		return result;
	}

	@Override
	public void set(T value) {
		if(value == null && ! insertNulls) {
			map.remove(key);
		} else {
			map.put(key, value);
		}
	}
}