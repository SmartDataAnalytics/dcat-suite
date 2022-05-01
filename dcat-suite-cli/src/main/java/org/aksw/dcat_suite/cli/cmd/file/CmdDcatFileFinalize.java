package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.utils.DcatUtils;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.arq.util.node.NodeEnvsubst;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;

import picocli.CommandLine.Command;

@Command(name = "finalize", separator = "=", description="Create a dcat.ttl file with final URIs", mixinStandardHelpOptions = true)
public class CmdDcatFileFinalize
    implements Callable<Integer>
{
    @Override
    public Integer call() throws Exception {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo(Path.of(""));

        Dataset result = DatasetFactory.create();
        Dataset repoDs = repo.getDataset();
        repoDs.begin(ReadWrite.READ);
        result.asDatasetGraph().addAll(repoDs.asDatasetGraph());
        repoDs.commit();


        Iterator<Node> it = result.asDatasetGraph().listGraphNodes();
        Map<Node, Node> renames = new HashMap<>();
        while (it.hasNext()) {
            Node g = it.next();

            DatasetOneNg dong = DatasetOneNgImpl.create(repoDs, g);
            Resource r = dong.getSelfResource();

            if (r.hasProperty(RDF.type, DCAT.Dataset)) {
                DcatDataset dcatDataset = r.as(DcatDataset.class);

                String id = DcatIdUtils.createDatasetId(dcatDataset);
                dcatDataset.setIdentifier(id);

                String prefix = "urn:mvn:"; // "#"
                renames.put(r.asNode(), NodeFactory.createURI(prefix + id));
            }
        }

        NodeTransformLib2.applyNodeTransform(NodeTransformLib2.wrapWithNullAsIdentity(renames::get), result);


        result = makeDatasetCentric(result);

        RDFDataMgr.write(System.out, result, RDFFormat.TRIG);
        return 0;
    }



    /**
     * First, for every distribution referencing a file, create a copy of the file-related data
     * thereby replacing the file name with the distribution identifier.
     *
     * Then, for every dataset, create a copy of any referenced distribution and replace the distribution
     * identifier with the pattern ${dataset_id}__${distribution_id}.
     *
     * As a result, all information related to a dataset is contained in a set of graphs whose names
     * start with the dataset id.
     *
     *
     * @param ds
     * @return
     */
    public static Dataset makeDatasetCentric(Dataset ds) {
        // DatasetGraph dg = ds.asDatasetGraph();

        Dataset result = DatasetFactory.create();

        // On the first iteration the files are mapped to distribution ids
        // On the second pass, the distributions are mapped to dataset-related ids
        Map<Node, Node> fileToDist = new HashMap<>();

        Collection<DatasetOneNg> distGraphs = DcatUtils.listDistributionGraphs(ds);

        for (DatasetOneNg distGraph : distGraphs) {
            String distributionIri = distGraph.getGraphName();
            ResourceInDataset distRes = new ResourceInDatasetImpl(ds, distributionIri, distGraph.getSelfResource().asNode());

            DcatDistribution dist = distRes.as(DcatDistribution.class);

            String downloadUrl = dist.getDownloadUrl();

            Map<Node, Node> relatedGraphs = listRelatedGraphs(ds, downloadUrl, distributionIri);
            fileToDist.putAll(relatedGraphs);
        }


        Collection<DatasetOneNg> datasetGraphs = DcatUtils.listDatasetGraphs(ds);

        Map<Node, Node> distToDataset = new HashMap<>();
        for (DatasetOneNg datasetGraph : datasetGraphs) {
            Node datasetNode = datasetGraph.getSelfResource().asNode();

            // Add the dataset graph itself
            distToDataset.put(datasetNode, datasetNode);

            // Get all referenced distributions in that dataset graph
            Collection<Node> distNodes = DcatUtils.listDistributionGraphs(ds).stream()
                    .map(g -> g.getSelfResource().asNode())
                    .collect(Collectors.toList());

            for (Node distNode : distNodes) {
                Map<Node, Node> relatedGraphs = listRelatedGraphs(ds, distNode.getURI(), datasetNode.getURI() + "__" + distNode.getURI());

                distToDataset.putAll(relatedGraphs);
            }
        }

        Map<String, String> distToDatasetStr = toIriMap(distToDataset);
        NodeTransform xform = NodeTransformLib2.substPrefix(distToDatasetStr, "__");
        Map<Node, Node> fileToDataset = fileToDist.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> xform.apply(e.getValue())));

        Map<Node, Node> anyToDataset = new HashMap<>();
        anyToDataset.putAll(fileToDataset);
        anyToDataset.putAll(distToDataset);

        for (Node graphNode : anyToDataset.keySet()) {
            Dataset src = DatasetOneNgImpl.create(ds, graphNode);
            result.asDatasetGraph().addAll(src.asDatasetGraph());
        }

        Map<String, String> allRenamesStr = toIriMap(anyToDataset);
        NodeTransformLib2.applyNodeTransform(NodeTransformLib2.substPrefix(allRenamesStr, "__"), result);

        return result;
    }

    public static Map<String, String> toIriMap(Map<? extends Node, ? extends Node> map) {
        Map<String, String> result = map.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getURI(), e -> e.getValue().getURI()));
        return result;
    }

    /**
     * Return a map for how to rename file-related graphs to distribution-related graphs.
     * For example, given a source file.trig and a target urn:mvn:org.example:file:1.0.0
     * a renaming of related graphs would be
     * file.trig__void__prov -&gt; urn:mvn:org.example:file:1.0.0__void__prov
     *
     * The name mapping is from old to new.
     *
     *
     * @param dataset
     * @param source
     * @param target
     * @return
     */
    public static Map<Node, Node> listRelatedGraphs(Dataset dataset, String source, String target) {
        Map<String, String> env = new HashMap<>();
        env.put("SOURCE_NS", source);
        env.put("TARGET_NS", target);

        String queryStr = String.join("\n",
            "PREFIX iri: <http://jsa.aksw.org/fn/iri/>",
            "SELECT DISTINCT ?g ?n {",
            "  BIND ('__' AS ?delim)",
            "  BIND (concat('^(', <env:SOURCE_NS>, ')') AS ?prefixPattern)",
            "  BIND (concat(?prefixPattern, '(', ?delim, '.*)?$') AS ?pattern)",
            "  GRAPH ?g { ?s ?p ?o }",
            "  BIND (str(?g) AS ?gs)",
            "  BIND (isIRI(?g) && strstarts(?gs, <env:SOURCE_NS>) && regex(?gs, ?pattern) AS ?gr)",
            "  FILTER (?gr)",
            "  BIND (IF(?gr, iri:asGiven(replace(?gs, ?prefixPattern, <env:TARGET_NS>)), ?g) AS ?n)",
            "}"
        );

        Query rawQuery = QueryFactory.create(queryStr);
        Query query = QueryUtils.applyNodeTransform(rawQuery, n -> NodeEnvsubst.subst(n, env::get));
        System.out.println(query);
        Map<Node, Node> result = SparqlRx.execSelectRaw(() -> QueryExecutionFactory.create(query, dataset))
            .toMap(b -> b.get(Vars.g), b -> b.get(Vars.n))
            .blockingGet();

        return result;
    }

    //@Parameters(description = "Files to add as datasets to the current dcat repository")
    // public List<String> files = new ArrayList<>();
}
