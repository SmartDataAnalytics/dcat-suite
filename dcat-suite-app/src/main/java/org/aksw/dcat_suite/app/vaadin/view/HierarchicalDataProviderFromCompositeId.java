package org.aksw.dcat_suite.app.vaadin.view;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.concepts.UnaryXExpr;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.path.core.PathOpsNode;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.system.Txn;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

public class HierarchicalDataProviderFromCompositeId
	extends AbstractBackEndHierarchicalDataProvider<Path<Node>, UnaryXExpr>
{
	private static final long serialVersionUID = 1L;

	protected Dataset dataset;
	protected Path<Node> basePath;

    protected boolean includeBasePath;


	public HierarchicalDataProviderFromCompositeId(Dataset dataset) {
		this(dataset, PathOpsNode.newAbsolutePath());
	}

	public HierarchicalDataProviderFromCompositeId(Dataset dataset, Path<Node> basePath) {
		this(dataset, PathOpsNode.newAbsolutePath(), true);
	}

	public HierarchicalDataProviderFromCompositeId(Dataset dataset, boolean includeBasePath) {
		this(dataset, PathOpsNode.newAbsolutePath(), includeBasePath);
	}

	public HierarchicalDataProviderFromCompositeId(Dataset dataset, Path<Node> basePath, boolean includeBasePath) {
		super();
		this.dataset = dataset;
		this.basePath = basePath;
		this.includeBasePath = includeBasePath;
	}

	@Override
	public int getChildCount(HierarchicalQuery<Path<Node>, UnaryXExpr> query) {
		int result;
		Path<Node> parent = query.getParent();

		if (parent == null && includeBasePath) {
            result = 1;
        } else {
        	parent = nullToRoot(parent);
			Query q = createRelation(query.getParent(), query.getFilter().orElse(null)).toQuery();
			q.setDistinct(true);
			result = Txn.calculateRead(dataset,
					() -> SparqlRx.fetchCountQuery((Query qq) -> QueryExecutionFactory.create(qq, dataset), q, null, null)
						.blockingGet().lowerEndpoint().intValue());
        }
		return result;
	}

	public static UnaryRelation createRelation(Path<Node> path, UnaryXExpr predicateExpr) {
		List<Node> nodes = new ArrayList<>(path == null ? Collections.emptyList() : path.getSegments());
		List<Expr> exprs = Optional.ofNullable(predicateExpr).map(UnaryXExpr::getExpr).map(Collections::singletonList).orElse(Collections.emptyList());

		Var v = predicateExpr == null ? Vars.y : predicateExpr.getVar();
		nodes.add(v);

		UnaryRelation result = GraphEntityUtils.createRelationForEntity(nodes, true)
				.filter(exprs)
				.project(v)
				.toUnaryRelation();
		return result;
	}


	@Override
	public boolean hasChildren(Path<Node> item) {
        boolean result;
		if (item == null && includeBasePath) {
            result = true;
        } else {
			Query q = createRelation(item, null).toQuery();
			q.setQueryAskType();
			result = Txn.calculateRead(dataset, () -> SparqlRx.execAsk(qq -> QueryExecutionFactory.create(qq, dataset), q).blockingGet());
        }
		return result;
	}

    protected Path<Node> nullToRoot(Path<Node> path) {
        return path == null ? basePath : path;
    }


	@Override
	protected Stream<Path<Node>> fetchChildrenFromBackEnd(HierarchicalQuery<Path<Node>, UnaryXExpr> query) {
		Path<Node> parent = query.getParent();

		List<Path<Node>> list;
		if (parent == null && includeBasePath) {
            list = Collections.singletonList(basePath);
        } else {
        	parent = nullToRoot(parent);
			Query q = createRelation(parent, query.getFilter().orElse(null)).toQuery();
			q.setDistinct(true);

			Path<Node> tmp = query.getParent();

			Path<Node> basePath = tmp == null ? PathOpsNode.newAbsolutePath() : tmp;

			list = Txn.calculateRead(dataset, () -> SparqlRx.execConceptRaw(qq -> QueryExecutionFactory.create(qq, dataset), q)
					.map(basePath::resolve)
//					.map(node -> {
//						Path<Node> x = basePath.resolve(node);
//
//						Path<Node> y = PathOpsNode.get().fromString( x.toString() );
//
//						System.out.println("GOT PATH: " + y);
//						return y;
//					})
					.toList()
					.blockingGet());
        }

		return list.stream();
	}
}
