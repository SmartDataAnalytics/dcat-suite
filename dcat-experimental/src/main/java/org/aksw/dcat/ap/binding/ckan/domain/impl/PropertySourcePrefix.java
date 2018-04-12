package org.aksw.dcat.ap.binding.ckan.domain.impl;

import org.aksw.dcat.util.view.SingleValuedAccessor;

public class PropertySourcePrefix
	implements PropertySource
{
	protected String prefix;
	protected PropertySource delegate;

	public PropertySourcePrefix(String prefix, PropertySource delegate) {
		super();
		this.delegate = delegate;
		this.prefix = prefix;
	}

	@Override
	public <T> SingleValuedAccessor<T> getProperty(String name, Class<T> valueType) {
		String qualifiedName = prefix + name;
		SingleValuedAccessor<T> result = delegate.getProperty(qualifiedName, valueType);
		return result;
	}

	@Override
	public String toString() {
		return "PropertySourcePrefix [prefix=" + prefix + ", delegate=" + delegate + "]";
	}

	@Override
	public Object getSource() {
		return delegate.getSource();
	}

}
