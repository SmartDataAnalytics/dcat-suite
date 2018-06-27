package org.aksw.dcat.ap.playground.main;

import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDataset;
import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDistribution;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgent;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgentImpl;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDatasetImpl;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDistributionImpl;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.vocabulary.DCTerms;

public class MainDemoDcatApRdf {

	public static void main(String[] args) {
		JenaSystem.init();
		BuiltinPersonalities.model.add(DcatApDataset.class, new SimpleImplementation(RdfDcatApDatasetImpl::new));
		BuiltinPersonalities.model.add(DcatApDistribution.class, new SimpleImplementation(RdfDcatApDistributionImpl::new));
		BuiltinPersonalities.model.add(RdfDcatApAgent.class, new SimpleImplementation(RdfDcatApAgentImpl::new));

		
		Model model = ModelFactory.createDefaultModel();
		DcatApDataset rdfDataset = model.createResource().as(DcatApDataset.class);
		
		rdfDataset.setTitle("World Domination Plans");
		//plainDataset.setDescription("Top Secret");
		rdfDataset.setVersionInfo("0.3-SNAPSHOT");
		rdfDataset.setVersionNotes("Work in progress");
		rdfDataset.setAccuralPeriodicity("http://foo.bar/baz");
		rdfDataset.addLiteral(DCTerms.description, "Top Secret");		
		
		DcatApDistribution dist = rdfDataset.createDistribution();
		rdfDataset.getDistributions(Resource.class).add(dist);
		
		dist.setAccessUrl("http://some.url/");
		dist.setTitle("My dist");
		dist.setDescription("Some description");

		// How to switch to different views over the underlying model?
		// The point I did not properly consider up to now is that:
		// A model is generally/primarily a collection of entities (+their state)
		// Hence, the next thing we need is an object that encapsulates reference to an entity
		// And then we can put views on that entity
		// - SparqlMetadata m = dist.regardAs(SparqlMetadata.class);
		// - dist.switch()
		
		rdfDataset.getDistributions().remove(dist);
		
		
		System.out.println("Description: " + rdfDataset.getDescription());
		
		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE);

		/*
		 * Copy values to the ckan model
		 */
		
		// Create the ckan dataset bean
		// NOTE Unfortunately the Jackan CkanDataset does not seem to cover all attributes
//		CkanDataset ckanDataset = new CkanDataset();
//		
//		// Abstract the bean as something that has properties - so it could also be plain json
//		PropertySource s = new PropertySourceCkanDataset(ckanDataset);
//		DcatApAgent publisher = new DcatApAgentFromPropertySource("extra:publisher_", s);
		
		//ckanDataset = new DcatApCkanDatsetViewImpl(ckanDataset, personalities);
		
	}
}
