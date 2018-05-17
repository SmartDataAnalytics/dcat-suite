package org.aksw.dcat.ap.binding.ckan.domain.impl;

import org.aksw.commons.accessors.PropertySource;
import org.aksw.commons.accessors.SingleValuedAccessor;

import eu.trentorise.opendata.jackan.model.CkanResource;

public class PropertySourceCkanResource
	implements PropertySource
{
	protected CkanResource ckanResource;
	
	public PropertySourceCkanResource(CkanResource ckanResource) {
		super();
		this.ckanResource = ckanResource;
	}
	
	@Override
	public <T> SingleValuedAccessor<T> getProperty(String name, Class<T> valueType) {
		SingleValuedAccessor<T> result = CkanUtils.getSingleValuedAccessor(ckanResource, name, valueType);
		return result;
	}

	@Override
	public Object getSource() {
		return ckanResource;
	}
}
