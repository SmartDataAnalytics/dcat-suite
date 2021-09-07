package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.path.core.PathOpsPE;
import org.aksw.jena_sparql_api.path.core.PathPE;
import org.aksw.jena_sparql_api.path.relgen.RelationGeneratorSimple;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmtMgr;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.lang.arq.ParseException;

import com.google.common.primitives.Ints;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

public class HierarchicalDataProviderForPathPE
    extends AbstractBackEndHierarchicalDataProvider<PathPE, Node>
{

    protected PathPE basePathPE;

    // If includeBasePathPE is true (default), then null has basePathPE as its only child
    // Otherwise the children of null are the children of basePathPE
    protected boolean includeBasePathPE;
    // protected boolean foldersOnly;
    protected Predicate<PathPE> folderItemFilter;



    protected SparqlQueryConnection conn;
    protected RelationGeneratorSimple relgen;


    public static Relation createShaclRelation() {

        Query query;
        try {
            query = SparqlStmtMgr.loadQuery("shacl-relation.rq");
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        Relation result = RelationUtils.fromQuery(query);

        return result;
    }

    public static HierarchicalDataProviderForPathPE createTest() {
        Dataset m = RDFDataMgr.loadDataset("dcat-ap_2.0.0_shacl_shapes.ttl");
        Node root = NodeFactory.createURI("http://data.europa.eu/r5r#Catalog_Shape");

        RDFConnection conn = RDFConnectionFactory.connect(m);
        Relation r = createShaclRelation();
        RelationGeneratorSimple gen = RelationGeneratorSimple.create(r);


        PathPE exprs = PathOpsPE.newAbsolutePath();

        // gen.process(exprs);



        return new HierarchicalDataProviderForPathPE(exprs, true, null, conn, gen);
    }


    /** Whether to only show the folder structure */
    public HierarchicalDataProviderForPathPE(
            PathPE basePathPE,
            boolean includeBasePathPE,
            Predicate<PathPE> folderItemFilter,
            SparqlQueryConnection conn,
            RelationGeneratorSimple relgen) {
        super();
        this.basePathPE = basePathPE;
        // this.foldersOnly = foldersOnly;
        this.includeBasePathPE = includeBasePathPE;
        this.folderItemFilter = folderItemFilter;

        this.conn = conn;
        this.relgen = relgen;
    }

//    public static HierarchicalDataProviderForPathPE createForFolderStructure(PathPE basePathPE) {
//        return new HierarchicalDataProviderForPathPE(basePathPE, true, Files::isDirectory);
//    }


    protected PathPE nullToRoot(PathPE path) {
        return path == null ? basePathPE : path;
    }

    @Override
    public int getChildCount(HierarchicalQuery<PathPE, Node> query) {
        int result = Ints.saturatedCast(fetchChildrenFromBackEnd(query).count());
        return result;
    }

    @Override
    public boolean hasChildren(PathPE item) {
        HierarchicalQuery<PathPE, Node> hq = new HierarchicalQuery<>(0, 1, Collections.emptyList(), null, null, item);

        boolean result = fetchChildrenFromBackEnd(hq).findAny().isPresent();
        return result;
    }

    @Override
    protected Stream<PathPE> fetchChildrenFromBackEnd(HierarchicalQuery<PathPE, Node> query) {
        Stream<PathPE> result;

        PathPE p = query.getParent();
        if (p == null) {
            p = basePathPE;
        }
        PathPE parent = p;

        relgen.process(parent);
        UnaryRelation ur = relgen.getCurrentConcept();
        Query q = ur.toQuery();
        q.setLimit(query.getLimit());
        q.setOffset(query.getOffset());
        q.setDistinct(true);


        Var urv = ur.getVar();
        QueryUtils.injectFilter(q, new E_Bound(new ExprVar(urv)));

        Query qp = QueryUtils.applyOpTransform(q, AlgebraUtils.createDefaultRewriter()::rewrite);
        // conn.querySelect(qp, row -> System.out.println(row));


        // FIXME unbound var is null and null is not allowed in rx...
        result = SparqlRx.execConceptRaw(() -> conn.query(qp), urv)
            .map(node -> parent.resolve(node))
            .blockingStream()
            ;


        return result;
    }
}
