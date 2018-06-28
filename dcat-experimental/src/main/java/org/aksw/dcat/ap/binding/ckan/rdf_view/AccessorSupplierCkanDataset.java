package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.function.Function;

import org.aksw.commons.accessors.AccessorSupplierFactory;
import org.aksw.commons.accessors.AccessorSupplierFactoryDelegate;
import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.accessors.SingleValuedAccessorFromCollection;

import eu.trentorise.opendata.jackan.model.CkanDataset;

public class AccessorSupplierCkanDataset
	extends AccessorSupplierFactoryDelegate<CkanDataset>
{
	public AccessorSupplierCkanDataset(AccessorSupplierFactory<CkanDataset> delegate) {
		super(delegate);
	}

	@Override
	public <T> Function<CkanDataset, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz) {
		Function<CkanDataset, ? extends SingleValuedAccessor<T>> result;

		String[] parts = name.split("\\:", 2);

		String namespace = parts.length == 2 ? parts[0] : "";
		String localName = parts.length == 2 ? parts[1] : parts[0];

		if(namespace.equals("extra")) {
			// FIXME hack ... need a converter in general
			result = ckanDataset -> (SingleValuedAccessor<T>)new SingleValuedAccessorFromCollection<>(new SetFromCkanExtras(ckanDataset, localName));
		} else {
			result = delegate.createAccessor(localName, clazz);
		}
		
		return result;
	}
}