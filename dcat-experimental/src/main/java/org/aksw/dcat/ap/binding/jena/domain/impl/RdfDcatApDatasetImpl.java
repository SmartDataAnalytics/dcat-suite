package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import org.aksw.dcat.ap.domain.accessors.DcatApDatasetAccessor;
import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.dcat.ap.domain.api.DcatApContactPoint;
import org.aksw.dcat.ap.domain.api.DcatApDistribution;
import org.aksw.dcat.ap.domain.api.PeriodOfTime;
import org.aksw.dcat.ap.domain.api.Spatial;
import org.aksw.dcat.ap.domain.api.View;
import org.aksw.dcat.jena.domain.api.Adms;
import org.aksw.dcat.util.view.LazyCollection;
import org.aksw.dcat.util.view.SetFromConverter;
import org.aksw.dcat.util.view.SingleValuedAccessor;
import org.aksw.dcat.util.view.SingleValuedAccessorDirect;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;

import com.google.common.base.Converter;

public class RdfDcatApDatasetImpl
	extends RdfDcatApResource 
	implements RdfDcatApDataset, DcatApDatasetAccessor
{

	public RdfDcatApDatasetImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	/**
	 * Creates a new blank node and views it as a DcapApDistribution
	 * 
	 */
	@Override
	public RdfDcatApDistribution createDistribution() {
		return getModel().createResource().as(RdfDcatApDistribution.class);
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
		return create(this, DCTerms.title, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<String> description() {
		return create(this, DCTerms.description, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<Set<String>> tags() {
		return createSet(this, DCAT.keyword, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<Set<String>> themes() {
		return createSet(this, DCAT.theme, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> identifier() {
		return create(this, DCTerms.identifier, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<String> alternateIdentifier() {
		return create(this, Adms.identifier, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Instant> issued() {
		return create(this, DCTerms.issued, NodeMapperFactory.from(Calendar.class))
				.convert(null);
		// TODO Add a proper converter
	}

	@Override
	public SingleValuedAccessor<Instant> modified() {
		return create(this, DCTerms.modified, NodeMapperFactory.from(Calendar.class))
				.convert(null);
	}

	@Override
	public SingleValuedAccessor<String> versionInfo() {
		return create(this, OWL.versionInfo, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<String> versionNotes() {
		return create(this, Adms.versionNotes, NodeMapperFactory.string);
	}

	@Override
	public SingleValuedAccessor<Set<String>> languages() {
		return createSet(this, DCTerms.language, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> landingPage() {
		return create(this, DCAT.landingPage, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> accrualPeriodicity() {
		return create(this, DCTerms.accrualPeriodicity, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> conformsTo() {
		return createSet(this, DCTerms.conformsTo, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> accessRights() {
		return create(this, DCTerms.accessRights, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> pages() {
		return createSet(this, FOAF.page, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> provenance() {
		return create(this, DCTerms.provenance, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<String> type() {
		return create(this, DCTerms.type, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> hasVersions() {
		return createSet(this, DCTerms.hasVersion, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> isVersionOf() {
		return createSet(this, DCTerms.isVersionOf, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> sources() {
		return createSet(this, DCTerms.source, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Set<String>> samples() {
		return createSet(this, Adms.sample, NodeMapperFactory.uriString);
	}

	@Override
	public SingleValuedAccessor<Spatial> spatial() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingleValuedAccessor<PeriodOfTime> temporal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingleValuedAccessor<DcatApAgent> publisher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingleValuedAccessor<DcatApContactPoint> contactPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingleValuedAccessor<Set<DcatApDistribution>> distributions() {
		Converter<DcatApDistribution, Resource> converter = new Converter<DcatApDistribution, Resource>() {
			@Override
			protected Resource doForward(DcatApDistribution a) {
				return (Resource)a;
			}

			@Override
			protected DcatApDistribution doBackward(Resource b) {
				return b.as(RdfDcatApDistribution.class);
				//return CkanPersonalities.resourcePersonalities.getImplementation(DcatApDistribution.class).wrap(b);
			}
		};
	
		Set<DcatApDistribution> result = new SetFromConverter<>(
						new SetFromPropertyValues<>(this, DCAT.distribution, Resource.class),
						converter);
		
		SingleValuedAccessor<Set<DcatApDistribution>> tmp = new SingleValuedAccessorDirect<>(result);
		
		
		return tmp;
	}
//
//	public <T extends DcatApDistribution> SingleValuedAccessor<Set<T>> distributions(Class<T> distributionType) {
//		
//		Converter<T, Resource> converter = new Converter<T, Resource>() {
//			@Override
//			protected Resource doForward(T a) {
//				return (Resource)a;
//			}
//
//			@Override
//			protected DcatApDistribution doBackward(Resource b) {
//				b.as(distributionType);
//				//return CkanPersonalities.resourcePersonalities.getImplementation(DcatApDistribution.class).wrap(b);
//			}
//		};
//		
//		Set<DcatApDistribution> result = new SetFromConverter<>(
//				new LazyCollection<>(
//						new SingleValuedAccessorImpl<>(ckanDataset::getResources, ckanDataset::setResources),
//						ArrayList::new, true),
//				converter);
//				
//		return result;
//		//return createSet(this, DCAT.distribution, NodeMapperFactory.uriString);
//		return null;
//	}

}
