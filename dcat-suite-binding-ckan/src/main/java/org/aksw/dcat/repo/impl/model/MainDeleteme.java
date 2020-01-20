package org.aksw.dcat.repo.impl.model;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.experimental.VirtualPartitionedQuery;
import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.facete.v3.impl.RDFConnectionBuilder;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.utils.CountInfo;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

public class MainDeleteme {
	public static Function<String, Query> loadTemplate(String fileOrURI, String templateArgName) throws FileNotFoundException, IOException, ParseException {
		Query templateQuery = RDFDataMgrEx.loadQuery(fileOrURI);

		Function<String, Query> result = value -> {
			Map<String, String> map = Collections.singletonMap(templateArgName, value);
			Query r = QueryUtils.applyNodeTransform(templateQuery, x -> NodeUtils.substWithLookup(x, map::get));
			return r;
		};
		return result;
	};

	
	public static CatalogResolver createCatalogResolver(RDFConnection _conn) throws FileNotFoundException, IOException, ParseException {
		Query inferenceQuery = RDFDataMgrEx.loadQuery("/home/raven/Projects/Eclipse/dcat-suite-parent/queries/dcat-inferences.sparql");
		
		Collection<TernaryRelation> views = VirtualPartitionedQuery.toViews(inferenceQuery);
		views.add(RelationUtils.SPO);
		//views.add(Ternar);


		RDFConnection conn = RDFConnectionBuilder.from(_conn)
				.addQueryTransform(q -> VirtualPartitionedQuery.rewrite(views, q))
				.getConnection();
		
		Function<String, Query> patternToQuery = loadTemplate("/home/raven/Projects/Eclipse/dcat-suite-parent/queries/match-by-regex.sparql", "ARG");		
		Function<String, Query> idToQuery = loadTemplate("/home/raven/Projects/Eclipse/dcat-suite-parent/queries/match-by-regex.sparql", "ARG");

		CatalogResolver result = new CatalogResolverSparql(conn, idToQuery, patternToQuery);
		return result;

	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		JenaSystem.init();

		RDFConnection conn = RDFConnectionBuilder.start()
			.setSource(RDFDataMgr.loadModel("/home/raven/.dcat/test3/downloads/gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl/_content/data.ttl"))
			.getConnection();


		CatalogResolver catResolver = createCatalogResolver(conn);
		//String pattern = "org.limbo.*";
		String pattern = "org.limbo-bahn-1.0.0";

		
		// Load sparql template for matching resources by keyword
		// Function<Map<String, String>, Query> template = null;
		
		List<Resource> matches = catResolver.search(pattern).toList().blockingGet();
		for(Resource match : matches) {
			RDFDataMgr.write(System.out, match.getModel(), RDFFormat.TURTLE_PRETTY);
		}
		//System.out.println(dsResolver.getDataset().getModel());
		
		if(true) {
			return;
		}
		
		
//		System.out.println(templateQuery);
//		
		Query instanceQuery = null; //patternToQuery.apply(pattern);

		FacetedQuery fq = FacetedQueryBuilder.builder()
				.configDataConnection()
					.setSource(conn)
					.end()
				.create();


		UnaryRelation baseConcept = RelationUtils.fromQuery(instanceQuery).toUnaryRelation();
		
		System.out.println(baseConcept);
		
		// if(true) return;
		
		//RDFDataMgrEx.execConstruct(conn, filenameOrURI)
		
		// Infer classification of matching resources (virtual graph?)
		// Possibly filter matching resources by classification (e.g. datasets only)
		// 
		
		//Query view = QueryFactory.create("CONSTRUCT {?s ?p ?o } { ?s ?pRaw ?o . BIND(URI(CONCAT('http://foobar', STR(?pRaw))) AS ?p) }");		
		//PartitionedQuery1 pq = PartitionedQuery1.from(view, Vars.s);
		//Resolver resolver = Resolvers.from(pq);
//			FILTER(?s = <http://www.wikidata.org/prop/P299>)
		
		
		fq.baseConcept(baseConcept);
//			KeywordSearchUtils.createConceptRegexLabelOnly(BinaryRelationImpl.create(DCTerms.identifier), "org.limbo:train_3:0.0.2")
				//KeywordSearchUtils.createConceptRegex(BinaryRelationImpl.create(DCTerms.identifier), "org.limbo.*", true)

//		q.startUnion()
//			.add(x -> x.fwd(DCTerms.identifier).one().constraints().regex(pattern).activate())
//			.add()
//		.end()

// 		This does not work 
//		fq.focus().fwd(DCTerms.identifier).one().constraints().regex(pattern).activate();
//		fq.focus().fwd(DCAT.downloadURL).one().constraints().regex(pattern).activate();
//		fq.focus().constraints().regex(pattern).activate();
		
		long maxItems = 100l;
		CountInfo countInfo = fq.focus().availableValues()
			.count(maxItems, 10000l)
			.timeout(10, TimeUnit.SECONDS)
			.blockingGet();

		boolean abort = false;
		long count = countInfo.getCount();
		if(count == 0) {
			System.err.println("No matches");
			abort = true;
		} else if(count >= maxItems) {
			if(countInfo.isHasMoreItems()) {
				System.err.println("Too many items (" + count + ")");
				abort = true;
			}
		}

		Console console = System.console();
		
		RDFNode pick = null;
		if(!abort) {
//			System.out.println("Matches:");
			List<RDFNode> list = fq.focus().availableValues()
					.add(RDF.type)
					.exec()
					.timeout(10, TimeUnit.SECONDS)
					.toList().blockingGet();

			if(list.isEmpty()) {
				throw new RuntimeException("Should not happen: Obtained non-zero count for matching items, but set of items was empty");
			} else if(list.size() > 1) {
			
				System.out.println("Multiple candidates:");
				for(int i = 0; i < list.size(); ++i) {
					RDFNode item = list.get(i);
					RDFNode type = item.isResource()
							? item.asResource().getProperty(RDF.type).getObject()
							: null;
		
					System.out.println("(" + (i + 1) + ") " + list.get(i) + " (" + type + ")");
				}
				if(console != null) {
					System.out.println("(b) Browse resources");
					System.out.println("Please choose from above: ");
					String line = console.readLine().trim();
					if(line.equals("b")) {
						throw new RuntimeException("Not implemented");
					}
					int idx = Integer.parseInt(line);
					pick = list.get(idx);
				} else {
					// System.out.println("non-interactive mode");
				}
				
			} else {
				pick = list.iterator().next();
			}
		}
		
		System.out.println("Pick: " + pick);

		// TODO Combine the resources with their classification
		
		
//		for(Object item : list) {
//			System.out.println(item);
//		}
		//fq.focus().fwd(RDF.type).one().constraints().eq(node)
	}
}
