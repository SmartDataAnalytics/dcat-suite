package org.aksw.commons.accessors;

import java.util.Collection;

import com.google.common.base.Converter;
import com.google.common.collect.Range;

public class CollectionAccessorFromConverter<T, B>
	implements CollectionAccessor<T>
{
	protected final CollectionAccessor<B> delegate;
	protected final Converter<T, B> converter;
	
	public CollectionAccessorFromConverter(CollectionAccessor<B> delegate, Converter<T, B> converter) {
		super();
		this.delegate = delegate;
		this.converter = converter;
	}

	@Override
	public Collection<T> get() {
		Collection<B> tmp = delegate.get();
		CollectionFromConverter<T, B> result = new CollectionFromConverter<>(tmp, converter);
		return result;
		
	}	

	@Override
	public Range<Long> getMultiplicity() {
		Range<Long> result = delegate.getMultiplicity();
		return result;
	}
}
