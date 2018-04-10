package org.aksw.dcat.ap.binding.ckan.domain.impl;

import java.util.Set;

import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.dcat.ap.domain.api.DcatApDataset;
import org.aksw.dcat.ap.domain.api.DcatApDistribution;
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
public class DcatApCkanDatasetViewBase
	implements View
{
	protected CkanDataset ckanDataset;
	protected Personality<CkanDataset, View> personalities;
	
	
	public DcatApCkanDatasetViewBase(CkanDataset ckanDataset, Personality<CkanDataset, View> personalities) {
		super();
		this.ckanDataset = ckanDataset;
		this.personalities = personalities;
	}

	@Override
	public CkanDataset getEntity() {
		return ckanDataset;
	}


	public CkanDataset getContext() {
		return ckanDataset;
	}


	@Override
	public <T extends View> boolean canRegardAs(Class<T> view) {
		Implementation<CkanDataset, T> impl = personalities.getImplementation(view);
		boolean result = impl != null && impl.canWrap(ckanDataset);
		return result;
	}


	@Override
	public <T extends View> T regardAs(Class<T> view) {
		Implementation<CkanDataset, T> impl = personalities.getImplementation(view);
		T result = impl.wrap(ckanDataset);
		return result;
	}
	
	
}

