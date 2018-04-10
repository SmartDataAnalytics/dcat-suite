package org.aksw.dcat.ap.binding.ckan.domain.impl;

import org.aksw.dcat.util.view.SingleValuedAccessor;
import org.aksw.jena_sparql_api.beans.model.PropertyOps;

public class SingleValuedAccessorFromPropertyOps<T>
	implements SingleValuedAccessor<T>
{
	protected PropertyOps propertyOps;
	protected Object obj;
	
	public SingleValuedAccessorFromPropertyOps(PropertyOps propertyOps, Object obj) {
		super();
		this.propertyOps = propertyOps;
		this.obj = obj;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		Object o = propertyOps.getValue(obj);
		return (T)o;
	}

	@Override
	public void set(T value) {
		propertyOps.setValue(obj, value);
	}
	
}