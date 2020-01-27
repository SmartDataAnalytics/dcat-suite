package org.aksw.dcat.repo.impl.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.dcat.ckan.config.model.DcatResolverCkan;
import org.aksw.dcat.ckan.config.model.DcatResolverConfig;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.impl.cache.CatalogResolverCaching;
import org.aksw.dcat.repo.impl.ckan.CatalogResolverCkan;
import org.aksw.dcat.repo.impl.fs.CatalogResolverFilesystem;
import org.aksw.dcat.repo.impl.fs.CatalogResolverMulti;
import org.aksw.dcat.repo.impl.model.CatalogResolverSparql;
import org.aksw.dcat.repo.impl.model.DcatResolver;
import org.aksw.jena_sparql_api.algebra.utils.VirtualPartitionedQuery;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.datapod.impl.DataPods;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.resourcespec.ResourceSpecUtils;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.connection.RDFConnectionBuilder;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.commons.internal.org.apache.commons.lang3.SystemUtils;
import eu.trentorise.opendata.jackan.CkanClient;

public class CatalogResolverUtils {
	private static final Logger logger = LoggerFactory.getLogger(CatalogResolverUtils.class);
	
	public static CatalogResolver createCatalogResolverDefault() throws IOException, ParseException {
		Path dcatPath = Paths.get(SystemUtils.USER_HOME).resolve(".dcat");
		CatalogResolver result = createCatalogResolverDefault(dcatPath);
		return result;
	}
	
	public static CatalogResolver wrapWithDiskCache(CatalogResolver coreCatalogResolver) {
		CatalogResolver result = new CatalogResolverCaching(
				CatalogResolverFilesystem.createDefault(),
				coreCatalogResolver);

		return result;
	}
	
	public static Function<String, Query> loadTemplate(String fileOrURI, String templateArgName) throws FileNotFoundException, IOException, ParseException {
		Query templateQuery = RDFDataMgrEx.loadQuery(fileOrURI);

		Function<String, Query> result = value -> {
			Map<String, String> map = Collections.singletonMap(templateArgName, value);
			Query r = QueryUtils.applyNodeTransform(templateQuery, x -> NodeUtils.substWithLookup(x, map::get));
			return r;
		};
		return result;
	};

	// No longer needed, as this is now part of the settings.ttl
	public static List<TernaryRelation> loadViews(Collection<String> extraViews) throws FileNotFoundException, IOException, ParseException {
		//Query inferenceQuery = RDFDataMgrEx.loadQuery("dcat-inferences.sparql");
		Query latestVersionQuery = RDFDataMgrEx.loadQuery("latest-version.sparql");
		Query relatedDataset = RDFDataMgrEx.loadQuery("related-dataset.sparql");

		List<TernaryRelation> views = new ArrayList<>();
		
		//views.addAll(VirtualPartitionedQuery.toViews(inferenceQuery));
		views.addAll(VirtualPartitionedQuery.toViews(latestVersionQuery));
		views.addAll(VirtualPartitionedQuery.toViews(relatedDataset));

		SparqlQueryParser parser = SparqlQueryParserImpl.create(DefaultPrefixes.prefixes);
		
		for(String extraView : extraViews) {
			Query query = parser.apply(extraView);
			views.addAll(VirtualPartitionedQuery.toViews(query));
		}
		
		views.add(RelationUtils.SPO);
		//views.add(Ternar);

		return views;
	}
	
	public static CatalogResolver createCatalogResolver(SparqlQueryConnection conn, List<String> extraViews) throws FileNotFoundException, IOException, ParseException {
		List<TernaryRelation> views = loadViews(extraViews);
//
//		
		RDFConnection _conn = RDFConnectionBuilder.from(new RDFConnectionModular(conn, null, null))
				.addQueryTransform(q -> VirtualPartitionedQuery.rewrite(views, q))
				.getConnection();
		
		Function<String, Query> patternToQuery = loadTemplate("match-by-regex.sparql", "ARG");		
		Function<String, Query> idToQuery = loadTemplate("match-exact.sparql", "ARG");

		CatalogResolver result = new CatalogResolverSparql(_conn, idToQuery, patternToQuery);
		return result;

	}

