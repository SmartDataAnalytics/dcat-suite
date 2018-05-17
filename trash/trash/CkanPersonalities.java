package org.aksw.dcat.ap.trash;

import org.aksw.dcat.ap.domain.api.DcatApDataset;
import org.aksw.dcat.ap.domain.api.DcatApDistribution;
import org.aksw.dcat.ap.domain.api.View;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;

public class CkanPersonalities {
	public static Personality<CkanDataset, View> datasetPersonalities = new PersonalityImpl<>();

	public static Personality<CkanResource, View> resourcePersonalities = new PersonalityImpl<>();

	static {
		datasetPersonalities.add(DcatApDataset.class, new SimpleImplementation<>(
				ckanDataset -> new DcatApCkanDatsetViewImpl(ckanDataset, datasetPersonalities)));

		resourcePersonalities.add(DcatApDistribution.class, new SimpleImplementation<>(
				ckanResource -> new DcatApDistributionViewImpl(ckanResource, resourcePersonalities)));
	}
}