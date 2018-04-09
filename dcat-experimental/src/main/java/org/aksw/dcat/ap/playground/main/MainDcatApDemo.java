package org.aksw.dcat.ap.playground.main;

import java.util.Set;

import org.aksw.dcat.ap.ckan.CkanPersonalities;
import org.aksw.dcat.ap.ckan.Implementation;
import org.aksw.dcat.ap.jena.domain.api.DcatApAgent;
import org.aksw.dcat.ap.jena.domain.api.DcatApDatasetCore;
import org.aksw.dcat.ap.jena.domain.api.DcatApDistribution;

import eu.trentorise.opendata.jackan.model.CkanDataset;

public class MainDcatApDemo {

	public static void main(String[] args) {
		CkanDataset ckanDataset = new CkanDataset();

		Implementation<CkanDataset, DcatApDatasetCore> impl = CkanPersonalities.datasetPersonalities.getImplementation(DcatApDatasetCore.class);
		DcatApDatasetCore dcatDataset = impl.wrap(ckanDataset);
		
		dcatDataset.setTitle("LinkedGeoData");
		dcatDataset.setLandingPage("http://linkedgeodata.org");
		
		DcatApAgent publisher = dcatDataset.getPublisher();
		publisher.setName("AKSW Research Group");
		publisher.setHomepage("http://aksw.org");
		publisher.setMbox("mailto:foo@bar.baz");
		publisher.setType("whatever this field indicates");
		
		System.out.println("Backend title: " + ckanDataset.getTitle());
		System.out.println("Backend landing page: " + ckanDataset.getUrl());				
		System.out.println("Extras: " + ckanDataset.getExtrasAsHashMap());
		
		
		Set<DcatApDistribution> distributions = dcatDataset.getDistributions();

		DcatApDistribution dist = dcatDataset.createDistribution();
		
		distributions.add(dist);
		//distributions.add(dist);
		
		distributions.remove(dist);
		
		
		
//		CkanResource ckanResource = new CkanResource();
//		
//		DcatApDistribution dcatDistribution = new DcatApDistributionViewImpl(ckanResource, CkanPersonalities.resourcePersonalities);
//		distributions.add(dcatDistribution);
		
		
		System.out.println(ckanDataset.getResources());
	}
}
