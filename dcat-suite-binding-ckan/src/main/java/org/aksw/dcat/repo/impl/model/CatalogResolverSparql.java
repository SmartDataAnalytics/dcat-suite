package org.aksw.dcat.repo.impl.model;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.aksw.commons.collections.CollectionFromIterable;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.dcat.repo.impl.core.DatasetResolverImpl;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.vocab.DCAT;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;

import io.reactivex.Flowable;
import io.reactivex.Maybe;


/**
 * There are two approaches:
 * (1) Have separate methods for datasets, distributions and downloads
 * (2) Have a single comprehensive query for all resources together with their classification
 * 
 * @author raven
 *
 */
public class CatalogResolverSparql 
	implements CatalogResolver
{
	protected RDFConnection conn;
	protected Function<String, Query> patternToQuery;
	protected Function<String, Query> idToQuery;
	
	
	@Override
	public Flowable<Resource> search(String pattern) {
		return resolveAny(pattern, patternToQuery, null);
	}

	public CatalogResolverSparql(
			RDFConnection conn,
			Function<String, Query> idToQuery,
			Function<String, Query> patternToQuery) {
		this.conn = conn;
		this.idToQuery = idToQuery;
		this.patternToQuery = patternToQuery;
	}
	
	public Flowable<Resource> resolveAny(
			String pattern,
			Function<String, Query> patternToQuery,
			RDFNode type) {
		FacetedQuery fq = FacetedQueryBuilder.builder()
				.configDataConnection()
					.setSource(conn)
					.end()
				.create();

		Query instanceQuery = patternToQuery.apply(pattern);

		UnaryRelation baseConcept = RelationUtils.fromQuery(instanceQuery).toUnaryRelation();
		fq.baseConcept(baseConcept);

		List<RDFNode> validTypes = Arrays.asList(DCAT.Dataset, DCAT.Distribution);
		
		ConstraintFacade<? extends FacetNode> constraints = fq.focus().fwd(RDF.type).one().constraints();
		if(type != null) {
			constraints.eq(type).activate();
		} else {
			for(RDFNode validType : validTypes) {
				constraints.eq(validType).activate();
			}
		}
		
		DataQuery<RDFNode> dataQuery = fq.focus().availableValues()
			.filter(Concept.create("FILTER(isIRI(?s))", "s"))
			.add(RDF.type);
		
		System.out.println("DATA QUERY: ");
		System.out.println(dataQuery.toConstructQuery());
		
		// Remove all non-valid types
		Flowable<Resource> r = dataQuery
				.exec()
				.map(RDFNode::asResource)
//				.doOnNext(x -> {
//					// Filter out non-valid types
//					Collection<RDFNode> nonValidTypes = CollectionFromIterable.create(() -> ResourceUtils
//						.listPropertyValues(x.asResource(), RDF.type)
//						.filterDrop(validTypes::contains));
//					nonValidTypes.clear();
//				})
				.timeout(10, TimeUnit.SECONDS);
		
		return r;
	}
	
	@Override
	public Maybe<DatasetResolver> resolveDataset(String datasetId) {
		return resolveAny(datasetId, idToQuery, DCAT.Dataset)
			.firstElement()
			.map(e -> new DatasetResolverImpl(this, e.as(DcatDataset.class)));
		
//		Resource r = model.getResource(datasetId);
//
//		Maybe<DatasetResolver> result = r == null ? Maybe.empty() : Maybe.just(
//				new DatasetResolverImpl(this, r.as(DcatDataset.class)));
//		return result;		
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String distributionId) {
		return null;

//		DcatDistribution rResource = model.createResource(distributionId).as(DcatDistribution.class);
//		DcatDistribution rBlankNode = model.wrapAsResource(NodeFactory.createBlankNode(distributionId)).as(DcatDistribution.class);
//
//		List<DistributionResolver> list =
//			Arrays.asList(rResource, rBlankNode).stream().flatMap(r ->
//				ResourceUtils.listReversePropertyValues(r, DCAT.distribution).toList().stream()
//					.map(s -> Maps.immutableEntry(s, r)))
//			.map(e -> (DistributionResolver)new DistributionResolverImpl(new DatasetResolverImpl(this, e.getKey().as(DcatDataset.class)), e.getValue()))
//			.collect(Collectors.toList());
//
//		Flowable<DistributionResolver> result = Flowable.fromIterable(list);
//		return result;
	}

	@Override
	public Maybe<URL> resolveDownload(String downloadUri) throws Exception {
		return null;
//		Maybe<URL> result = Maybe.just(new URL(downloadUri));
//		return result;
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
		return resolveDistribution(distributionId)
			.filter(r -> r.getDatasetResolver().getDataset().getURI().equals(datasetId));
	}

	@Override
	public Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, String distributionId) {
		return resolveDistribution(dataset.getURI(), distributionId);
	}
	
	
}
