package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.Objects;
import java.util.function.Function;

import org.aksw.commons.accessors.AccessorSupplierFactory;
import org.aksw.commons.accessors.PropertySource;
import org.aksw.commons.accessors.SingleValuedAccessor;

class PropertySourceFromAccessorSupplier<S>
	implements PropertySource
{
	protected S source;
	protected AccessorSupplierFactory<S> accessorSupplierFactory;
	//protected Table<String, Class<?>, >
	
	public PropertySourceFromAccessorSupplier(S source, AccessorSupplierFactory<S> accessorSupplierFactory) {
		super();
		Objects.requireNonNull(source);
		Objects.requireNonNull(accessorSupplierFactory);
		
		this.source = source;
		this.accessorSupplierFactory = accessorSupplierFactory;
	}

	@Override
	public S getSource() {
		return source;
	}

	@Override
	public <T> SingleValuedAccessor<T> getProperty(String name, Class<T> valueType) {
		Function<S, ? extends SingleValuedAccessor<T>> accessorSupplier = accessorSupplierFactory.createAccessor(name, valueType);
		Objects.requireNonNull(accessorSupplier, "Could not obtain an access supplier for attribute '" + name + "' of type " + valueType + " in " + accessorSupplierFactory);
		SingleValuedAccessor<T> result = accessorSupplier.apply(source);

		return result;
	}
	
}