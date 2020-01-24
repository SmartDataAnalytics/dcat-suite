package org.aksw.dcat_suite.cli.main;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.impl.core.CatalogResolverUtils;
import org.aksw.dcat.repo.impl.model.SearchResult;
import org.aksw.facete.v3.experimental.VirtualPartitionedQuery;
import org.aksw.facete.v3.impl.RDFConnectionBuilder;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.algebra.utils.FixpointIteration;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilder;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilderImpl;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureContext;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sys.JenaSystem;

public class MainDeleteme {

	public static void print(Collection<SearchResult> items) {
		for(SearchResult item : items) {
			print(item);
		}
	}
	
	public static void print(SearchResult item) {
		String id = item.getIdentifier();
		if(id == null) {
			id = item.getURI();
		}
		
		String types = item.getTypes().stream()
				.map(r -> r.getLocalName())
				.sorted()
				.collect(Collectors.joining(", "));
		
		PrintStream ps = System.out;
		
		ps.println(types + ": " + (id == null ? item : id));
//		if(item.getRelatedDataset() != null && !item.equals(item.getRelatedDataset())) {
//			System.out.println("  related to dataset: " + item.getRelatedDataset());
//		}
		if(item.isLatestVersion()) {
			ps.println("  latest");
		}
		if(id != null) {
			ps.println("  url: " + item);
		}
		ps.println();
	}
	
//	public static List<SearchResult> searchDcat(List<? extends SparqlQueryConnection> conns, Query instanceQuery) {
//		
//	}
	

	
	
	public static void pick(List<SearchResult> list) {
		Console console = System.console();
		boolean interactiveMode = false;
		RDFNode pick = null;

		if(list.isEmpty()) {
			throw new RuntimeException("Should not happen: Obtained non-zero count for matching items, but set of items was empty");
		} else if(list.size() > 1) {
		
			// System.out.println("Multiple candidates:");
			for(int i = 0; i < list.size(); ++i) {
				SearchResult item = list.get(i);
				print(item);
				
//				Resource item = list.get(i);
//				RDFNode type = ResourceUtils.getPropertyValue(item, RDF.type);
//				RDFNode id = ResourceUtils.getPropertyValue(item, DCTerms.identifier);
//				Boolean isLatestVersion = ResourceUtils.getLiteralPropertyValue(item, DCATX.isLatestVersion, Boolean.class);
						
//				System.out.println("(" + (i + 1) + ") " + list.get(i) + " (" + type + ") " + isLatestVersion + " " + id);
			}
			if(interactiveMode && console != null) {
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
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		JenaSystem.init();
		Query inferenceQuery = RDFDataMgrEx.loadQuery("dcat-inferences.sparql");
		Query latestVersionQuery = RDFDataMgrEx.loadQuery("latest-version.sparql");
		Query relatedDataset = RDFDataMgrEx.loadQuery("related-dataset.sparql");

		List<TernaryRelation> views = new ArrayList<>();
		
		views.addAll(VirtualPartitionedQuery.toViews(inferenceQuery));
		views.addAll(VirtualPartitionedQuery.toViews(latestVersionQuery));
		views.addAll(VirtualPartitionedQuery.toViews(relatedDataset));

		//views.add(RelationUtils.SPO);

		Query userQuery = QueryFactory.create("SELECT * { ?s <http://foo.bar/baz> ?o }");
//		Query userQuery = QueryFactory.create("SELECT * { ?s ?p ?o }");

		Query rawRewrite = VirtualPartitionedQuery.rewrite(views, userQuery);
		
		Rewrite coreRewriter = AlgebraUtils.createDefaultRewriter();
//		Function<Op, Op> rewriter = op -> FixpointIteration.apply(100, op, coreRewriter::rewrite);
		Function<Op, Op> rewriter = op -> coreRewriter.rewrite(op);
		
		Query finalRewrite = QueryUtils.rewrite(rawRewrite, rewriter::apply);
		
		System.out.println("User Query:");
		System.out.println(userQuery);
		System.out.println();
		System.out.println("Rewrite");
		System.out.println(finalRewrite);

		
	}
	public static void main3(String[] args) throws IOException, ParseException {
		CatalogResolverUtils.createCatalogResolverDefault();
		
		
		ConjureContext ctx = new ConjureContext();
		Model xmodel = ctx.getModel();
		xmodel.setNsPrefix("rpif", DefaultPrefixes.prefixes.getNsPrefixURI("rpif"));

		ConjureBuilder cj = new ConjureBuilderImpl(ctx);
		cj.union(
			cj.fromDataRef(null).construct("CONSTRUCT WHERE { tp1 }"),
			cj.fromDataRef(null).construct("CONSTRUCT WHERE { tp2 }"),
			cj.fromDataRef(null).construct("CONSTRUCT WHERE { tp3 }")
		);
		
	}
	
	public static void main2(String[] args) throws FileNotFoundException, IOException, ParseException {
		JenaSystem.init();

		RDFConnection conn = RDFConnectionBuilder.start()
			.setSource(RDFDataMgr.loadModel("/home/raven/.dcat/test3/downloads/gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl/_content/data.ttl"))
			.getConnection();


		CatalogResolver catResolver = CatalogResolverUtils.createCatalogResolver(conn);
		//String pattern = "org.limbo.*";
		//String pattern = "org.limbo-bahn-1.0.0";
		String pattern = "train_2";

		
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

		// TODO Combine the resources with their classification
		
		
//		for(Object item : list) {
//			System.out.println(item);
//		}
		//fq.focus().fwd(RDF.type).one().constraints().eq(node)
	}
}
