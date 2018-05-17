package org.aksw.dcat.ap.domain.accessors;

import org.aksw.commons.accessors.PropertySource;
import org.aksw.dcat.ap.domain.api.View;
import org.aksw.dcat.ap.trash.Personality;

public class PropertyBasedView
	implements View
{
	protected String prefix; // e.g. a string such as 'extra:publisher_' TODO or such an expression in compiled form?
	//protected Integer index; // optional index; may be null - should the index be part of the prefix?
	protected PropertySource propertySource;
	protected Personality<PropertySource, View> personalities;
	
	public PropertyBasedView(
			String prefix,
			PropertySource propertySource) {
		super();
		this.prefix = prefix;
		//this.index = index;
		this.propertySource = propertySource;
	}

	@Override
	public Object getEntity() {
		return prefix;
	}

	@Override
	public Object getContext() {
		return propertySource;
	}

	@Override
	public <T extends View> boolean canRegardAs(Class<T> view) {
//		Implementation<CkanResource, T> impl = personalities.getImplementation(view);
//		boolean result = impl != null && impl.canWrap(ckanResource);
//		return result;
		return false;
	}


	@Override
	public <T extends View> T regardAs(Class<T> view) {
//		Implementation<CkanResource, T> impl = personalities.getImplementation(view);
//		T result = impl.wrap(ckanResource);
//		return result;
		return null;
	}	
}
