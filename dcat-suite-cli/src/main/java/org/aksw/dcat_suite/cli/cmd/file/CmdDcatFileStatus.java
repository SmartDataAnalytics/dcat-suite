package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.dcat.jena.domain.api.MavenEntityCore;
import org.aksw.jena_sparql_api.lookup.LookupServiceSparqlQuery;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryDataset;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.Invoker;

import com.google.common.collect.Streams;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "status", separator = "=", description="Show local/remote DCAT status", mixinStandardHelpOptions = true)
public class CmdDcatFileStatus
    implements Callable<Integer>
{
    @Parameters(description = "Files for which to show status information")
    public List<String> files = new ArrayList<>();

    public Integer call() throws Exception {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo();
        Dataset repoDs = repo.getMemDataset();

        Invoker invoker = new DefaultInvoker();
        Path templatePomXml = DcatRepoLocalMvnUtils.getDefaultPomTemplate();
        ArtifactResolver artifactResolver = MavenUtils.createDefaultArtifactResolver(invoker, templatePomXml, null, null);


        // Find the relation between files, distributions and datasets
        // String statusQuery = "SELECT * { GRAPH ?g { ?s } }"

        // The list of files that have corresponding graphs
        Map<Path, String> fileGraphs = Streams.stream(repoDs.listNames())
            .map(name -> new SimpleEntry<>(Path.of(name), name))
            .filter(e -> Files.exists(e.getKey()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        for (Entry<Path, String> e : fileGraphs.entrySet()) {
            Path path = e.getKey();
            Node fileGraph = NodeFactory.createURI(e.getValue());

            // String localHash = com.google.common.io.Files.asByteSource(path.toFile()).hash(Hashing.sha1()).toString();

            // Get the distributions of fileGraph (if any)
            List<ResourceInDataset> distributions = WrappedIterator.create(repoDs.asDatasetGraph().find(Node.ANY, Node.ANY, DCAT.downloadURL.asNode(), fileGraph))
                .filterKeep(quad -> Objects.equals(quad.getGraph(), quad.getSubject()))
                .mapWith(quad -> (ResourceInDataset)new ResourceInDatasetImpl(repoDs, quad.getGraph().getURI(), quad.getSubject()))
                .toList();


            // Find the entity with the sha1 hash
            Query query = QueryFactory.create("PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX spdx: <http://spdx.org/rdf/terms#> SELECT ?e ?value ?timestamp { GRAPH ?g { ?e spdx:checksum [ spdx:algorithm \"sha1\" ; spdx:checksumValue ?value ] } GRAPH ?gp { [ <http://w3id.org/rpif/vocab#graphIri> ?g ] prov:wasGeneratedBy [ prov:endedAtTime ?timestamp ] } }");
            LookupService<Node, Entry<String, Instant>> ls =
                    new LookupServiceSparqlQuery(new QueryExecutionFactoryDataset(repoDs), query, Var.alloc("e"))
                    .mapValues((key, table) -> {
                        Iterator<Binding> it = table.rows();
                        Entry<String, Instant> r = null;
                        if (it.hasNext()) {
                            Binding b = it.next();
                            r = new SimpleEntry<>(b.get("value").getLiteralLexicalForm(), ((XSDDateTime)b.get("timestamp").getLiteralValue()).asCalendar().toInstant());
                        }
                        return r;
                    });

            Entry<String, Instant> localHash = ls.fetchItem(fileGraph);



            String remoteHash = null;
            for (ResourceInDataset distribution : distributions) {
                String mvnUrn = distribution.getGraphName();
                MavenEntityCore mvnEntity = MavenEntityCore.parse(mvnUrn);

                mvnEntity.setType(mvnEntity.getType() + ".sha1");

                String hashId = MavenEntityCore.toString(mvnEntity);

                ArtifactResolutionRequest request = new ArtifactResolutionRequestImpl();
                request.setArtifactId(hashId);
                request.setUpdateCache(true);

                try {
                    Path artifactPath = artifactResolver.resolve(request);
                    remoteHash = Files.exists(artifactPath) ? Files.readString(artifactPath) : null;
                } catch (Exception ex) {
                    remoteHash = null;
                }

            }

//            List<ResourceInDataset> datasets = WrappedIterator.create(repoDs.asDatasetGraph().find(Node.ANY, Node.ANY, DCAT.distribution.asNode(), fileGraph))
//                    .mapWith(quad -> (ResourceInDataset)new ResourceInDatasetImpl(repoDs, quad.getGraph().getURI(), quad.getSubject()))
//                    .toList();


            // TODO Warn if remote distributions differ in hash


            System.out.println(path + "\t" + distributions + "\t" + localHash + "\t" + remoteHash);
        }

//
//        Path basePath = repo.getBasePath();
//        for (String file : files) {
//            Path relPath = DcatRepoLocalUtils.normalizeRelPath(basePath, file);
//
//            Dataset ds = DatasetOneNgImpl.create(repoDs, relPath.toString());
//            RDFDataMgr.write(System.out, ds, RDFFormat.TRIG_PRETTY);
//        }


        return 0;
    }

}
