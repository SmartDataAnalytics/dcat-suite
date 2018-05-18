package org.aksw.commons.accessors;

import java.util.function.Function;

public class AccessorSupplierFactoryDelegate<S>
	implements AccessorSupplierFactory<S>
{
	protected AccessorSupplierFactory<S> delegate;

	public AccessorSupplierFactoryDelegate(AccessorSupplierFactory<S> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public <T> Function<S, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz) {
		Function<S, ? extends SingleValuedAccessor<T>> result = delegate.createAccessor(name, clazz);
		return result;
	}
}