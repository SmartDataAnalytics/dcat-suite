package org.aksw.commons.accessors;

import com.google.common.base.Converter;

public class SingleValuedAccessorConverter<T, U>
	implements SingleValuedAccessor<U>
{
	protected SingleValuedAccessor<T> source;
	protected Converter<T, U> converter;

	public SingleValuedAccessorConverter(SingleValuedAccessor<T> source, Converter<T, U> converter) {
		super();
		this.source = source;
		this.converter = converter;
	}

	@Override
	public U get() {
		T tmp = source.get();
		U result = converter.convert(tmp);
		return result;
	}

	@Override
	public void set(U value) {
		T tmp = converter.reverse().convert(value);
		source.set(tmp);
	}

	@Override
	public String toString() {
		return "SingleValuedAccessorConverter [source=" + source + ", converter=" + converter + "]";
	}
}
