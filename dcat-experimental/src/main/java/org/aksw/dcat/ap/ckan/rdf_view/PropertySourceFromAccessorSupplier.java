package org.aksw.dcat.ap.ckan.rdf_view;

import java.util.Objects;
import java.util.function.Function;

import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySource;
import org.aksw.dcat.util.view.SingleValuedAccessor;

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