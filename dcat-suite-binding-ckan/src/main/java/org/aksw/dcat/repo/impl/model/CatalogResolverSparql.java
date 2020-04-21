package org.aksw.dcat.repo.impl.model;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.CountInfo;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import com.github.jsonldjava.shaded.com.google.common.collect.Ordering;
import com.google.common.collect.ImmutableMap;

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
    protected Query dcatShape;

    protected SparqlQueryConnection conn;
    protected Function<String, Query> patternToQuery;
    protected Function<String, Query> idToQuery;

    public SparqlQueryConnection getConnection() {
        return conn;
    }

    public Function<String, Query> getPatternToQuery() {
        return patternToQuery;
    }

    public Function<String, Query> getIdToQuery() {
        return idToQuery;
    }

    public Query getDcatShape() {
        return dcatShape;
    }

    @Override
    public Flowable<Resource> search(String pattern) {
        return resolveAny(pattern, patternToQuery, null)
                .map(RDFNode::asResource);
    }

    public CatalogResolverSparql(
            SparqlQueryConnection conn,
            Function<String, Query> idToQuery,
            Function<String, Query> patternToQuery) {
        this.conn = conn;
        this.idToQuery = idToQuery;
        this.patternToQuery = patternToQuery;

        try {
            this.dcatShape = RDFDataMgrEx.loadQuery("dcat-shape.sparql");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static List<SearchResult> searchDcat(
            SparqlQueryConnection conn,
            Query instanceQuery,
            Query shapeQuery) {

        FacetedQuery fq = FacetedQueryBuilder.builder()
                .configDataConnection()
                    .setSource(conn)
                    .end()
                .create();


        UnaryRelation baseConcept = RelationUtils.fromQuery(instanceQuery).toUnaryRelation();

        // System.out.println(baseConcept);

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

        fq.focus().fwd(RDF.type).one().constraints().eq(DCAT.Dataset).activate();

        boolean doCounting = true;
        long count = -1;
        long maxItems = 100l;
        CountInfo countInfo = null;
        if(doCounting) {
            countInfo = fq.focus().availableValues()
                .count(maxItems, 10000l)
                .timeout(10, TimeUnit.SECONDS)
                .blockingGet();
            count = countInfo.getCount();
        }

        boolean abort = false;
        if(count == -1) {
            // Nothing to do
        } else if(count == 0) {
            System.err.println("No matches");
            abort = true;
        } else if(count >= maxItems) {
            if(countInfo.isHasMoreItems()) {
                System.err.println("Too many items (" + count + ")");
                abort = true;
            }
        }

        DataQuery<RDFNode> dataQuery = fq.focus().availableValues()
                .add(RDF.type)
                //.add(DCATX.relatedDataset)
                .add(DCATX.versionTag)
                .addOptional(DCTerms.identifier)
                .filter(Concept.create("FILTER(isIRI(?s))", "s"));


        Entry<Node, Query> q = combineDataQueryWithShape(dataQuery, shapeQuery);

//		System.out.println("DATA QUERY: " + dataQuery.toConstructQuery());

        List<SearchResult> list = null;
        if(!abort) {
            list = SparqlRx.execConstructGrouped(conn, q)
                    .map(x -> x.as(SearchResult.class))
                    .timeout(10, TimeUnit.SECONDS)
                    .sorted(Ordering.from(SearchResult::defaultCompare).reversed())
                    .toList().blockingGet();
//			System.out.println("Matches:");
//			list = dataQuery
//					.exec()
//					.map(x -> x.as(SearchResult.class))
//					.timeout(10, TimeUnit.SECONDS)
//					.sorted(Ordering.from(SearchResult::defaultCompare).reversed())
//					.toList().blockingGet();

        }
//		System.out.println("Pick: " + pick);

        return list;
    }

    public static Entry<Node, Query> combineDataQueryWithShape(DataQuery<RDFNode> dq, Query shape) {
        //DataQuery<RDFNode> dq = createSearchDataQuery(pattern, patternToQuery, null);

        //Query shape = this.dcatShape;
        Var shapeRootVar = Var.alloc("DATASET");

        Entry<Node, Query> e = dq.toConstructQuery();
        UnaryRelation ur = new Concept(e.getValue().getQueryPattern(), (Var)e.getKey());

        Relation r = RelationUtils.fromQuery(shape);
        Relation el = r.joinOn(shapeRootVar)
            .filterRelationFirst(true)
            .with(ur)
            ;

        Query tmp = new Query();
        tmp.setQuerySelectType();
        tmp.setQueryPattern(el.getElement());;
        Query finalQuery = QueryUtils.restoreQueryForm(tmp, shape);

        Entry<Node, Query> result = Maps.immutableEntry(shapeRootVar, finalQuery);
        return result;
        //Flowable<RDFNode> result = SparqlRx.execConstructGrouped(conn, shapeRootVar, finalQuery);
        //return result;
//		List<RDFNode> items = SparqlRx.execConstructGrouped(conn, shapeRootVar, finalQuery)
//			.toList()
//			.blockingGet();
//
//		for(RDFNode item : items) {
//			System.out.println("Item: "  + item);
//			RDFDataMgr.write(System.out, item.getModel(), RDFFormat.TURTLE_PRETTY);
//		}
//
//		System.out.println("Printed " + items.size() + " items");
//
//		System.out.println("Relation");
//		System.out.println(el);
    }


    public static Query subst(SearchResult item, Query shape) {
        Map<Resource, String> typeToPlaceholder = ImmutableMap.<Resource, String>builder()
            .put(DCAT.Dataset, "DATASET")
            .put(DCAT.Distribution, "DISTRIBUTION")
            .build();

        String placeholder = item.getTypes().stream()
            .map(t -> typeToPlaceholder.get(t))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        Query result = null;
        if(placeholder != null) {
            Var v = Var.alloc(placeholder);
            result = QueryUtils.applyNodeTransform(shape, n -> n.equals(v) ? item.asNode() : n);
        }
        return result;
    }

    public static DcatDataset fetchDataset(SparqlQueryConnection conn, SearchResult item, Query shape) {
        Query query = subst(item, shape);
        Model model = conn.queryConstruct(query);
        List<Resource> tmp = model.listSubjectsWithProperty(RDF.type, DCAT.Dataset)
                .toList();

        DcatDataset pick = tmp.stream().findFirst().map(x -> x.as(DcatDataset.class)).orElse(null);

        return pick;
    }

    public DcatDataset searchResultToDataset(SearchResult item) {
        DcatDataset result = fetchDataset(conn, item, dcatShape);
        return result;
    }

    public Flowable<SearchResult> resolveAny(
            String pattern,
            Function<String, Query> patternToQuery,
            RDFNode type) {

        Query intstanceQuery = patternToQuery.apply(pattern);
        List<SearchResult> l = searchDcat(conn, intstanceQuery, dcatShape);

        if(l == null) {
            l = Collections.emptyList();
        }

        return Flowable.fromIterable(l);
            //.map(RDFNode::asResource);
    }



    public DataQuery<RDFNode> createSearchDataQuery(String pattern,
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

        List<RDFNode> validTypes = Arrays.asList(DCAT.Dataset, DCAT.Distribution, DCATX.DownloadURL);

        ConstraintFacade<? extends FacetNode> constraints = fq.focus().fwd(RDF.type).one().constraints();
        if(type != null) {
            constraints.eq(type).activate();
        } else {
            for(RDFNode validType : validTypes) {
                constraints.eq(validType).activate();
            }
        }

        DataQuery<RDFNode> result = fq.focus().availableValues()
            .filter(Concept.create("FILTER(isIRI(?s))", "s"));

        return result;
    }


    public Flowable<Resource> resolveAnyOld(
            String pattern,
            Function<String, Query> patternToQuery,
            RDFNode type) {

        DataQuery<RDFNode> dataQuery = createSearchDataQuery(pattern, patternToQuery, type)
                .add(RDF.type);

//		System.out.println("DATA QUERY: ");
//		System.out.println(dataQuery.toConstructQuery());

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
        // TODO Fetch data according to some shape associated with datasets
        return resolveAny(datasetId, idToQuery, DCAT.Dataset)
            .firstElement()
            .map(this::searchResultToDataset)
            .map(e -> new DatasetResolverImpl(this, e.as(DcatDataset.class)));

//		Resource r = model.getResource(datasetId);
//
//		Maybe<DatasetResolver> result = r == null ? Maybe.empty() : Maybe.just(
//				new DatasetResolverImpl(this, r.as(DcatDataset.class)));
//		return result;
    }

    @Override
    public Flowable<DistributionResolver> resolveDistribution(String distributionId) {

//		return resolveAny(distributionId, idToQuery, DCAT.Distribution)
//				.firstElement()
//				.map(this::searchResultToDataset)
//				.map(e -> new DatasetResolverImpl(this, e.as(DcatDataset.class)));

        //new DistributionResolverImpl(datasetResolver, dcatDistribution)
        // FIXME The setup of the DatasetResolver is a hack: We need to fetch the information properly
        return Flowable.empty();
//		return resolveAny(distributionId, idToQuery, DCAT.Dataset)
//				.firstElement()
//				.map(e -> (DistributionResolver)new DistributionResolverImpl(new DatasetResolverImpl(this, e.getPropertyResourceValue(DCATX.relatedDataset).as(DcatDataset.class)), e.as(DcatDistribution.class)))
//				.toFlowable();


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
        return Maybe.empty();
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
