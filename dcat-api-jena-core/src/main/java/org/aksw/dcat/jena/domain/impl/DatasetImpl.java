package org.aksw.dcat.jena.domain.impl;

import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.jena_sparql_api.utils.model.SetFromResourceAndProperty;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.DCAT;

public class DatasetImpl
	extends DcatEntityImpl
	implements DcatDataset
{
	public DatasetImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	@Override
	public Set<DcatDistribution> getDistributions() {
		return new SetFromResourceAndProperty<>(this, DCAT.distribution, DcatDistribution.class);
	}

}
