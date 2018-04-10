package org.aksw.dcat.ap.playground.main;

import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgent;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgentImpl;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDataset;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDatasetImpl;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDistribution;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDistributionImpl;
import org.aksw.dcat.ap.domain.api.DcatApDataset;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.system.JenaSystem;

public class MainDcatApRdfDemo {

	public static void main(String[] args) {
		JenaSystem.init();
		BuiltinPersonalities.model.add(RdfDcatApDataset.class, new SimpleImplementation(RdfDcatApDatasetImpl::new));
		BuiltinPersonalities.model.add(RdfDcatApDistribution.class, new SimpleImplementation(RdfDcatApDistributionImpl::new));
		BuiltinPersonalities.model.add(RdfDcatApAgent.class, new SimpleImplementation(RdfDcatApAgentImpl::new));

		
		Model model = ModelFactory.createDefaultModel();
		DcatApDataset plainDataset = model.createResource().as(RdfDcatApDataset.class);
		
		plainDataset.setTitle("Wold Domination Plans");
		plainDataset.setDescription("Top Secret");
		plainDataset.setVersionInfo("0.3-SNAPSHOT");
		plainDataset.setVersionNotes("Work in progress");
		plainDataset.setAccuralPeriodicity("http://foo.bar/baz");
		
		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE);
		
	}
}
