package org.aksw.dcat_suite.cli.main;

import java.util.Collections;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.service.ServiceExecutorFactory;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.QueryExecUtils;

public class MainExampleJenaService {
    public static final String QUERY_STR = String.join("\n",
            "PREFIX wd: <http://www.wikidata.org/entity/>",
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
            "PREFIX dbr: <http://dbpedia.org/resource/>",
            "SELECT * {",
            "  SERVICE <http://query.wikidata.org/sparql> {",
            "    SELECT * {",
            "      ?s rdfs:label \"Apache Jena\"@en",
            "     } LIMIT 10",
            "  }",
            "}");


    public static void main(String[] args) {

        Dataset dataset = DatasetFactory.empty();

        // Default: Send request to Wikidata
        {
            execQueryAndShowResult(dataset, QUERY_STR, null);

            /* ---------------
             * | s           |
             * ===============
             * | wd:Q1686799 |
             * ---------------
             */
        }

        // Example (1): Relay requests to Wikidata to DBpedia instead
        {
            Node WIKIDATA = NodeFactory.createURI("http://query.wikidata.org/sparql");
            Node DBPEDIA = NodeFactory.createURI("http://dbpedia.org/sparql");

            ServiceExecutorFactory relaySef = (opExecute, original, binding, execCxt) -> {
                    if (opExecute.getService().equals(WIKIDATA)) {
                        opExecute = new OpService(DBPEDIA, opExecute.getSubOp(), opExecute.getSilent());
                        return ServiceExecutorRegistry.httpService.createExecutor(opExecute, original, binding, execCxt);
                    }
                    return null;
                };

            Context cxt = ARQ.getContext().copy();
            ServiceExecutorRegistry registry = new ServiceExecutorRegistry();
            registry.add(relaySef);

            ServiceExecutorRegistry.set(cxt, registry);
            execQueryAndShowResult(dataset, QUERY_STR, cxt);

            /*
             * -------------------
             * | s               |
             * ===================
             * | dbr:Apache_Jena |
             * -------------------
             */
        }


        // Example (2): Suppress remote requests - make any SERVICE request return the input binding
        {
            ServiceExecutorFactory noop = (opExecute, original, binding, execCxt) ->
                () -> QueryIterPlainWrapper.create(Collections.singleton(binding).iterator());

            Context cxt = ARQ.getContext().copy();
            ServiceExecutorRegistry registry = new ServiceExecutorRegistry();
            registry.add(noop);

            ServiceExecutorRegistry.set(cxt, registry);
            execQueryAndShowResult(dataset, QUERY_STR, cxt);

            /*
             * -----
             * | s |
             * =====
             * |   |
             * -----
             */
        }
    }

    public static void execQueryAndShowResult(
            Dataset dataset,
            String queryStr,
            Context cxt) {
        try {
            try (QueryExec exec = QueryExecDatasetBuilder.create()
                    .dataset(dataset.asDatasetGraph())
                    .query(queryStr)
                    .context(cxt)
                    .build()) {
                QueryExecUtils.exec(exec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
