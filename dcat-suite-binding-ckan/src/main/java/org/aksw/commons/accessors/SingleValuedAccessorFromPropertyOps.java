package org.aksw.commons.accessors;

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

	@Override
	public String toString() {
		return "SingleValuedAccessorFromPropertyOps [" + propertyOps.getName() + "=" + get() + " from [" + obj + "]]";
	}
	
	
}