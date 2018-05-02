package org.aksw.dcat.util.view;

import java.util.Collection;

import org.aksw.dcat.ap.playground.main.CollectionFromSingleValuedAccessor;

import com.google.common.collect.Range;

public class CollectionAccessorFromCollectionValue<T>
	implements CollectionAccessor<T>
{
	protected final SingleValuedAccessor<Collection<T>> delegate;
	protected final Range<Long> multiplicity = Range.atLeast(0l);
	
	public CollectionAccessorFromCollectionValue(SingleValuedAccessor<Collection<T>> delegate) {
		super();
		this.delegate = delegate;
		//this.delegateCollectionView = new CollectionFromSingleValuedAccessor<>(delegate);
	}

	@Override
	public Collection<T> get() {
		Collection<T> result = delegate.get();
		return result;
	}
	
	@Override
	public Range<Long> getMultiplicity() {
		return multiplicity;
	}
}
