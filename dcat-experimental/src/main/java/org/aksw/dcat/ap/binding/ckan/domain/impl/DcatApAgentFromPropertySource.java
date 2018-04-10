package org.aksw.dcat.ap.binding.ckan.domain.impl;

import org.aksw.dcat.ap.domain.accessors.DcatApAgentAccessor;
import org.aksw.dcat.util.view.SingleValuedAccessor;

//interface AccessorFactory<C, P> {
//	<T> SingleValuedAccessor<T> getProperty(C model, P property, Class<T> valueType);
//}


public class DcatApAgentFromPropertySource
	implements DcatApAgentAccessor
{
	protected PropertySource model; // This roughly corresponds to the graph
	protected String prefix; // e.g. "extra:publisher_"; // This roughly corresponds to a node in the graph

	public DcatApAgentFromPropertySource(String prefix, PropertySource model) {
		this.prefix = prefix;
		this.model = model;
	}

	@Override
	public SingleValuedAccessor<String> entityUri() {
		return model.getProperty(prefix + "uri", String.class);
	}

	@Override
	public SingleValuedAccessor<String> name() {
		return model.getProperty(prefix + "name", String.class);
	}

	@Override
	public SingleValuedAccessor<String> mbox() {
		return model.getProperty(prefix + "email", String.class);
	}

	@Override
	public SingleValuedAccessor<String> homepage() {
		return model.getProperty(prefix + "url", String.class);
	}

	@Override
	public SingleValuedAccessor<String> type() {
		return model.getProperty(prefix + "type", String.class);
	}
}
