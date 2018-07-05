package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.Collection;

import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgent;
import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDataset;
import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDistribution;
import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.dcat.ap.domain.api.DcatApDistributionCore;
import org.aksw.dcat.ap.playground.main.RdfDcatApPersonalities;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.system.JenaSystem;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;

public class MainDcatApDemo {

	public static void main(String[] args) {
        JenaSystem.init();
        RdfDcatApPersonalities.init(BuiltinPersonalities.model);
        /*
         * playground
         */
        
        Model model = ModelFactory.createModelForGraph(new PseudoGraph());
        
        

        CkanDataset ckanDataset = new CkanDataset();
        CkanResource ckanResource = new CkanResource();
        ckanResource.setDescription("test description");


        DcatApDataset dcatDataset = model.createResource().as(DcatApDataset.class);

        RdfDcatApAgent rdfPublisher = model.createResource("http://my.agent").as(RdfDcatApAgent.class);

        dcatDataset.setPublisher(rdfPublisher);
        
        //RdfDcatApDistribution rdfDistribution = rdfModel.createResource("http://my.dist/ribution").as(RdfDcatApDistribution.class);

		
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
		
		
		Collection<DcatApDistribution> distributions = dcatDataset.getDistributions(DcatApDistribution.class);

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
