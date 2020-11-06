package org.aksw.dcat.ap.binding.ckan.domain.impl;

import org.aksw.commons.accessors.PropertySource;
import org.aksw.commons.accessors.SingleValuedAccessor;

import eu.trentorise.opendata.jackan.model.CkanDataset;

public class PropertySourceCkanDataset
	implements PropertySource
{
	protected CkanDataset ckanDataset;
	
	public PropertySourceCkanDataset(CkanDataset ckanDataset) {
		super();
		this.ckanDataset = ckanDataset;
	}
	
	@Override
	public <T> SingleValuedAccessor<T> getProperty(String name, Class<T> valueType) {
		SingleValuedAccessor<T> result = CkanUtils.getSingleValuedAccessor(ckanDataset, name, valueType);
		return result;
	}

	@Override
	public Object getSource() {
		return ckanDataset;
	}
}
