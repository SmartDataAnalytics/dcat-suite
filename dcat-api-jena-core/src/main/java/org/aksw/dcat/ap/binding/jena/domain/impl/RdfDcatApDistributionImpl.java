package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.time.Instant;
import java.util.Set;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.dcat.ap.domain.accessors.DcatApDistributionCoreAccessor;
import org.aksw.dcat.jena.domain.api.Adms;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
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
		return create(this, DCTerms.title, NodeMappers.string);
	}

	@Override
	public SingleValuedAccessor<String> description() {
		return create(this, DCTerms.description, NodeMappers.string);
	}

	@Override
	public SingleValuedAccessor<Set<String>> accessUrls() {
		return createSet(this, DCAT.accessURL, NodeMappers.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> downloadUrls() {
		return createSet(this, DCAT.downloadURL, NodeMappers.uriString);
	}

	@Override
	public SingleValuedAccessor<String> mediaType() {
		return create(this, DCAT.mediaType, NodeMappers.string);
	}

	@Override
	public SingleValuedAccessor<String> format() {
		return create(this, DCTerms.format, NodeMappers.string);
	}

	@Override
	public SingleValuedAccessor<String> license() {
		return create(this, DCTerms.license, NodeMappers.uriString);
	}

	@Override
	public SingleValuedAccessor<String> status() {
		return create(this, Adms.status, NodeMappers.uriString);
	}

	@Override
	public SingleValuedAccessor<Long> byteSize() {
		return create(this, DCAT.byteSize, NodeMappers.from(Long.class));
	}

	@Override
	public SingleValuedAccessor<Instant> issued() {
		return create(this, DCTerms.issued, NodeMappers.from(Instant.class));
	}

	@Override
	public SingleValuedAccessor<Instant> modified() {
		return create(this, DCTerms.modified, NodeMappers.from(Instant.class));
	}

	@Override
	public SingleValuedAccessor<String> rights() {
		return create(this, DCTerms.rights, NodeMappers.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> pages() {
		return createSet(this, FOAF.page, NodeMappers.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> conformsTo() {
		return createSet(this, DCTerms.conformsTo, NodeMappers.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> languages() {
		return createSet(this, DCTerms.language, NodeMappers.uriString);
	}	
}
