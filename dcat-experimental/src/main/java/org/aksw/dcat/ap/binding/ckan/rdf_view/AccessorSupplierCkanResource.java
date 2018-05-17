package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.accessors.AccessorSupplierFactory;
import org.aksw.commons.accessors.AccessorSupplierFactoryDelegate;
import org.aksw.commons.accessors.LazyMap;
import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.accessors.SingleValuedAccessorFromMap;
import org.aksw.commons.accessors.SingleValuedAccessorImpl;

import eu.trentorise.opendata.jackan.model.CkanResource;

class AccessorSupplierCkanResource
	extends AccessorSupplierFactoryDelegate<CkanResource>
{
	public AccessorSupplierCkanResource(AccessorSupplierFactory<CkanResource> delegate) {
		super(delegate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Function<CkanResource, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz) {
		Function<CkanResource, ? extends SingleValuedAccessor<T>> result;
	
		String[] parts = name.split("\\:", 2);
	
		String namespace = parts.length == 2 ? parts[0] : "";
		String localName = parts.length == 2 ? parts[1] : parts[0];
	
		if(namespace.equals("others")) {
			// FIXME hack ... need a converter in general
//			result = null; //(SingleValuedAccessor<T>)new SingleValuedAccessorFromSet<>(new SetFromCkanExtras(ckanResource, localName));
			result = ckanResource -> (SingleValuedAccessor<T>)new SingleValuedAccessorFromMap<>(
					new LazyMap<>(
							new SingleValuedAccessorImpl<>(ckanResource::getOthers, ckanResource::setOthers), HashMap::new),
							localName);

//			CkanResource x = new CkanResource();
//			result.apply(x).set((T)"http://foobar");
//			System.out.println(x.getOthers());
			
			
		} else {
			result = delegate.createAccessor(localName, clazz);
		}
		
		return result;
	}
}