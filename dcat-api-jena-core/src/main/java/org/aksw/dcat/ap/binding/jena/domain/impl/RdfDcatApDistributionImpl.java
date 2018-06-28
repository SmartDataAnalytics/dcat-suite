package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.time.Instant;
import java.util.Set;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.dcat.ap.domain.accessors.DcatApDistributionCoreAccessor;
import org.aksw.dcat.jena.domain.api.Adms;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;

public class RdfDcatApDistributionImpl
	extends RdfDcatApResourceImpl
	implements DcatApDistribution, DcatApDistributionCoreAccessor
{
	public RdfDcatApDistributionImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	@Override
	public SingleValuedAccessor<String> title() {
		return create(this, DCTerms.title, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<String> description() {
		return create(this, DCTerms.description, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<Set<String>> accessUrls() {
		return createSet(this, DCAT.accessURL, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> downloadUrls() {
		return createSet(this, DCAT.downloadURL, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> mediaType() {
		return create(this, DCAT.mediaType, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<String> format() {
		return create(this, DCTerms.format, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<String> license() {
		return create(this, DCTerms.license, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> status() {
		return create(this, Adms.status, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Long> byteSize() {
		return create(this, DCAT.byteSize, NodeMapperFactory.from(Long.class));
	}

	@Override
	public SingleValuedAccessor<Instant> issued() {
		return create(this, DCTerms.issued, NodeMapperFactory.from(Instant.class));
	}

	@Override
	public SingleValuedAccessor<Instant> modified() {
		return create(this, DCTerms.modified, NodeMapperFactory.from(Instant.class));
	}

	@Override
	public SingleValuedAccessor<String> rights() {
		return create(this, DCTerms.rights, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> pages() {
		return createSet(this, FOAF.page, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> conformsTo() {
		return createSet(this, DCTerms.conformsTo, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> languages() {
		return createSet(this, DCTerms.language, NodeMapperFactory.uriString);
	}	
}
