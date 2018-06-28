package org.aksw.dcat.ap.playground.main;

import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgent;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgentImpl;
import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDataset;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDatasetImpl;
import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDistribution;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDistributionImpl;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;

public class RdfDcatApPersonalities {
	public static void init(Personality<RDFNode> p) {
		p.add(DcatApDataset.class, new SimpleImplementation(RdfDcatApDatasetImpl::new));
		p.add(DcatApDistribution.class, new SimpleImplementation(RdfDcatApDistributionImpl::new));
		p.add(RdfDcatApAgent.class, new SimpleImplementation(RdfDcatApAgentImpl::new));
	}
}
