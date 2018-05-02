package org.aksw.dcat.util.view;

import java.util.Collection;

import org.aksw.dcat.ap.playground.main.CollectionFromSingleValuedAccessor;

import com.google.common.collect.Range;

public class CollectionAccessorFromSingleValuedAccessor<T>
	implements CollectionAccessor<T>
{
	protected final SingleValuedAccessor<T> delegate;
	protected transient Collection<T> delegateCollectionView;
	protected final Range<Long> multiplicity = Range.closed(0l, 1l);
	
	public CollectionAccessorFromSingleValuedAccessor(SingleValuedAccessor<T> delegate) {
		super();
		this.delegate = delegate;
		this.delegateCollectionView = new CollectionFromSingleValuedAccessor<>(delegate);
	}

	@Override
	public Collection<T> get() {
		return delegateCollectionView;
	}
	
	@Override
	public Range<Long> getMultiplicity() {
		return multiplicity;
	}
}
