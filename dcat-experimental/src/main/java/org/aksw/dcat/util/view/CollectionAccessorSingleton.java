package org.aksw.dcat.util.view;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Range;

/**
 * An accessor that refers to an immutable collection.
 * 
 * @author raven May 2, 2018
 *
 * @param <T>
 */
public class CollectionAccessorSingleton<T>
	implements CollectionAccessor<T>
{
	protected Collection<T> values;
	
	protected transient Collection<T> unmodifiableValuesView;
	protected transient Range<Long> multiplicity;

	public CollectionAccessorSingleton(T value) {
		this(Collections.singleton(value));
	}

	public CollectionAccessorSingleton(Collection<T> values) {
		super();
		this.values = values;
		this.unmodifiableValuesView = Collections.unmodifiableCollection(values);
		this.multiplicity = Range.singleton((long)values.size());
	}

	@Override
	public Collection<T> get() {
		return unmodifiableValuesView;
	}
	
	@Override
	public Range<Long> getMultiplicity() {
		return multiplicity;
	}
}
