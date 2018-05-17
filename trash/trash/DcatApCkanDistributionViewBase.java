package org.aksw.dcat.ap.trash;

import org.aksw.dcat.ap.domain.api.View;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;



/**
 * A dataset view provides access to portion of a CKAN datsaet
 * and supports polymorphism via .as()
 * 
 * @author raven Apr 9, 2018
 *
 */
public class DcatApCkanDistributionViewBase
	implements View
{
	protected CkanResource ckanResource;
	protected Personality<CkanResource, View> personalities;
	
	
	public DcatApCkanDistributionViewBase(CkanResource ckanResource, Personality<CkanResource, View> personalities) {
		super();
		this.ckanResource = ckanResource;
		this.personalities = personalities;
	}

	@Override
	public CkanResource getEntity() {
		return ckanResource;
	}


	public CkanDataset getContext() {
		return null;
	}


	@Override
	public <T extends View> boolean canRegardAs(Class<T> view) {
		Implementation<CkanResource, T> impl = personalities.getImplementation(view);
		boolean result = impl != null && impl.canWrap(ckanResource);
		return result;
	}


	@Override
	public <T extends View> T regardAs(Class<T> view) {
		Implementation<CkanResource, T> impl = personalities.getImplementation(view);
		T result = impl.wrap(ckanResource);
		return result;
	}
	
}

