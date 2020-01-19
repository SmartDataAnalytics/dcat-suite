package org.aksw.dcat.repo.impl.model;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.dcat.repo.impl.core.DatasetResolverImpl;
import org.aksw.dcat.repo.impl.core.DistributionResolverImpl;
import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;

import com.google.common.collect.Maps;

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
//public class CatalogResolverSparql 
//	implements CatalogResolver
//{
//	protected RDFConnection conn;
//	
//	public CatalogResolverSparql(RDFConnection conn) {
//		this.conn = conn;
//	}
//	
//	@Override
//	public Maybe<DatasetResolver> resolveDataset(String datasetId) {
//		
//		Resource r = model.getResource(datasetId);
//
//		Maybe<DatasetResolver> result = r == null ? Maybe.empty() : Maybe.just(
//				new DatasetResolverImpl(this, r.as(DcatDataset.class)));
//		return result;		
//	}
//
//	@Override
//	public Flowable<DistributionResolver> resolveDistribution(String distributionId) {
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
//	}
//
//	@Override
//	public Maybe<URL> resolveDownload(String downloadUri) throws Exception {
//		Maybe<URL> result = Maybe.just(new URL(downloadUri));
//		return result;
//	}
//
//	@Override
//	public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
//		return resolveDistribution(distributionId)
//			.filter(r -> r.getDatasetResolver().getDataset().getURI().equals(datasetId));
//	}
//
//	@Override
//	public Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, String distributionId) {
//		return resolveDistribution(dataset.getURI(), distributionId);
//	}
//	
//	
//}
