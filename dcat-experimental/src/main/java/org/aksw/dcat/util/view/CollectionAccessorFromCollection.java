package org.aksw.dcat.util.view;

import java.util.Collection;

import com.google.common.collect.Range;

public class CollectionAccessorFromCollection<T>
	implements CollectionAccessor<T>
{
	protected Collection<T> delegate;
	protected Range<Long> multiplicity;

	public CollectionAccessorFromCollection(Collection<T> delegate) {
		this(delegate, Range.atLeast(0l)); // 0..* items
	}
	
	public CollectionAccessorFromCollection(Collection<T> delegate, Range<Long> multiplicity) {
		super();
		this.delegate = delegate;
		this.multiplicity = multiplicity;
	}

	@Override
	public Range<Long> getMultiplicity() {
		return multiplicity;
	}

	@Override
	public Collection<T> get() {
		return delegate;
	}
}
