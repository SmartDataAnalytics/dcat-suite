package org.aksw.dcat.ap.binding.jena.domain.impl;

import org.aksw.dcat.ap.domain.accessors.DcatApAgentAccessor;
import org.aksw.dcat.jena.domain.impl.DcatEntityImpl;
import org.aksw.dcat.util.view.SingleValuedAccessor;
import org.aksw.dcat.util.view.SingleValuedAccessorFromSet;
import org.aksw.jena_sparql_api.utils.model.NodeMapper;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.aksw.jena_sparql_api.utils.model.SetFromMappedPropertyValues;
import org.aksw.move_to_proper_place.TMP;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;


public class RdfDcatApAgentImpl
	extends DcatEntityImpl
	implements RdfDcatApAgent, DcatApAgentAccessor
{
	public RdfDcatApAgentImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	public static <T> SingleValuedAccessor<T> create(Resource s, Property p, NodeMapper<T> nodeMapper) {
		return new SingleValuedAccessorFromSet<>(new SetFromMappedPropertyValues<>(s, p, nodeMapper));
	}

	
	@Override
	public SingleValuedAccessor<String> entityUri() {
		return create(this, TMP.key, NodeMapperFactory.from(String.class));
	}

	@Override
	public SingleValuedAccessor<String> name() {
		return create(this, FOAF.name, NodeMapperFactory.from(String.class));
	}

	@Override
	public SingleValuedAccessor<String> mbox() {
		return create(this, FOAF.mbox, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> homepage() {
		return create(this, FOAF.homepage, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> type() {
		return create(this, DCTerms.type, NodeMapperFactory.from(String.class));
	}

}