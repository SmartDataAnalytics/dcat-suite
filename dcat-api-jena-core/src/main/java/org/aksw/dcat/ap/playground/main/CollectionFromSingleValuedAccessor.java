package org.aksw.dcat.ap.playground.main;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Objects;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.collections.SinglePrefetchIterator;

public class CollectionFromSingleValuedAccessor<T>
	extends AbstractCollection<T>
{
	protected SingleValuedAccessor<T> accessor;

	public CollectionFromSingleValuedAccessor(SingleValuedAccessor<T> accessor) {
		super();
		Objects.requireNonNull(accessor);
		this.accessor = accessor;
	}

	@Override
	public boolean add(T e) {
		Objects.requireNonNull(e);

		T value = accessor.get();
		// Added value must be reference equal
		if(value != null && value != e) {
			throw new RuntimeException("Cannot add because a value already exists: " + value);
		}
		accessor.set(e);
		return true;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new SinglePrefetchIterator<T>() {
			T value = accessor.get();
			int emitted = 0;
			
			@Override
			protected T prefetch() throws Exception {
				
				return emitted++ != 0 || value == null ? finish() : value;
			}
			
			@Override
			protected void doRemove() {
				accessor.set(null);
			}
		};		
	}

	@Override
	public int size() {
		T value = accessor.get();
		int result = value == null ? 0 : 1;
		return result;
	}
}