	/**
	 * 
	 * dcatPath is e.g. ~/.dcat
	 * 
	 * @param dcatPath
	 * @return
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static CatalogResolver createCatalogResolverDefault(Path dcatPath) throws IOException, ParseException {
		
//		HttpResourceRepositoryFromFileSystemImpl repo = HttpResourceRepositoryFromFileSystemImpl.createDefault();

		
		//Model configModel = ModelFactory.createDefaultModel();
		String configUrl = dcatPath.resolve("settings.ttl").toUri().toString();
		Model configModel = RDFDataMgr.loadModel(configUrl);
		
		// Resolve placeholders in the model
		ResourceSpecUtils.resolve(configModel);
		
		List<DcatResolverConfig> configs = configModel
				.listResourcesWithProperty(ResourceFactory.createProperty("http://www.example.org/resolvers"))
				.mapWith(r -> r.as(DcatResolverConfig.class))
				.toList();

		CatalogResolverMulti coreResolver = new CatalogResolverMulti();
		
		for(DcatResolverConfig config : configs) {
			Collection<DcatResolver> resolvers = config.resolvers(DcatResolver.class);
			for(DcatResolver resolverSpec : resolvers) {
				
				DataRef dataRef = resolverSpec.getDataRef();
				
				// Try to map as a DataRef
				//DataRef dataRef = JenaPluginUtils.polymorphicCast(resolverSpec, DataRef.class);
				if(dataRef != null) {
					RdfDataPod dataPod = DataPods.fromDataRef(dataRef);
					
					logger.info("Loaded catalog: " + dataPod + " from " + dataRef);
					
					SparqlQueryConnection conn = dataPod.openConnection();
					
					// Wrap with client side construct because ... virtuoso
					conn = new SparqlQueryConnectionJsa(
							FluentQueryExecutionFactory
							.from(new QueryExecutionFactorySparqlQueryConnection(conn))
							.config()
								.withClientSideConstruct()
							.end()
							.create());

					List<String> extraViews = resolverSpec.getViews();
					
					CatalogResolver resolver = createCatalogResolver(conn, extraViews);
					coreResolver.getResolvers().add(resolver);
					
//					DataPodFactory dataPodFactory = new DataPodFactoryImpl(opExecutor);
//					DataPods.fromSparqlEndpoint(dataRef)
				} else {
					
					// TODO Improve this - we simply assume a ckan resolver here which is not
					// necessarily what we get
					
					DcatResolverCkan ckanResolverSpec = resolverSpec.as(DcatResolverCkan.class);
					
//					System.out.println("Got: " + ckanResolverSpec.getApiKey());	
//					System.out.println("Got: " + ckanResolverSpec.getUrl());
					
					
					String ckanApiUrl = ckanResolverSpec.getUrl();
					String ckanApiKey = ckanResolverSpec.getApiKey();
					
	//				CkanClient ckanClient = new CkanClient("http://ckan.qrowd.aksw.org", "25b91078-fbc6-4b3a-93c5-acfce414bbeb");
					CkanClient ckanClient = new CkanClient(ckanApiUrl, ckanApiKey);
					CatalogResolver ckanResolver = new CatalogResolverCkan(ckanClient);
					coreResolver.getResolvers().add(ckanResolver);
				}
			}			
		}
		
		logger.info("Registered " + coreResolver.getResolvers().size() + " dcat resolvers");

			
		
		//String id = "http://ckan.qrowd.aksw.org/dataset/8bbb915a-f476-4749-b441-5790b368c38b/resource/fb3fed1f-cc9a-4232-a876-b185d8e002c8/download/osm-bremen-2018-04-04-ways-amenity.sorted.nt.bz2";
		//String id = "fb3fed1f-cc9a-4232-a876-b185d8e002c8";
		//String id = "http://dcat.linkedgeodata.org/distribution/osm-bremen-2018-04-04-ways-amenity";
		
//		CatalogResolver result = wrapWithDiskCache(coreResolver);
		CatalogResolver result = coreResolver;

		return result;
	}
}
