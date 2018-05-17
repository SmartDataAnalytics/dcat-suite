package org.aksw.commons.accessors;

import com.google.common.base.Converter;

public interface SingleValuedAccessor<T> {
	T get();
	void set(T value);
	
	default <U> SingleValuedAccessor<U> convert(Converter<T, U> converter) {
		return new SingleValuedAccessorConverter<>(this, converter);
	}
}
