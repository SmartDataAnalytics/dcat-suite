package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.time.Instant;
import java.util.Set;

import org.aksw.dcat.ap.domain.accessors.DcatApDistributionAccessor;
import org.aksw.dcat.ap.domain.api.View;
import org.aksw.dcat.jena.domain.api.Adms;
import org.aksw.dcat.util.view.SingleValuedAccessor;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;

public class RdfDcatApDistributionImpl
	extends RdfDcatApResource
	implements DcatApDistributionAccessor
{
	public RdfDcatApDistributionImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	@Override
	public Object getEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends View> boolean canRegardAs(Class<T> view) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T extends View> T regardAs(Class<T> view) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingleValuedAccessor<String> title() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingleValuedAccessor<String> description() {
		return create(this, DCTerms.title, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<String> accessUrl() {
		return create(this, DCAT.accessURL, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> downloadUrl() {
		return create(this, DCAT.downloadURL, NodeMapperFactory.uriString);
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
