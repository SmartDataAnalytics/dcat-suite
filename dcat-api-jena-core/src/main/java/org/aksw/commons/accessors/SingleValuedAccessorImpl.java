package org.aksw.commons.accessors;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SingleValuedAccessorImpl<T>
	implements SingleValuedAccessor<T>
{
	protected Supplier<T> getter;
	protected Consumer<T> setter;
	
	public SingleValuedAccessorImpl(Supplier<T> getter, Consumer<T> setter) {
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public T get() {
		T result = getter.get();
		return result;
	}

	@Override
	public void set(T value) {
		setter.accept(value);
	}
}
